package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName
import dev.minios.pdaiv1.network.model.OpenAiImageRaw

data class OpenAiResponse(
    @SerializedName("created")
    val created: Long?,
    @SerializedName("data")
    val data: List<OpenAiImageRaw>?
)
