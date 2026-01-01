package dev.minios.pdaiv1.domain.usecase.gallery

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class GetGalleryPagedIdsUseCaseImplTest {

    private val stubException = Throwable("Failed to get gallery ids.")
    private val stubRepository = mock<GenerationResultRepository>()

    private val useCase = GetGalleryPagedIdsUseCaseImpl(
        repository = stubRepository,
    )

    @Test
    fun `given attempt to get gallery ids, repository returns data, expected valid list value`() {
        val expectedIds = listOf(1L, 2L, 3L, 5598L)

        whenever(stubRepository.getAllIds())
            .thenReturn(Single.just(expectedIds))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(expectedIds)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get gallery ids, repository returns empty list, expected empty list value`() {
        whenever(stubRepository.getAllIds())
            .thenReturn(Single.just(emptyList()))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get gallery ids, repository throws exception, expected error value`() {
        whenever(stubRepository.getAllIds())
            .thenReturn(Single.error(stubException))

        useCase()
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
