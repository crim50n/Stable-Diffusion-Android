plugins {
    alias(libs.plugins.generic.library)
}

android {
    namespace = "dev.minios.pdaiv1.core.common"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.koin.core)
    implementation(libs.rx.java)
    implementation(libs.timber)
    testImplementation(libs.test.junit)
}
