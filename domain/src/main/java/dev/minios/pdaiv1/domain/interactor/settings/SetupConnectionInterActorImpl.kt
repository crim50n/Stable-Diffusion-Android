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

internal data class SetupConnectionInterActorImpl(
    override val connectToHorde: ConnectToHordeUseCase,
    override val connectToLocal: ConnectToLocalDiffusionUseCase,
    override val connectToMediaPipe: ConnectToMediaPipeUseCase,
    override val connectToQnn: ConnectToQnnUseCase,
    override val connectToA1111: ConnectToA1111UseCase,
    override val connectToHuggingFace: ConnectToHuggingFaceUseCase,
    override val connectToOpenAi: ConnectToOpenAiUseCase,
    override val connectToStabilityAi: ConnectToStabilityAiUseCase,
    override val connectToSwarmUi: ConnectToSwarmUiUseCase,
    override val connectToFalAi: ConnectToFalAiUseCase,
) : SetupConnectionInterActor
