package com.shifthackz.aisdv1.network.request

import com.google.gson.annotations.SerializedName

data class OverrideSettings(
    @SerializedName("forge_additional_modules")
    val forgeAdditionalModules: List<String>? = null,
)
