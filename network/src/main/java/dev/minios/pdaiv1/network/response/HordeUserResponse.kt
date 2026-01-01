package dev.minios.pdaiv1.network.response

import com.google.gson.annotations.SerializedName

data class HordeUserResponse(
    @SerializedName("id")
    val id: Int?,
)
