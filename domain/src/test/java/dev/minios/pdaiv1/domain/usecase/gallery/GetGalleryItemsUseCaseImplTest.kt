package dev.minios.pdaiv1.domain.usecase.gallery

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.mocks.mockAiGenerationResults
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class GetGalleryItemsUseCaseImplTest {

    private val stubRepository = mock<GenerationResultRepository>()

    private val useCase = GetGalleryItemsUseCaseImpl(stubRepository)

    @Test
    fun `given repository returns items, expected valid items list`() {
        val ids = listOf(1L, 2L, 3L)

        whenever(stubRepository.getByIds(ids))
            .thenReturn(Single.just(mockAiGenerationResults))

        useCase(ids)
            .test()
            .assertNoErrors()
            .assertValue(mockAiGenerationResults)
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository returns empty list, expected empty list`() {
        val ids = listOf(1L, 2L, 3L)

        whenever(stubRepository.getByIds(ids))
            .thenReturn(Single.just(emptyList()))

        useCase(ids)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given empty ids list, expected empty list from repository`() {
        val ids = emptyList<Long>()

        whenever(stubRepository.getByIds(ids))
            .thenReturn(Single.just(emptyList()))

        useCase(ids)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository throws error, expected error`() {
        val ids = listOf(1L, 2L, 3L)
        val stubException = Throwable("Database error")

        whenever(stubRepository.getByIds(ids))
            .thenReturn(Single.error(stubException))

        useCase(ids)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
