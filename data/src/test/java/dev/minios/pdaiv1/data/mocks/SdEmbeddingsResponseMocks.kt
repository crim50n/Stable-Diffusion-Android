package dev.minios.pdaiv1.data.mocks

import dev.minios.pdaiv1.network.response.SdEmbeddingsResponse

val mockSdEmbeddingsResponse = SdEmbeddingsResponse(
    loaded = mapOf("1504" to "5598"),
)

val mockEmptySdEmbeddingsResponse = SdEmbeddingsResponse(
    loaded = emptyMap(),
)
