package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mocks.mockSupporters
import dev.minios.pdaiv1.data.mocks.mockSupportersRaw
import dev.minios.pdaiv1.network.api.pdai.DonateApi
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class SupportersRemoteDataSourceTest {

    private val stubException = Throwable("Bad response.")
    private val stubApi = mockk<DonateApi>()

    private val remoteDataSource = SupportersRemoteDataSource(stubApi)

    @Test
    fun `given api returns supporters, expected valid domain models list value`() {
        every {
            stubApi.fetchSupporters()
        } returns Single.just(mockSupportersRaw)

        remoteDataSource
            .fetch()
            .test()
            .assertNoErrors()
            .await()
            .assertValue { it.size == mockSupporters.size }
            .assertComplete()
    }

    @Test
    fun `given api returns empty list, expected empty domain models list value`() {
        every {
            stubApi.fetchSupporters()
        } returns Single.just(emptyList())

        remoteDataSource
            .fetch()
            .test()
            .assertNoErrors()
            .await()
            .assertValue(emptyList())
            .assertComplete()
    }

    @Test
    fun `given api returns error, expected error value`() {
        every {
            stubApi.fetchSupporters()
        } returns Single.error(stubException)

        remoteDataSource
            .fetch()
            .test()
            .assertError(stubException)
            .await()
            .assertNoValues()
            .assertNotComplete()
    }
}
