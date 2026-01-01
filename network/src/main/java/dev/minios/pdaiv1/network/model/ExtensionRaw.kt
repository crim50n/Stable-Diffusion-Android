package dev.minios.pdaiv1.network.model

import com.google.gson.annotations.SerializedName

data class ExtensionRaw(
    @SerializedName("name")
    val name: String,
    @SerializedName("enabled")
    val enabled: Boolean,
)
