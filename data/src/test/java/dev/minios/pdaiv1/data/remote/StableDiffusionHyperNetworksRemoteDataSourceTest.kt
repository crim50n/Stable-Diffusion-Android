package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mocks.mockStableDiffusionHyperNetworkRaw
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.entity.StableDiffusionHyperNetwork
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.model.StableDiffusionHyperNetworkRaw
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test

class StableDiffusionHyperNetworksRemoteDataSourceTest {

    private val stubException = Throwable("Internal server error.")
    private val stubUrlProvider = mockk<ServerUrlProvider>()
    private val stubApi = mockk<Automatic1111RestApi>()

    private val remoteDataSource = StableDiffusionHyperNetworksRemoteDataSource(
        serverUrlProvider = stubUrlProvider,
        api = stubApi,
    )

    @Before
    fun initialize() {
        every {
            stubUrlProvider(any())
        } returns Single.just("http://192.168.0.1:7860")
    }

    @Test
    fun `given attempt to fetch hyper networks, api returns success response, expected valid hyper networks list value`() {
        every {
            stubApi.fetchHyperNetworks(any())
        } returns Single.just(mockStableDiffusionHyperNetworkRaw)

        remoteDataSource
            .fetchHyperNetworks()
            .test()
            .assertNoErrors()
            .assertValue { networks ->
                networks is List<StableDiffusionHyperNetwork>
                        && networks.size == mockStableDiffusionHyperNetworkRaw.size
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to fetch hyper networks, api returns empty response, expected empty hyper networks list value`() {
        every {
            stubApi.fetchHyperNetworks(any())
        } returns Single.just(emptyList())

        remoteDataSource
            .fetchHyperNetworks()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to fetch hyper networks, api returns error response, expected error value`() {
        every {
            stubApi.fetchHyperNetworks(any())
        } returns Single.error(stubException)

        remoteDataSource
            .fetchHyperNetworks()
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
