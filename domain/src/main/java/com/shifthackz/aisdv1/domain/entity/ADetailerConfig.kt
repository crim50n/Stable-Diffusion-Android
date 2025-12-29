package com.shifthackz.aisdv1.domain.entity

import java.io.Serializable

/**
 * Configuration for the ADetailer extension in A1111/Forge.
 * ADetailer automatically detects faces/hands and refines them with a second pass.
 *
 * @param enabled Whether ADetailer should be used for generation.
 * @param model The detection model to use (e.g., "face_yolov8n.pt", "hand_yolov8n.pt").
 * @param prompt Optional prompt override for the detected regions.
 * @param negativePrompt Optional negative prompt override for the detected regions.
 * @param confidence Detection confidence threshold (0.0 to 1.0).
 * @param maskBlur Blur amount for the detected mask.
 * @param denoisingStrength Denoising strength for the second pass (0.0 to 1.0).
 * @param inpaintOnlyMasked Whether to inpaint only the masked region.
 * @param inpaintPadding Padding around detected regions in pixels.
 */
data class ADetailerConfig(
    val enabled: Boolean = false,
    val model: String = "face_yolov8s.pt",
    val prompt: String = "",
    val negativePrompt: String = "",
    val confidence: Float = 0.3f,
    val maskBlur: Int = 4,
    val denoisingStrength: Float = 0.4f,
    val inpaintOnlyMasked: Boolean = true,
    val inpaintPadding: Int = 32,
) : Serializable {

    companion object {
        val DISABLED = ADetailerConfig(enabled = false)

        val AVAILABLE_MODELS = listOf(
            "face_yolov8n.pt",
            "face_yolov8s.pt",
            "hand_yolov8n.pt",
            "person_yolov8n-seg.pt",
            "person_yolov8s-seg.pt",
            "yolov8x-worldv2.pt",
            "mediapipe_face_full",
            "mediapipe_face_short",
            "mediapipe_face_mesh",
            "mediapipe_face_mesh_eyes_only",
        )
    }

    /**
     * Converts this config to the alwayson_scripts format expected by A1111 API.
     */
    fun toAlwaysOnScripts(): Map<String, Any>? {
        if (!enabled) return null

        return mapOf(
            "ADetailer" to mapOf(
                "args" to listOf(
                    true, // enable
                    false, // skip_img2img
                    mapOf(
                        "ad_model" to model,
                        "ad_prompt" to prompt,
                        "ad_negative_prompt" to negativePrompt,
                        "ad_confidence" to confidence,
                        "ad_dilate_erode" to maskBlur,
                        "ad_denoising_strength" to denoisingStrength,
                        "ad_inpaint_only_masked" to inpaintOnlyMasked,
                        "ad_inpaint_only_masked_padding" to inpaintPadding,
                        "is_api" to true,
                    )
                )
            )
        )
    }
}
