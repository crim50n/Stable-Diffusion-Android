package dev.minios.pdaiv1.domain.entity

import java.io.Serializable

data class ForgeModule(
    val name: String,
    val path: String,
) : Serializable
