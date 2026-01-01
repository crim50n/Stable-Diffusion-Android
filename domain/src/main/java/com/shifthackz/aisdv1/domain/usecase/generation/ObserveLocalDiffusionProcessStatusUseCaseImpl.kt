package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.QnnGenerationRepository
import io.reactivex.rxjava3.core.Observable

internal class ObserveLocalDiffusionProcessStatusUseCaseImpl(
    private val localDiffusionGenerationRepository: LocalDiffusionGenerationRepository,
    private val qnnGenerationRepository: QnnGenerationRepository,
) : ObserveLocalDiffusionProcessStatusUseCase {

    override fun invoke() = Observable.merge(
        localDiffusionGenerationRepository.observeStatus(),
        qnnGenerationRepository.observeStatus()
    ).distinctUntilChanged()
}
