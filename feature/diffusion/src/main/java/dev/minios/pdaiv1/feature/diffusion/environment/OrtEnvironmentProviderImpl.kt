package dev.minios.pdaiv1.feature.diffusion.environment

import ai.onnxruntime.OrtEnvironment

internal class OrtEnvironmentProviderImpl : OrtEnvironmentProvider {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()

    override fun get(): OrtEnvironment {
        return environment
    }
}
