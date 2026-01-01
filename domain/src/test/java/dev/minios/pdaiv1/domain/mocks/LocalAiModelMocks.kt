package dev.minios.pdaiv1.domain.mocks

import dev.minios.pdaiv1.domain.entity.LocalAiModel

val mockLocalAiModels = listOf(
    LocalAiModel.CustomOnnx,
    LocalAiModel(
        id = "1",
        type = LocalAiModel.Type.ONNX,
        name = "Model 1",
        size = "5 Gb",
        sources = listOf("https://example.com/1.html"),
        downloaded = false,
        selected = false,
    ),
)
