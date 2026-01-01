package dev.minios.pdaiv1.feature.diffusion.environment

import ai.onnxruntime.OrtEnvironment

internal fun interface OrtEnvironmentProvider {
    fun get(): OrtEnvironment
}
