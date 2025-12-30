package com.shifthackz.aisdv1.domain.mocks

import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.domain.entity.FalAiEndpointCategory
import com.shifthackz.aisdv1.domain.entity.FalAiEndpointSchema
import com.shifthackz.aisdv1.domain.entity.FalAiInputProperty
import com.shifthackz.aisdv1.domain.entity.FalAiPayload
import com.shifthackz.aisdv1.domain.entity.FalAiPropertyType

val mockFalAiInputProperty = FalAiInputProperty(
    name = "prompt",
    title = "Prompt",
    description = "The prompt to generate an image from",
    type = FalAiPropertyType.STRING,
    default = null,
    minimum = null,
    maximum = null,
    enumValues = null,
    isRequired = true,
    isImageInput = false,
)

val mockFalAiEndpointSchema = FalAiEndpointSchema(
    baseUrl = "https://queue.fal.run",
    submissionPath = "/fal-ai/flux/schnell",
    inputProperties = listOf(mockFalAiInputProperty),
    requiredProperties = listOf("prompt"),
    propertyOrder = listOf("prompt"),
)

val mockFalAiEndpoint = FalAiEndpoint(
    id = "fal-ai/flux/schnell",
    endpointId = "fal-ai/flux/schnell",
    title = "FLUX.1 [schnell]",
    description = "Fast text to image generation",
    category = FalAiEndpointCategory.TEXT_TO_IMAGE,
    group = "FLUX",
    thumbnailUrl = "https://fal.ai/thumbnails/flux-schnell.jpg",
    playgroundUrl = "https://fal.ai/models/fal-ai/flux/schnell",
    documentationUrl = "https://fal.ai/models/fal-ai/flux/schnell/api",
    isCustom = false,
    schema = mockFalAiEndpointSchema,
)

val mockFalAiEndpoints = listOf(mockFalAiEndpoint)

val mockFalAiPayload = FalAiPayload(
    endpointId = "fal-ai/flux/schnell",
    parameters = mapOf(
        "prompt" to "a beautiful sunset",
        "num_inference_steps" to 4,
    ),
)
