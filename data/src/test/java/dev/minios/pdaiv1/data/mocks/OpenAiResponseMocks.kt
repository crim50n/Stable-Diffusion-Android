package dev.minios.pdaiv1.data.mocks

import dev.minios.pdaiv1.network.model.OpenAiImageRaw
import dev.minios.pdaiv1.network.response.OpenAiResponse

val mockSuccessOpenAiResponse = OpenAiResponse(
    created = System.currentTimeMillis(),
    data = listOf(
        OpenAiImageRaw(
            "base64",
            "https://openai.com",
            "prompt",
        ),
    ),
)

val mockBadOpenAiResponse = OpenAiResponse(
    created = System.currentTimeMillis(),
    data = emptyList(),
)
