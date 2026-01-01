package dev.minios.pdaiv1.data.mappers

import dev.minios.pdaiv1.domain.entity.ForgeModule
import dev.minios.pdaiv1.network.model.ForgeModuleRaw

fun List<ForgeModuleRaw>.mapRawToDomain(): List<ForgeModule> = map { it.mapRawToDomain() }

fun ForgeModuleRaw.mapRawToDomain(): ForgeModule = ForgeModule(
    name = modelName ?: "",
    path = filename ?: "",
)
