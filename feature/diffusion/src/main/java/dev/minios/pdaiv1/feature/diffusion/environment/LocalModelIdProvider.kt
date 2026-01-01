package dev.minios.pdaiv1.feature.diffusion.environment

fun interface LocalModelIdProvider {
    fun get(): String
}
