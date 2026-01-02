package dev.minios.pdaiv1.domain.mocks

import dev.minios.pdaiv1.domain.entity.SwarmUiModel

val mockSwarmUiModel = SwarmUiModel(
    name = "mock-model",
    title = "Mock Model",
    author = "Test Author",
)

val mockSwarmUiModels = listOf(
    mockSwarmUiModel,
    SwarmUiModel(
        name = "another-model",
        title = "Another Model",
        author = "Another Author",
    ),
)
