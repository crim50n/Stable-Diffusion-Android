package dev.minios.pdaiv1.domain.entity

data class StableDiffusionSampler(
    val name: String,
    val aliases: List<String>,
    val options: Map<String, String>,
)
