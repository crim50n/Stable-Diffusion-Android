package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.network.model.ForgeModuleRaw

fun List<ForgeModuleRaw>.mapRawToDomain(): List<ForgeModule> = map { it.mapRawToDomain() }

fun ForgeModuleRaw.mapRawToDomain(): ForgeModule = ForgeModule(
    name = modelName ?: "",
    path = filename ?: "",
)
