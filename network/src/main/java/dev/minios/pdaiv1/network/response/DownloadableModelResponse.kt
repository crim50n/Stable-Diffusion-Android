package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName

data class DownloadableModelResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("size")
    val size: String?,
    @SerializedName("sources")
    val sources: List<String>?,
    @SerializedName("metadata")
    val metadata: ModelMetadata? = null,
)

data class ModelMetadata(
    @SerializedName("chipset")
    val chipset: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("src")
    val src: String? = null,
)
