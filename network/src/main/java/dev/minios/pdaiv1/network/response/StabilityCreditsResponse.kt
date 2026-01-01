package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName

data class StabilityCreditsResponse(
    @SerializedName("credits")
    val credits: Float?,
)
