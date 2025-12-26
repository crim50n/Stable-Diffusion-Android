package com.shifthackz.aisdv1.network.model

import com.google.gson.annotations.SerializedName

data class ForgeModuleRaw(
    @SerializedName("model_name")
    val modelName: String?,
    @SerializedName("filename")
    val filename: String?,
)
