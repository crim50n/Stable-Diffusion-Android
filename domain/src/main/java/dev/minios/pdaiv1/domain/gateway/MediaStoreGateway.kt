package dev.minios.pdaiv1.domain.gateway

import dev.minios.pdaiv1.domain.entity.MediaStoreInfo

interface MediaStoreGateway {
    fun exportToFile(fileName: String, content: ByteArray)
    fun getInfo(): MediaStoreInfo
}
