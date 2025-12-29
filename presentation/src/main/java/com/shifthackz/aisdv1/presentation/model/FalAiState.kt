package com.shifthackz.aisdv1.presentation.model

import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.domain.entity.FalAiInputProperty
import com.shifthackz.aisdv1.domain.entity.FalAiPropertyType

/**
 * UI representation of a FalAi endpoint for display.
 */
data class FalAiEndpointUi(
    val id: String,
    val endpointId: String,
    val title: String,
    val description: String,
    val category: String,
    val group: String, // For hierarchical display
    val thumbnailUrl: String,
    val isCustom: Boolean,
    val properties: List<FalAiPropertyUi>,
)

/**
 * UI representation of an input property with current value.
 */
data class FalAiPropertyUi(
    val name: String,
    val title: String,
    val description: String,
    val type: FalAiPropertyType,
    val required: Boolean,
    val defaultValue: Any?,
    val currentValue: Any?,
    val minimum: Double?,
    val maximum: Double?,
    val enumValues: List<String>,
    val isAdvanced: Boolean,
    val arrayItemProperties: List<FalAiPropertyUi>?,
    val linkedMaskProperty: String? = null, // For inpainting: links image_url to its mask_url
) {
    companion object {
        fun fromDomain(property: FalAiInputProperty, isRequired: Boolean): FalAiPropertyUi {
            // For INPAINT type, it's a main property (not advanced)
            val isAdvanced = property.type != FalAiPropertyType.INPAINT
                    && property.name !in listOf("prompt", "image_url", "mask_url")
                    && !property.name.contains("image")
                    && !property.name.contains("mask")

            return FalAiPropertyUi(
                name = property.name,
                title = property.title.ifBlank { property.name.replace("_", " ").replaceFirstChar { it.uppercase() } },
                description = property.description,
                type = property.type,
                required = isRequired,
                defaultValue = property.default,
                currentValue = property.default,
                minimum = property.minimum?.toDouble(),
                maximum = property.maximum?.toDouble(),
                enumValues = property.enumValues ?: emptyList(),
                isAdvanced = isAdvanced,
                arrayItemProperties = property.arrayItemProperties?.map { itemProp ->
                    fromDomain(itemProp, itemProp.isRequired)
                },
                linkedMaskProperty = property.linkedMaskProperty,
            )
        }
    }
}

/**
 * State for FalAi generation form.
 */
data class FalAiFormState(
    val endpoints: List<FalAiEndpointUi> = emptyList(),
    val selectedEndpoint: FalAiEndpointUi? = null,
    val propertyValues: Map<String, Any?> = emptyMap(),
    val advancedOptionsVisible: Boolean = false,
    val isLoading: Boolean = false,
)

fun FalAiEndpoint.toUi(): FalAiEndpointUi = FalAiEndpointUi(
    id = id,
    endpointId = endpointId,
    title = title,
    description = description,
    category = category.displayName,
    group = group,
    thumbnailUrl = thumbnailUrl,
    isCustom = isCustom,
    properties = schema.inputProperties.map { prop ->
        FalAiPropertyUi.fromDomain(prop, schema.requiredProperties.contains(prop.name))
    },
)
