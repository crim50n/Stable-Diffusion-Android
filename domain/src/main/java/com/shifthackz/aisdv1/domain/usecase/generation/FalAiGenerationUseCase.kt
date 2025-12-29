package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.FalAiPayload
import io.reactivex.rxjava3.core.Single

interface FalAiGenerationUseCase {
    operator fun invoke(payload: FalAiPayload): Single<List<AiGenerationResult>>
}
