package dev.minios.pdaiv1.domain.interactor.settings

import dev.minios.pdaiv1.domain.usecase.settings.ConnectToA1111UseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToFalAiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToHordeUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToHuggingFaceUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToLocalDiffusionUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToMediaPipeUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToQnnUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToOpenAiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToStabilityAiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToSwarmUiUseCase

interface SetupConnectionInterActor {
    val connectToHorde: ConnectToHordeUseCase
    val connectToLocal: ConnectToLocalDiffusionUseCase
    val connectToMediaPipe: ConnectToMediaPipeUseCase
    val connectToQnn: ConnectToQnnUseCase
    val connectToA1111: ConnectToA1111UseCase
    val connectToHuggingFace: ConnectToHuggingFaceUseCase
    val connectToOpenAi: ConnectToOpenAiUseCase
    val connectToStabilityAi: ConnectToStabilityAiUseCase
    val connectToSwarmUi: ConnectToSwarmUiUseCase
    val connectToFalAi: ConnectToFalAiUseCase
}
