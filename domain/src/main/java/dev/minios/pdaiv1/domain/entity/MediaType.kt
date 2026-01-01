package dev.minios.pdaiv1.domain.entity

enum class MediaType(val key: String, val extension: String, val mimeType: String) {
    IMAGE("IMAGE", "png", "image/png"),
    VIDEO("VIDEO", "mp4", "video/mp4");

    companion object {
        fun parse(key: String): MediaType = entries.find { it.key == key } ?: IMAGE

        fun fromExtension(ext: String): MediaType = when (ext.lowercase()) {
            "mp4", "webm", "mov" -> VIDEO
            else -> IMAGE
        }

        fun fromUrl(url: String): MediaType {
            val ext = url.substringAfterLast('.').substringBefore('?').lowercase()
            return fromExtension(ext)
        }
    }
}
