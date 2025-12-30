package com.shifthackz.aisdv1.data.mocks

import com.shifthackz.aisdv1.domain.entity.ForgeModule

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
