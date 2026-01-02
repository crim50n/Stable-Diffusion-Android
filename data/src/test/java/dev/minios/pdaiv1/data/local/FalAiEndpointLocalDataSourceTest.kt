package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mocks.mockFalAiEndpoint
import dev.minios.pdaiv1.data.mocks.mockFalAiEndpoints
import dev.minios.pdaiv1.storage.db.persistent.dao.FalAiEndpointDao
import dev.minios.pdaiv1.storage.db.persistent.entity.FalAiEndpointEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class FalAiEndpointLocalDataSourceTest {

    private val stubException = Throwable("Database error")
    private val stubDao = mockk<FalAiEndpointDao>()

    private val localDataSource = FalAiEndpointLocalDataSource(stubDao)

    private val stubEntity = FalAiEndpointEntity(
        id = "fal-ai/flux/schnell",
        endpointId = "fal-ai/flux/schnell",
        title = "FLUX.1 [schnell]",
        description = "Fast text to image generation",
        category = "TEXT_TO_IMAGE",
        group = "FLUX",
        thumbnailUrl = "https://fal.ai/thumbnails/flux-schnell.jpg",
        playgroundUrl = "https://fal.ai/models/fal-ai/flux/schnell",
        documentationUrl = "https://fal.ai/models/fal-ai/flux/schnell/api",
        isCustom = true,
        schemaJson = """{"baseUrl":"https://queue.fal.run","submissionPath":"/fal-ai/flux/schnell","inputProperties":[{"name":"prompt","title":"Prompt","description":"The prompt","type":"STRING","default":"","minimum":null,"maximum":null,"enumValues":null,"isRequired":true,"isImageInput":false}],"requiredProperties":["prompt"],"propertyOrder":["prompt"]}""",
    )

    @Test
    fun `given attempt to observe all, dao returns entities, expected domain models`() {
        every {
            stubDao.observeAll()
        } returns Flowable.just(listOf(stubEntity))

        localDataSource
            .observeAll()
            .test()
            .assertNoErrors()
            .assertValue { endpoints -> endpoints.size == 1 }
    }

    @Test
    fun `given attempt to observe all, dao returns empty list, expected empty domain list`() {
        every {
            stubDao.observeAll()
        } returns Flowable.just(emptyList())

        localDataSource
            .observeAll()
            .test()
            .assertNoErrors()
            .assertValue { endpoints -> endpoints.isEmpty() }
    }

    @Test
    fun `given attempt to get all, dao returns entities, expected domain models`() {
        every {
            stubDao.queryAll()
        } returns Single.just(listOf(stubEntity))

        localDataSource
            .getAll()
            .test()
            .assertNoErrors()
            .assertValue { endpoints -> endpoints.size == 1 }
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get all, dao throws exception, expected error value`() {
        every {
            stubDao.queryAll()
        } returns Single.error(stubException)

        localDataSource
            .getAll()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to get by id, dao returns entity, expected domain model`() {
        val id = "fal-ai/flux/schnell"

        every {
            stubDao.queryById(id)
        } returns Single.just(stubEntity)

        localDataSource
            .getById(id)
            .test()
            .assertNoErrors()
            .assertValue { endpoint -> endpoint.id == id }
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to get by id, dao throws exception, expected error value`() {
        val id = "nonexistent-id"

        every {
            stubDao.queryById(id)
        } returns Single.error(stubException)

        localDataSource
            .getById(id)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to save endpoint, dao insert success, expected complete value`() {
        every {
            stubDao.insert(any())
        } returns Completable.complete()

        localDataSource
            .save(mockFalAiEndpoint)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify { stubDao.insert(any()) }
    }

    @Test
    fun `given attempt to save endpoint, dao insert fails, expected error value`() {
        every {
            stubDao.insert(any())
        } returns Completable.error(stubException)

        localDataSource
            .save(mockFalAiEndpoint)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to delete endpoint, dao delete success, expected complete value`() {
        val id = "fal-ai/flux/schnell"

        every {
            stubDao.deleteById(id)
        } returns Completable.complete()

        localDataSource
            .delete(id)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify { stubDao.deleteById(id) }
    }

    @Test
    fun `given attempt to delete endpoint, dao delete fails, expected error value`() {
        val id = "nonexistent-id"

        every {
            stubDao.deleteById(id)
        } returns Completable.error(stubException)

        localDataSource
            .delete(id)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
