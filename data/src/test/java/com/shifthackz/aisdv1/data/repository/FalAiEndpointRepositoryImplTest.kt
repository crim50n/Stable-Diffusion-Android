package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mocks.mockFalAiEndpoint
import com.shifthackz.aisdv1.data.mocks.mockFalAiEndpoints
import com.shifthackz.aisdv1.domain.datasource.FalAiEndpointDataSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class FalAiEndpointRepositoryImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubBuiltInDataSource = mockk<FalAiEndpointDataSource.BuiltIn>()
    private val stubRemoteDataSource = mockk<FalAiEndpointDataSource.Remote>()
    private val stubLocalDataSource = mockk<FalAiEndpointDataSource.Local>()
    private val stubPreferenceManager = mockk<PreferenceManager>(relaxed = true)

    private val repository = FalAiEndpointRepositoryImpl(
        builtInDataSource = stubBuiltInDataSource,
        remoteDataSource = stubRemoteDataSource,
        localDataSource = stubLocalDataSource,
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given attempt to observe all endpoints, both sources return data, expected combined list`() {
        val customEndpoint = mockFalAiEndpoint.copy(id = "custom-endpoint", isCustom = true)

        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        every {
            stubLocalDataSource.observeAll()
        } returns Observable.just(listOf(customEndpoint))

        repository
            .observeAll()
            .test()
            .assertNoErrors()
            .assertValueAt(1) { it.size == 2 && it.containsAll(mockFalAiEndpoints + customEndpoint) }
            .dispose()
    }

    @Test
    fun `given attempt to get all endpoints, both sources return data, expected combined list`() {
        val customEndpoint = mockFalAiEndpoint.copy(id = "custom-endpoint", isCustom = true)

        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        every {
            stubLocalDataSource.getAll()
        } returns Single.just(listOf(customEndpoint))

        repository
            .getAll()
            .test()
            .assertNoErrors()
            .assertValue { it.size == 2 && it.containsAll(mockFalAiEndpoints + customEndpoint) }
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get all endpoints, local source fails, expected built-in endpoints only`() {
        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        every {
            stubLocalDataSource.getAll()
        } returns Single.error(stubException)

        repository
            .getAll()
            .test()
            .assertNoErrors()
            .assertValue(mockFalAiEndpoints)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get endpoint by id, endpoint exists, expected valid endpoint`() {
        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        every {
            stubLocalDataSource.getAll()
        } returns Single.just(emptyList())

        repository
            .getById(mockFalAiEndpoint.id)
            .test()
            .assertNoErrors()
            .assertValue(mockFalAiEndpoint)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get endpoint by id, endpoint not found, expected error`() {
        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        every {
            stubLocalDataSource.getAll()
        } returns Single.just(emptyList())

        repository
            .getById("non-existent-id")
            .test()
            .assertError { it is NoSuchElementException }
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to get selected endpoint, preference has valid id, expected valid endpoint`() {
        every {
            stubPreferenceManager.falAiSelectedEndpointId
        } returns mockFalAiEndpoint.id

        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        every {
            stubLocalDataSource.getAll()
        } returns Single.just(emptyList())

        repository
            .getSelected()
            .test()
            .assertNoErrors()
            .assertValue(mockFalAiEndpoint)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get selected endpoint, preference is blank, expected first built-in endpoint`() {
        every {
            stubPreferenceManager.falAiSelectedEndpointId
        } returns ""

        every {
            stubBuiltInDataSource.getAll()
        } returns Single.just(mockFalAiEndpoints)

        repository
            .getSelected()
            .test()
            .assertNoErrors()
            .assertValue(mockFalAiEndpoints.first())
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to set selected endpoint, expected preference updated`() {
        every {
            stubPreferenceManager.falAiSelectedEndpointId = any()
        } returns Unit

        repository
            .setSelected("new-endpoint-id")
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify { stubPreferenceManager.falAiSelectedEndpointId = "new-endpoint-id" }
    }

    @Test
    fun `given attempt to import from url, remote returns endpoint, expected endpoint saved and returned`() {
        val importedEndpoint = mockFalAiEndpoint.copy(id = "imported", isCustom = true)

        every {
            stubRemoteDataSource.fetchFromUrl(any())
        } returns Single.just(importedEndpoint)

        every {
            stubLocalDataSource.save(any())
        } returns Completable.complete()

        repository
            .importFromUrl("https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=test")
            .test()
            .assertNoErrors()
            .assertValue(importedEndpoint)
            .await()
            .assertComplete()

        verify { stubLocalDataSource.save(importedEndpoint) }
    }

    @Test
    fun `given attempt to import from url, remote fails, expected error`() {
        every {
            stubRemoteDataSource.fetchFromUrl(any())
        } returns Single.error(stubException)

        repository
            .importFromUrl("https://fal.ai/api/invalid")
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to delete endpoint, expected delete called on local source`() {
        every {
            stubLocalDataSource.delete(any())
        } returns Completable.complete()

        repository
            .delete("endpoint-to-delete")
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify { stubLocalDataSource.delete("endpoint-to-delete") }
    }

    @Test
    fun `given attempt to delete endpoint, local source fails, expected error`() {
        every {
            stubLocalDataSource.delete(any())
        } returns Completable.error(stubException)

        repository
            .delete("endpoint-to-delete")
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
