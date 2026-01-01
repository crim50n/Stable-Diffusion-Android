package dev.minios.pdaiv1.domain.entity

/**
 * Represents a fal.ai endpoint loaded from OpenAPI schema.
 * Can be either built-in (shipped with app) or custom (imported by user).
 */
data class FalAiEndpoint(
    val id: String,
    val endpointId: String,
    val title: String,
    val description: String,
    val category: FalAiEndpointCategory,
    val group: String, // For hierarchical display, e.g. "FLUX 2", "FLUX LoRA"
    val thumbnailUrl: String,
    val playgroundUrl: String,
    val documentationUrl: String,
    val isCustom: Boolean,
    val schema: FalAiEndpointSchema,
)

enum class FalAiEndpointCategory(val key: String, val displayName: String) {
    TEXT_TO_IMAGE("text-to-image", "Text to Image"),
    IMAGE_TO_IMAGE("image-to-image", "Image to Image"),
    INPAINTING("inpainting", "Inpainting"),
    UPSCALING("upscaling", "Upscaling"),
    OTHER("other", "Other");

    companion object {
        fun fromKey(key: String): FalAiEndpointCategory =
            entries.find { it.key.equals(key, ignoreCase = true) } ?: OTHER
    }
}

/**
 * Schema definition for a fal.ai endpoint, parsed from OpenAPI JSON.
 */
data class FalAiEndpointSchema(
    val baseUrl: String,
    val submissionPath: String,
    val inputProperties: List<FalAiInputProperty>,
    val requiredProperties: List<String>,
    val propertyOrder: List<String>,
)

/**
 * Represents an input property from the OpenAPI schema.
 */
data class FalAiInputProperty(
    val name: String,
    val title: String,
    val description: String,
    val type: FalAiPropertyType,
    val default: Any? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val enumValues: List<String>? = null,
    val isRequired: Boolean = false,
    val isImageInput: Boolean = false,
    val arrayItemProperties: List<FalAiInputProperty>? = null,
    val linkedMaskProperty: String? = null, // For inpainting: links image_url to its mask_url
)

enum class FalAiPropertyType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    ARRAY,
    OBJECT,
    IMAGE_URL,
    IMAGE_URL_ARRAY,
    INPAINT,  // Combined image + mask drawing
    ENUM,
    IMAGE_SIZE;

    companion object {
        fun fromString(
            type: String?,
            hasEnum: Boolean = false,
            isImageField: Boolean = false,
            isImageArray: Boolean = false,
        ): FalAiPropertyType {
            if (isImageArray) return IMAGE_URL_ARRAY
            if (isImageField) return IMAGE_URL
            if (hasEnum) return ENUM
            return when (type?.lowercase()) {
                "string" -> STRING
                "integer" -> INTEGER
                "number" -> NUMBER
                "boolean" -> BOOLEAN
                "array" -> ARRAY
                "object" -> OBJECT
                else -> STRING
            }
        }
    }
}
