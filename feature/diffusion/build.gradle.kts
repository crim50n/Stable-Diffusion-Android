plugins {
    alias(libs.plugins.generic.library)
}

android {
    namespace = "dev.minios.pdaiv1.feature.diffusion"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(libs.koin.core)
    implementation(libs.rx.kotlin)
    implementation(libs.microsoft.onnx.runtime.android)
}
