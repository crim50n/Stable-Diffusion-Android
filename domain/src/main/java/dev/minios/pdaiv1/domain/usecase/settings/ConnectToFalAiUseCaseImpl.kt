package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.entity.Configuration
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.feature.auth.AuthorizationCredentials
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.connectivity.TestFalAiApiKeyUseCase
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

internal class ConnectToFalAiUseCaseImpl(
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase,
    private val testFalAiApiKeyUseCase: TestFalAiApiKeyUseCase,
    private val preferenceManager: PreferenceManager,
) : ConnectToFalAiUseCase {

    override fun invoke(apiKey: String, endpointId: String): Single<Result<Unit>> {
        var configuration: Configuration? = null
        return getConfigurationUseCase.invoke()
            .map { originalConfiguration ->
                configuration = originalConfiguration
                originalConfiguration.copy(
                    source = ServerSource.FAL_AI,
                    falAiApiKey = apiKey,
                    authCredentials = AuthorizationCredentials.None,
                )
            }
            .flatMapCompletable(setServerConfigurationUseCase::invoke)
            .doOnComplete { preferenceManager.falAiSelectedEndpointId = endpointId }
            .delay(3L, TimeUnit.SECONDS)
            .andThen(testFalAiApiKeyUseCase())
            .flatMap {
                if (it) Single.just(Result.success(Unit))
                else Single.error(IllegalStateException("Bad key"))
            }
            .onErrorResumeNext { t ->
                val rollback = configuration
                    ?.copy(authCredentials = AuthorizationCredentials.None)
                    ?: return@onErrorResumeNext Single.just(Result.failure(t))
                setServerConfigurationUseCase(rollback)
                    .andThen(Single.just(Result.failure(t)))
            }
    }
}
