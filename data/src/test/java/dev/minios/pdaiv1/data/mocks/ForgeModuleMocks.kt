package dev.minios.pdaiv1.data.mocks

import dev.minios.pdaiv1.domain.entity.ForgeModule

val mockForgeModule = ForgeModule(
    name = "ADetailer",
    path = "extensions/adetailer",
)

val mockForgeModules = listOf(
    mockForgeModule,
    ForgeModule(
        name = "ControlNet",
        path = "extensions/sd-webui-controlnet",
    ),
)
