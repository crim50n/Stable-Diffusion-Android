package com.shifthackz.aisdv1.domain.usecase.connectivity

import io.reactivex.rxjava3.core.Single

interface TestFalAiApiKeyUseCase {
    operator fun invoke(): Single<Boolean>
}
