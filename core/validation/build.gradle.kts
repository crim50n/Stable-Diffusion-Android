plugins {
    alias(libs.plugins.generic.library)
}

android {
    namespace = "dev.minios.pdaiv1.core.validation"
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.rx.kotlin)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
}
