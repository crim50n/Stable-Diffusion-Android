package dev.minios.pdaiv1.domain.entity

data class LocalAiModel(
    val id: String,
    val type: Type,
    val name: String,
    val size: String,
    val sources: List<String>,
    val downloaded: Boolean = false,
    val selected: Boolean = false,
    val runOnCpu: Boolean = false,
    val chipsetSuffix: String? = null, // "8gen1", "8gen2", "min", or null for all chips
) {
    enum class Type(val key: String) {
        ONNX("onnx"),
        MediaPipe("mediapipe"),
        QNN("qnn");

        companion object {
            fun parse(value: String?) = entries.find { it.key == value } ?: ONNX
        }
    }

    companion object {
        val CustomOnnx = LocalAiModel(
            id = "CUSTOM",
            type = Type.ONNX,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )

        val CustomMediaPipe = LocalAiModel(
            id = "CUSTOM_MP",
            type = Type.MediaPipe,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )

        val CustomQnn = LocalAiModel(
            id = "CUSTOM_QNN",
            type = Type.QNN,
            name = "Custom",
            size = "NaN",
            sources = emptyList(),
        )
    }
}
