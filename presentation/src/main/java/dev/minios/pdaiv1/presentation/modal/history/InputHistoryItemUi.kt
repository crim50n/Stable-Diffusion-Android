package dev.minios.pdaiv1.presentation.modal.history

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.domain.entity.AiGenerationResult

@Immutable
data class InputHistoryItemUi(
    val generationResult: AiGenerationResult,
    val bitmap: Bitmap,
)
