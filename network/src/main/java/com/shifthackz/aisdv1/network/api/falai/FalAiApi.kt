package com.shifthackz.aisdv1.network.api.falai

import com.shifthackz.aisdv1.network.request.FalAiTextToImageRequest
import com.shifthackz.aisdv1.network.response.FalAiQueueResponse
import com.shifthackz.aisdv1.network.response.FalAiQueueStatusResponse
import com.shifthackz.aisdv1.network.response.FalAiGenerationResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface FalAiApi {

    /**
     * Validate the API key by listing models.
     * This is a free endpoint that returns 200 OK for valid keys.
     */
    @GET("https://api.fal.ai/v1/models")
    fun listModels(@retrofit2.http.Query("limit") limit: Int = 1): Single<Any>

    /**
     * Submit a text-to-image request to the fal.ai queue.
     * Uses full URL to avoid path encoding issues with slashes.
     */
    @POST
    fun submitToQueue(
        @Url url: String,
        @Body request: FalAiTextToImageRequest,
    ): Single<FalAiQueueResponse>

    /**
     * Submit a dynamic request to the fal.ai queue.
     * Supports any endpoint with dynamic parameters based on OpenAPI schema.
     */
    @POST
    fun submitDynamicToQueue(
        @Url url: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>,
    ): Single<FalAiQueueResponse>

    /**
     * Check the status of a queued request.
     * Uses the status_url returned from submitToQueue.
     */
    @GET
    fun checkStatus(@Url statusUrl: String): Single<FalAiQueueStatusResponse>

    /**
     * Fetch the result of a completed request.
     * Uses the response_url returned from submitToQueue.
     */
    @GET
    fun fetchResult(@Url resultUrl: String): Single<FalAiGenerationResponse>
}
