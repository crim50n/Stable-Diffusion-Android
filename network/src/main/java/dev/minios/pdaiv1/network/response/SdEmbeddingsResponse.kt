package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName

data class SdEmbeddingsResponse(
    @SerializedName("loaded")
    val loaded: Map<String, Any?>?,
)
