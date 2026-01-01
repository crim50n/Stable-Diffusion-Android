package dev.minios.pdaiv1.domain.usecase.huggingface

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.mocks.mockHuggingFaceModels
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.HuggingFaceModelsRepository
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test

class FetchAndGetHuggingFaceModelsUseCaseImplTest {

    private val stubPreferenceManager = mock<PreferenceManager>()
    private val stubRepository = mock<HuggingFaceModelsRepository>()

    private val useCase = FetchAndGetHuggingFaceModelsUseCaseImpl(
        preferenceManager = stubPreferenceManager,
        huggingFaceModelsRepository = stubRepository,
    )

    @Before
    fun initialize() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HUGGING_FACE)
    }

    @Test
    fun `given repository provided models list, expected valid list value`() {
        whenever(stubRepository.fetchAndGetHuggingFaceModels())
            .thenReturn(Single.just(mockHuggingFaceModels))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(mockHuggingFaceModels)
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository provided empty models list, expected empty list value`() {
        whenever(stubRepository.fetchAndGetHuggingFaceModels())
            .thenReturn(Single.just(emptyList()))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository thrown exception, expected error value`() {
        val stubException = Throwable("Unknown error occurred.")

        whenever(stubRepository.fetchAndGetHuggingFaceModels())
            .thenReturn(Single.error(stubException))

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
