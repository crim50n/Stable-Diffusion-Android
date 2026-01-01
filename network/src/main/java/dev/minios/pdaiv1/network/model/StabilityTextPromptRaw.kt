package dev.minios.pdaiv1.network.model

import com.google.gson.annotations.SerializedName

data class StabilityTextPromptRaw(
    @SerializedName("text")
    val text: String,
    @SerializedName("weight")
    val weight: Double,
)
