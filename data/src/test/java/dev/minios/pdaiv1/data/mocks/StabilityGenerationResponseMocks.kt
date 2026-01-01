package dev.minios.pdaiv1.data.mocks

import dev.minios.pdaiv1.network.response.StabilityGenerationResponse

val mockStabilityGenerationResponse = StabilityGenerationResponse(
    artifacts = listOf(
        StabilityGenerationResponse.Artifact(
            base64 = "base64",
            finishReason = "reasonable reason",
            seed = 5598L,
        ),
    ),
)

val mockBadStabilityGenerationResponse = StabilityGenerationResponse(emptyList())
