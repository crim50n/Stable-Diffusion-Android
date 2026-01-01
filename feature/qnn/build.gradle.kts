plugins {
    alias(libs.plugins.generic.library)
    alias(libs.plugins.generic.flavors)
}

android {
    namespace = "com.shifthackz.aisdv1.feature.qnn"

    defaultConfig {
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    testOptions.unitTests.all { test ->
        test.jvmArgs(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
        )
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.rx.kotlin)
    implementation(libs.rx.java)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.google.gson)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.coroutines)
}
