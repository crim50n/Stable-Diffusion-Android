package com.shifthackz.aisdv1.domain.usecase.forgemodule

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shifthackz.aisdv1.domain.mocks.mockForgeModules
import com.shifthackz.aisdv1.domain.repository.ForgeModulesRepository
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class GetForgeModulesUseCaseImplTest {

    private val stubException = Throwable("Failed to fetch modules.")
    private val stubRepository = mock<ForgeModulesRepository>()

    private val useCase = GetForgeModulesUseCaseImpl(
        repository = stubRepository,
    )

    @Test
    fun `given attempt to get modules, repository returns data, expected valid list value`() {
        whenever(stubRepository.fetchModules())
            .thenReturn(Single.just(mockForgeModules))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(mockForgeModules)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get modules, repository returns empty list, expected empty list value`() {
        whenever(stubRepository.fetchModules())
            .thenReturn(Single.just(emptyList()))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get modules, repository throws exception, expected error value`() {
        whenever(stubRepository.fetchModules())
            .thenReturn(Single.error(stubException))

        useCase()
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
