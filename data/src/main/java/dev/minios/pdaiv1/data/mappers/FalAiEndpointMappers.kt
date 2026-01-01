package dev.minios.pdaiv1.data.mappers

import com.google.gson.Gson
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import dev.minios.pdaiv1.domain.entity.FalAiEndpointCategory
import dev.minios.pdaiv1.domain.entity.FalAiEndpointSchema
import dev.minios.pdaiv1.domain.entity.FalAiInputProperty
import dev.minios.pdaiv1.domain.entity.FalAiPropertyType
import dev.minios.pdaiv1.storage.db.persistent.entity.FalAiEndpointEntity

private val gson = Gson()

fun FalAiEndpointEntity.toDomain(): FalAiEndpoint = FalAiEndpoint(
    id = id,
    endpointId = endpointId,
    title = title,
    description = description,
    category = FalAiEndpointCategory.fromKey(category),
    group = group,
    thumbnailUrl = thumbnailUrl,
    playgroundUrl = playgroundUrl,
    documentationUrl = documentationUrl,
    isCustom = isCustom,
    schema = gson.fromJson(schemaJson, FalAiEndpointSchemaDto::class.java).toDomain(),
)

fun FalAiEndpoint.toEntity(): FalAiEndpointEntity = FalAiEndpointEntity(
    id = id,
    endpointId = endpointId,
    title = title,
    description = description,
    category = category.key,
    group = group,
    thumbnailUrl = thumbnailUrl,
    playgroundUrl = playgroundUrl,
    documentationUrl = documentationUrl,
    isCustom = isCustom,
    schemaJson = gson.toJson(schema.toDto()),
)

// DTOs for JSON serialization
private data class FalAiEndpointSchemaDto(
    val baseUrl: String,
    val submissionPath: String,
    val inputProperties: List<FalAiInputPropertyDto>,
    val requiredProperties: List<String>,
    val propertyOrder: List<String>,
)

private data class FalAiInputPropertyDto(
    val name: String,
    val title: String,
    val description: String,
    val type: String,
    val default: Any?,
    val minimum: Double?,
    val maximum: Double?,
    val enumValues: List<String>?,
    val isRequired: Boolean,
    val isImageInput: Boolean,
)

private fun FalAiEndpointSchemaDto.toDomain() = FalAiEndpointSchema(
    baseUrl = baseUrl,
    submissionPath = submissionPath,
    inputProperties = inputProperties.map { it.toDomain() },
    requiredProperties = requiredProperties,
    propertyOrder = propertyOrder,
)

private fun FalAiInputPropertyDto.toDomain() = FalAiInputProperty(
    name = name,
    title = title,
    description = description,
    type = FalAiPropertyType.valueOf(type),
    default = default,
    minimum = minimum,
    maximum = maximum,
    enumValues = enumValues,
    isRequired = isRequired,
    isImageInput = isImageInput,
)

private fun FalAiEndpointSchema.toDto() = FalAiEndpointSchemaDto(
    baseUrl = baseUrl,
    submissionPath = submissionPath,
    inputProperties = inputProperties.map { it.toDto() },
    requiredProperties = requiredProperties,
    propertyOrder = propertyOrder,
)

private fun FalAiInputProperty.toDto() = FalAiInputPropertyDto(
    name = name,
    title = title,
    description = description,
    type = type.name,
    default = default,
    minimum = minimum?.toDouble(),
    maximum = maximum?.toDouble(),
    enumValues = enumValues,
    isRequired = isRequired,
    isImageInput = isImageInput,
)
