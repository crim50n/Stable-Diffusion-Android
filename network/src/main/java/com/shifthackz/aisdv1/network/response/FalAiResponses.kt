package com.shifthackz.aisdv1.network.response

import com.google.gson.annotations.SerializedName

/**
 * Response from submitting a request to the fal.ai queue.
 */
data class FalAiQueueResponse(
    @SerializedName("request_id")
    val requestId: String,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("response_url")
    val responseUrl: String? = null,

    @SerializedName("status_url")
    val statusUrl: String? = null,

    @SerializedName("cancel_url")
    val cancelUrl: String? = null,

    // If sync_mode=true or immediate response, images may be included directly
    @SerializedName("images")
    val images: List<FalAiImage>? = null,

    // For video generation endpoints
    @SerializedName("video")
    val video: FalAiImage? = null,

    // Seed can exceed Long.MAX_VALUE, so we use String
    @SerializedName("seed")
    val seed: String? = null,

    @SerializedName("prompt")
    val prompt: String? = null,
)

/**
 * Response when checking status of a queued request.
 */
data class FalAiQueueStatusResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("request_id")
    val requestId: String? = null,

    @SerializedName("response_url")
    val responseUrl: String? = null,

    @SerializedName("queue_position")
    val queuePosition: Int? = null,

    @SerializedName("logs")
    val logs: Any? = null,
)

/**
 * Final response with generated images or video.
 */
data class FalAiGenerationResponse(
    @SerializedName("images")
    val images: List<FalAiImage>? = null,

    @SerializedName("video")
    val video: FalAiImage? = null,

    // Seed can exceed Long.MAX_VALUE, so we use String
    @SerializedName("seed")
    val seed: String? = null,

    @SerializedName("prompt")
    val prompt: String? = null,

    @SerializedName("timings")
    val timings: Map<String, Double>? = null,

    @SerializedName("has_nsfw_concepts")
    val hasNsfwConcepts: List<Boolean>? = null,
)

data class FalAiImage(
    @SerializedName("url")
    val url: String,

    @SerializedName("width")
    val width: Int? = null,

    @SerializedName("height")
    val height: Int? = null,

    @SerializedName("content_type")
    val contentType: String? = null,
)
