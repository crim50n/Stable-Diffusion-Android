package dev.minios.pdaiv1.data.mocks

import dev.minios.pdaiv1.network.response.DownloadableModelResponse

val mockDownloadableModelsResponse = listOf(
    DownloadableModelResponse(
        id = "1",
        name = "Model 1",
        size = "5 Gb",
        sources = listOf("https://example.com/1.html"),
    )
)
