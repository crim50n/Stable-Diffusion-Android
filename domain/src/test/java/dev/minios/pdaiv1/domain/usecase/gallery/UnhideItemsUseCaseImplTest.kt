package dev.minios.pdaiv1.domain.usecase.gallery

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Completable
import org.junit.Test

class UnhideItemsUseCaseImplTest {

    private val stubRepository = mock<GenerationResultRepository>()

    private val useCase = UnhideItemsUseCaseImpl(stubRepository)

    @Test
    fun `given repository unhid items successfully, expected complete`() {
        whenever(stubRepository.unhideByIds(listOf(5598L, 151297L)))
            .thenReturn(Completable.complete())

        useCase(listOf(5598L, 151297L))
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository unhid items with fail, expected error`() {
        val stubException = Throwable("Database communication error.")

        whenever(stubRepository.unhideByIds(listOf(5598L, 151297L)))
            .thenReturn(Completable.error(stubException))

        useCase(listOf(5598L, 151297L))
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
