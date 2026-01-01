import com.android.build.api.dsl.LibraryExtension
import dev.minios.pdaiv1.buildlogic.configureCompose
import dev.minios.pdaiv1.buildlogic.configureKotlinAndroid
import dev.minios.pdaiv1.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class ComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("generic.jacoco")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            val extension = extensions.getByType<LibraryExtension>()
            configureCompose(extension)

            extensions.configure<com.android.build.gradle.LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
            }
        }
    }
}
