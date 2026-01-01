package dev.minios.pdaiv1.data.mappers

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import dev.minios.pdaiv1.domain.entity.FalAiEndpointCategory
import dev.minios.pdaiv1.domain.entity.FalAiEndpointSchema
import dev.minios.pdaiv1.domain.entity.FalAiInputProperty
import dev.minios.pdaiv1.domain.entity.FalAiPropertyType
import java.util.UUID

/**
 * Parses OpenAPI JSON schema into FalAiEndpoint domain entity.
 */
object FalAiOpenApiParser {

    private val gson = Gson()

    /**
     * Parses an OpenAPI JSON schema into FalAiEndpoint.
     * @param json The OpenAPI JSON content
     * @param isCustom Whether this is a user-imported endpoint
     * @param assetPath Optional asset path for built-in endpoints, used to extract group name
     */
    fun parse(json: String, isCustom: Boolean = true, assetPath: String? = null): FalAiEndpoint {
        val root = gson.fromJson(json, JsonObject::class.java)

        val info = root.getAsJsonObject("info")
        val metadata = info.getAsJsonObject("x-fal-metadata")

        val endpointId = metadata.get("endpointId").asString
        val id = if (isCustom) "custom-${UUID.randomUUID()}" else endpointId

        val category = FalAiEndpointCategory.fromKey(
            metadata.get("category")?.asString ?: "other"
        )

        val schema = parseSchema(root)

        // Use endpointId as title, removing "fal-ai/" prefix
        val title = endpointId.removePrefix("fal-ai/")

        // Extract group from asset path or use category as fallback
        val group = extractGroupFromPath(assetPath) ?: category.displayName

        return FalAiEndpoint(
            id = id,
            endpointId = endpointId,
            title = title,
            description = info.get("description")?.asString ?: "",
            category = category,
            group = group,
            thumbnailUrl = metadata.get("thumbnailUrl")?.asString ?: "",
            playgroundUrl = metadata.get("playgroundUrl")?.asString ?: "",
            documentationUrl = metadata.get("documentationUrl")?.asString ?: "",
            isCustom = isCustom,
            schema = schema,
        )
    }

    private fun parseSchema(root: JsonObject): FalAiEndpointSchema {
        val servers = root.getAsJsonArray("servers")
        val baseUrl = servers?.get(0)?.asJsonObject?.get("url")?.asString
            ?: "https://queue.fal.run"

        val paths = root.getAsJsonObject("paths")
        val (submissionPath, inputSchemaRef) = findSubmissionEndpoint(paths)

        val components = root.getAsJsonObject("components")
        val schemas = components?.getAsJsonObject("schemas")

        val inputSchema = resolveSchema(inputSchemaRef, schemas)
        val (properties, required, order) = parseInputProperties(inputSchema, schemas)

        return FalAiEndpointSchema(
            baseUrl = baseUrl,
            submissionPath = submissionPath,
            inputProperties = properties,
            requiredProperties = required,
            propertyOrder = order,
        )
    }

    private fun findSubmissionEndpoint(paths: JsonObject): Pair<String, JsonObject?> {
        for ((path, methods) in paths.entrySet()) {
            val methodsObj = methods.asJsonObject
            val post = methodsObj.getAsJsonObject("post")
            if (post != null) {
                val requestBody = post.getAsJsonObject("requestBody")
                if (requestBody != null) {
                    val content = requestBody.getAsJsonObject("content")
                    val appJson = content?.getAsJsonObject("application/json")
                    val schema = appJson?.getAsJsonObject("schema")
                    return path to schema
                }
            }
        }
        throw IllegalArgumentException("No POST endpoint with requestBody found in OpenAPI schema")
    }

    private fun resolveSchema(schemaRef: JsonObject?, schemas: JsonObject?): JsonObject? {
        if (schemaRef == null) return null

        val ref = schemaRef.get("\$ref")?.asString
        if (ref != null && schemas != null) {
            val refName = ref.split("/").last()
            return schemas.getAsJsonObject(refName)
        }
        return schemaRef
    }

    private fun parseInputProperties(
        inputSchema: JsonObject?,
        schemas: JsonObject?,
    ): Triple<List<FalAiInputProperty>, List<String>, List<String>> {
        if (inputSchema == null) return Triple(emptyList(), emptyList(), emptyList())

        val properties = inputSchema.getAsJsonObject("properties") ?: return Triple(emptyList(), emptyList(), emptyList())
        val required = inputSchema.getAsJsonArray("required")
            ?.map { it.asString } ?: emptyList()
        val order = inputSchema.getAsJsonArray("x-fal-order-properties")
            ?.map { it.asString } ?: properties.keySet().toList()

        // Check if this is an inpainting endpoint (has both image_url and mask_url in required)
        val hasMaskUrl = properties.has("mask_url")
        val hasImageUrl = properties.has("image_url")
        val isInpaintingEndpoint = hasMaskUrl && hasImageUrl

        val parsedProperties = mutableListOf<FalAiInputProperty>()

        for ((name, propElement) in properties.entrySet()) {
            // Skip mask_url if this is an inpainting endpoint - it will be handled by image_url's INPAINT type
            if (isInpaintingEndpoint && name == "mask_url") {
                continue
            }

            var prop = propElement.asJsonObject
            val originalProp = prop // Keep original for default value extraction

            // Check if this is an image_size field with anyOf (ImageSize object + presets)
            val anyOf = prop.getAsJsonArray("anyOf")
            val isImageSizeField = name == "image_size" && anyOf != null && anyOf.any { element ->
                val refStr = element.asJsonObject?.get("\$ref")?.asString
                refStr?.contains("ImageSize") == true
            }

            var imageSizePresets: List<String>? = null

            if (isImageSizeField && anyOf != null) {
                // Extract presets from enum option
                val enumOption = anyOf.find { it.asJsonObject.has("enum") }?.asJsonObject
                imageSizePresets = enumOption?.getAsJsonArray("enum")?.map { it.asString }
            } else if (anyOf != null && anyOf.size() > 0) {
                // Handle other anyOf - pick enum option or first option
                val enumOption = anyOf.find {
                    it.asJsonObject.has("enum")
                }?.asJsonObject
                prop = enumOption ?: anyOf.get(0).asJsonObject
            }

            // Resolve $ref if present (skip for image_size as we handle it specially)
            if (!isImageSizeField) {
                val ref = prop.get("\$ref")?.asString
                if (ref != null && schemas != null) {
                    val refName = ref.split("/").last()
                    prop = schemas.getAsJsonObject(refName) ?: prop
                }
            }

            val isImageField = name.contains("image", ignoreCase = true) ||
                               name.contains("mask", ignoreCase = true)
            val hasEnum = prop.has("enum")
            val typeStr = prop.get("type")?.asString

            // Check if this is an array of image URLs
            val isImageArray = typeStr == "array" && isImageField &&
                               originalProp.getAsJsonObject("items")?.get("type")?.asString == "string"

            val enumValues = when {
                isImageSizeField -> imageSizePresets
                hasEnum -> prop.getAsJsonArray("enum")?.map { it.asString }
                else -> null
            }

            // Determine if this is an inpainting image field (image_url with linked mask_url)
            val isInpaintImageField = isInpaintingEndpoint && name == "image_url"

            val propertyType = when {
                isInpaintImageField -> FalAiPropertyType.INPAINT
                isImageSizeField -> FalAiPropertyType.IMAGE_SIZE
                isImageArray -> FalAiPropertyType.IMAGE_URL_ARRAY
                else -> FalAiPropertyType.fromString(typeStr, hasEnum, isImageField && typeStr == "string")
            }

            // Parse array item properties if this is an array type
            val arrayItemProperties = if (typeStr == "array") {
                parseArrayItemProperties(originalProp, schemas)
            } else null

            parsedProperties.add(
                FalAiInputProperty(
                    name = name,
                    title = if (isInpaintImageField) "Image & Mask" else (prop.get("title")?.asString ?: name),
                    description = if (isInpaintImageField) {
                        "Select an image and draw a mask on the area to inpaint"
                    } else {
                        prop.get("description")?.asString ?: ""
                    },
                    type = propertyType,
                    default = parseDefault(originalProp),
                    minimum = prop.get("minimum")?.asNumber,
                    maximum = prop.get("maximum")?.asNumber,
                    enumValues = enumValues,
                    isRequired = required.contains(name),
                    isImageInput = isImageField && (typeStr == "string" || prop.has("format")),
                    arrayItemProperties = arrayItemProperties,
                    linkedMaskProperty = if (isInpaintImageField) "mask_url" else null,
                )
            )
        }

        return Triple(parsedProperties, required, order)
    }

    private fun parseDefault(prop: JsonObject): Any? {
        val default = prop.get("default") ?: return null
        return when {
            default.isJsonPrimitive -> {
                val primitive = default.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber
                    primitive.isString -> primitive.asString
                    else -> primitive.asString
                }
            }
            default.isJsonArray -> default.asJsonArray.map { it.asString }
            else -> null
        }
    }

    /**
     * Parses array item properties from the "items" field.
     * Handles $ref resolution for object schemas like LoraWeight.
     */
    private fun parseArrayItemProperties(
        arrayProp: JsonObject,
        schemas: JsonObject?,
    ): List<FalAiInputProperty>? {
        val items = arrayProp.getAsJsonObject("items") ?: return null

        // Resolve $ref if present
        var itemSchema = items
        val ref = items.get("\$ref")?.asString
        if (ref != null && schemas != null) {
            val refName = ref.split("/").last()
            itemSchema = schemas.getAsJsonObject(refName) ?: return null
        }

        // Only parse if it's an object type with properties
        if (itemSchema.get("type")?.asString != "object") return null
        val properties = itemSchema.getAsJsonObject("properties") ?: return null
        val required = itemSchema.getAsJsonArray("required")?.map { it.asString } ?: emptyList()
        val order = itemSchema.getAsJsonArray("x-fal-order-properties")?.map { it.asString }
            ?: properties.keySet().toList()

        val parsedProperties = mutableListOf<FalAiInputProperty>()

        for (propName in order) {
            val propElement = properties.get(propName) ?: continue
            val prop = propElement.asJsonObject

            val hasEnum = prop.has("enum")
            val typeStr = prop.get("type")?.asString

            val enumValues = if (hasEnum) {
                prop.getAsJsonArray("enum")?.map { it.asString }
            } else null

            parsedProperties.add(
                FalAiInputProperty(
                    name = propName,
                    title = prop.get("title")?.asString ?: propName,
                    description = prop.get("description")?.asString ?: "",
                    type = FalAiPropertyType.fromString(typeStr, hasEnum, false),
                    default = parseDefault(prop),
                    minimum = prop.get("minimum")?.asNumber,
                    maximum = prop.get("maximum")?.asNumber,
                    enumValues = enumValues,
                    isRequired = required.contains(propName),
                    isImageInput = false,
                )
            )
        }

        return parsedProperties.takeIf { it.isNotEmpty() }
    }

    /**
     * Extracts group name from asset path.
     * E.g., "falai-endpoints/flux-2/edit/openapi.json" -> "FLUX 2"
     */
    private fun extractGroupFromPath(assetPath: String?): String? {
        if (assetPath == null) return null

        // Remove base directory prefix
        val relativePath = assetPath
            .removePrefix("falai-endpoints/")
            .removePrefix("falai-endpoints\\")

        // Get the first directory segment
        val firstSegment = relativePath.split("/", "\\").firstOrNull()
            ?: return null

        // Format the segment into a readable group name
        return formatGroupName(firstSegment)
    }

    /**
     * Formats a directory name like "flux-2" into a readable group name like "FLUX 2".
     */
    private fun formatGroupName(dirName: String): String {
        return dirName.split("-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                when (word.lowercase()) {
                    "flux" -> "FLUX"
                    "lora" -> "LoRA"
                    "kontext" -> "Kontext"
                    "krea" -> "Krea"
                    "pro" -> "Pro"
                    "dev" -> "Dev"
                    else -> word.replaceFirstChar { it.uppercase() }
                }
            }
    }
}
