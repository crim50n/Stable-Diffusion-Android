package com.shifthackz.aisdv1.network.model

import com.google.gson.annotations.SerializedName

data class ExtensionRaw(
    @SerializedName("name")
    val name: String,
    @SerializedName("enabled")
    val enabled: Boolean,
)
