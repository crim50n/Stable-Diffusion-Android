package dev.minios.pdaiv1.presentation.utils

import android.content.Context
import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.common.log.FileLoggingTree
import dev.minios.pdaiv1.core.sharing.shareEmail
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class ReportProblemEmailComposer : KoinComponent {

    private val fileProviderDescriptor: FileProviderDescriptor by inject()
    private val buildInfoProvider: BuildInfoProvider by inject()

    fun invoke(context: Context) {
        val logFile = File(
            fileProviderDescriptor.logsCacheDirPath +
                    "/" +
                    FileLoggingTree.LOGGER_FILENAME
        )
        context.shareEmail(
            email = "crims0n@minios.dev",
            subject = "PDAI - Problem report",
            body = "PDAI : $buildInfoProvider",
            file = if (!logFile.exists()) null else logFile,
            fileProviderPath = fileProviderDescriptor.providerPath,
        )
    }
}
