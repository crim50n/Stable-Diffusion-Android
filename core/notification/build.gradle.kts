plugins {
    alias(libs.plugins.generic.library)
}

android {
    namespace = "dev.minios.pdaiv1.core.notification"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.androidx.core)
}
