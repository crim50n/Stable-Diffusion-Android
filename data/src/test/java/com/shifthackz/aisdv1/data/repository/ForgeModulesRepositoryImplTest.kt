package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.provider.ServerUrlProvider
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111RestApi
import com.shifthackz.aisdv1.network.model.ForgeModuleRaw
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test

class ForgeModulesRepositoryImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubServerUrlProvider = mockk<ServerUrlProvider>()
    private val stubApi = mockk<Automatic1111RestApi>()

    private val stubModulesRaw = listOf(
        ForgeModuleRaw(modelName = "ADetailer", filename = "extensions/adetailer"),
        ForgeModuleRaw(modelName = "ControlNet", filename = "extensions/controlnet"),
    )

    private val expectedModules = listOf(
        ForgeModule(name = "ADetailer", path = "extensions/adetailer"),
        ForgeModule(name = "ControlNet", path = "extensions/controlnet"),
    )

    private val repository = ForgeModulesRepositoryImpl(
        serverUrlProvider = stubServerUrlProvider,
        api = stubApi,
    )

    @Before
    fun initialize() {
        every {
            stubServerUrlProvider(any())
        } returns Single.just("http://192.168.0.1:7860/sdapi/v1/extensions")
    }

    @Test
    fun `given attempt to fetch modules, api returns data, expected valid modules list value`() {
        every {
            stubApi.fetchForgeModules(any())
        } returns Single.just(stubModulesRaw)

        repository
            .fetchModules()
            .test()
            .assertNoErrors()
            .assertValue(expectedModules)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to fetch modules, api returns empty list, expected empty list value`() {
        every {
            stubApi.fetchForgeModules(any())
        } returns Single.just(emptyList())

        repository
            .fetchModules()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to fetch modules, api throws exception, expected empty list value due to onErrorReturn`() {
        every {
            stubApi.fetchForgeModules(any())
        } returns Single.error(stubException)

        repository
            .fetchModules()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to fetch modules, url provider fails, expected empty list value due to onErrorReturn`() {
        every {
            stubServerUrlProvider(any())
        } returns Single.error(stubException)

        repository
            .fetchModules()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }
}
