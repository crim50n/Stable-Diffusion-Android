package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.FalAiOpenApiParser
import dev.minios.pdaiv1.domain.datasource.FalAiEndpointDataSource
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Fetches fal.ai endpoint schemas from remote URLs.
 */
internal class FalAiEndpointRemoteDataSource(
    private val httpClient: OkHttpClient,
) : FalAiEndpointDataSource.Remote {

    override fun fetchFromUrl(url: String): Single<FalAiEndpoint> = Single.fromCallable {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to fetch OpenAPI schema: ${response.code}")
        }

        val json = response.body?.string()
            ?: throw IllegalStateException("Empty response body")

        FalAiOpenApiParser.parse(json, isCustom = true)
    }
}
