package dev.minios.pdaiv1.data.remote

import io.mockk.every
import io.mockk.mockk
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class FalAiEndpointRemoteDataSourceTest {

    private val stubHttpClient = mockk<OkHttpClient>()
    private val stubCall = mockk<Call>()

    private val remoteDataSource = FalAiEndpointRemoteDataSource(stubHttpClient)

    @Test
    fun `given attempt to fetch from url, http returns error response, expected IllegalStateException`() {
        val url = "https://fal.ai/models/test-model/openapi.json"

        val errorResponse = Response.Builder()
            .request(Request.Builder().url(url).build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body("Not found".toResponseBody())
            .build()

        every { stubHttpClient.newCall(any()) } returns stubCall
        every { stubCall.execute() } returns errorResponse

        remoteDataSource
            .fetchFromUrl(url)
            .test()
            .assertError { error ->
                error is IllegalStateException &&
                error.message?.contains("Failed to fetch OpenAPI schema: 404") == true
            }
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to fetch from url, http returns empty body string, expected parse error`() {
        val url = "https://fal.ai/models/test-model/openapi.json"

        val emptyResponse = Response.Builder()
            .request(Request.Builder().url(url).build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody())
            .build()

        every { stubHttpClient.newCall(any()) } returns stubCall
        every { stubCall.execute() } returns emptyResponse

        remoteDataSource
            .fetchFromUrl(url)
            .test()
            .assertError { error -> error is Exception }
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to fetch from url, http call throws exception, expected error value`() {
        val url = "https://fal.ai/models/test-model/openapi.json"
        val stubException = RuntimeException("Network error")

        every { stubHttpClient.newCall(any()) } returns stubCall
        every { stubCall.execute() } throws stubException

        remoteDataSource
            .fetchFromUrl(url)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
