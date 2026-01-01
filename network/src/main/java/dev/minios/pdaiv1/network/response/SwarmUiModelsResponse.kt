package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName
import dev.minios.pdaiv1.network.model.SwarmUiModelRaw

data class SwarmUiModelsResponse(
    @SerializedName("files")
    val files: List<SwarmUiModelRaw>?,
)
