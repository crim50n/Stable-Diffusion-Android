package dev.minios.pdaiv1.presentation.screen.logger

import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.common.log.FileLoggingTree
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import java.io.File

class LoggerViewModel(
    dispatchersProvider: DispatchersProvider,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val mainRouter: MainRouter,
) : MviRxViewModel<LoggerState, LoggerIntent, LoggerEffect>() {

    override val initialState = LoggerState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        readLogs()
    }

    override fun processIntent(intent: LoggerIntent) {
        when (intent) {
            LoggerIntent.ReadLogs -> readLogs()
            LoggerIntent.CopyLogs -> emitEffect(LoggerEffect.CopyToClipboard(state.value.text))
            LoggerIntent.ShareLogs -> emitEffect(LoggerEffect.ShareLog(state.value.text))
            LoggerIntent.NavigateBack -> mainRouter.navigateBack()
        }
    }

    private fun readLogs() {
        updateState { it.copy(loading = true, text = "") }
        try {
            val logFile = File(
                fileProviderDescriptor.logsCacheDirPath +
                        "/" +
                        FileLoggingTree.LOGGER_FILENAME
            )
            val content = logFile.readText()
            updateState {
                it.copy(
                    loading = false,
                    text = content,
                )
            }
        } catch (e: Exception) {
            updateState { it.copy(loading = false) }
        }
    }
}
