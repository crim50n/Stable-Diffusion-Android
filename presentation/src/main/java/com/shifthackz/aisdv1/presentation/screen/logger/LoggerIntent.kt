package com.shifthackz.aisdv1.presentation.screen.logger

import com.shifthackz.android.core.mvi.MviIntent

sealed interface LoggerIntent : MviIntent {

    data object ReadLogs : LoggerIntent

    data object CopyLogs : LoggerIntent

    data object ShareLogs : LoggerIntent

    data object NavigateBack : LoggerIntent
}
