package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName

data class StabilityAiErrorResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("message")
    val message: String?,
)
