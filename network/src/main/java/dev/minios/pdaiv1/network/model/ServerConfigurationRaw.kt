package dev.minios.pdaiv1.network.model

import com.google.gson.annotations.SerializedName

data class ServerConfigurationRaw(
    @SerializedName("sd_model_checkpoint")
    val sdModelCheckpoint: String?,
)
