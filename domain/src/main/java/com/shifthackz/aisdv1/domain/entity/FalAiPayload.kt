package com.shifthackz.aisdv1.domain.entity

data class FalAiPayload(
    val endpointId: String,
    val parameters: Map<String, Any?>,
)
