package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.reactivex.rxjava3.core.Single

internal class ConnectToQnnUseCaseImpl(
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase,
    private val preferenceManager: PreferenceManager,
) : ConnectToQnnUseCase {

    override fun invoke(modelId: String, runOnCpu: Boolean): Single<Result<Unit>> = getConfigurationUseCase()
        .map { originalConfiguration ->
            preferenceManager.localQnnRunOnCpu = runOnCpu
            originalConfiguration.copy(
                source = ServerSource.LOCAL_QUALCOMM_QNN,
                localQnnModelId = modelId,
            )
        }
        .flatMapCompletable(setServerConfigurationUseCase::invoke)
        .andThen(Single.just(Result.success(Unit)))
        .onErrorResumeNext { t -> Single.just(Result.failure(t)) }
}
