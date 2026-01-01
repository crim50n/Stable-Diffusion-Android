package dev.minios.pdaiv1.presentation.screen.donate

import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.domain.entity.Supporter
import com.shifthackz.android.core.mvi.MviState

@Immutable
data class DonateState(
    val loading: Boolean = true,
    val supporters: List<Supporter> = emptyList(),
) : MviState
