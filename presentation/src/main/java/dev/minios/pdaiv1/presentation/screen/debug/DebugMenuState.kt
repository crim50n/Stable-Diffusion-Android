package dev.minios.pdaiv1.presentation.screen.debug

import dev.minios.pdaiv1.core.common.schedulers.SchedulersToken
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviState

data class DebugMenuState(
    val screenModal: Modal = Modal.None,
    val localDiffusionAllowCancel: Boolean = false,
    val localDiffusionSchedulerThread: SchedulersToken = SchedulersToken.COMPUTATION,
) : MviState
