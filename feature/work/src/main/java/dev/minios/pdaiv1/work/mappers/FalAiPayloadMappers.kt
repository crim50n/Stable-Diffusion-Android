package dev.minios.pdaiv1.work.mappers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.minios.pdaiv1.domain.entity.FalAiPayload

private val gson = Gson()

internal fun FalAiPayload.toByteArray(): ByteArray {
    val json = gson.toJson(mapOf(
        "endpointId" to endpointId,
        "parameters" to parameters,
    ))
    return json.toByteArray(Charsets.UTF_8)
}

internal fun FalAiPayload.toFalAiPayloadByteArray(): ByteArray = toByteArray()

internal fun ByteArray.toFalAiPayload(): FalAiPayload? {
    return try {
        val json = toString(Charsets.UTF_8)
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        val map: Map<String, Any?> = gson.fromJson(json, type)

        val endpointId = map["endpointId"] as? String ?: return null
        @Suppress("UNCHECKED_CAST")
        val parameters = map["parameters"] as? Map<String, Any?> ?: emptyMap()

        FalAiPayload(
            endpointId = endpointId,
            parameters = parameters,
        )
    } catch (e: Exception) {
        null
    }
}
