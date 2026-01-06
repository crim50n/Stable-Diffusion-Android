package dev.minios.pdaiv1.domain.gateway

import dev.minios.pdaiv1.domain.entity.MediaStoreInfo
import java.io.File

interface MediaStoreGateway {
    fun exportToFile(fileName: String, content: ByteArray)
    fun exportFromFile(fileName: String, sourceFile: File)
    fun getInfo(): MediaStoreInfo
}
