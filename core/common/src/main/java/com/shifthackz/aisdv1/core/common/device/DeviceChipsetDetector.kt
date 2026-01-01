package com.shifthackz.aisdv1.core.common.device

import android.os.Build

/**
 * Utility for detecting Qualcomm Snapdragon chipset and determining
 * appropriate QNN model variant suffix.
 *
 * QNN models come in three variants:
 * - 8gen2: For flagship chips (Snapdragon 8 Gen 2, 3, 4)
 * - 8gen1: For older flagship chips (Snapdragon 8 Gen 1)
 * - min: For other Snapdragon chips or unknown devices
 */
object DeviceChipsetDetector {

    /**
     * Chipset model suffixes for QNN model compatibility.
     * Maps SOC_MODEL to appropriate model suffix.
     */
    private val chipsetModelSuffixes = mapOf(
        // Snapdragon 8 Gen 1 family
        "SM8450" to "8gen1",   // Snapdragon 8 Gen 1
        "SM8475" to "8gen1",   // Snapdragon 8+ Gen 1

        // Snapdragon 8 Gen 2+ family (all use 8gen2 suffix)
        "SM8550" to "8gen2",   // Snapdragon 8 Gen 2
        "SM8550P" to "8gen2",
        "QCS8550" to "8gen2",
        "QCM8550" to "8gen2",
        "SM8650" to "8gen2",   // Snapdragon 8 Gen 3
        "SM8650P" to "8gen2",
        "SM8750" to "8gen2",   // Snapdragon 8 Gen 4
        "SM8750P" to "8gen2",
        "SM8850" to "8gen2",   // Future chips
        "SM8850P" to "8gen2",
        "SM8735" to "8gen2",
        "SM8845" to "8gen2",
    )

    /**
     * Get the device SOC model name.
     * Returns empty string on older Android versions (< S).
     */
    fun getDeviceSoc(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            ""
        }
    }

    /**
     * Get the chipset suffix for QNN model selection.
     *
     * @param soc The SOC model string (usually from Build.SOC_MODEL)
     * @return One of: "8gen2", "8gen1", or "min"
     */
    fun getChipsetSuffix(soc: String = getDeviceSoc()): String {
        // Direct match in our known chipsets
        chipsetModelSuffixes[soc]?.let { return it }

        // For unknown SM* chips, default to "min" for safety
        if (soc.startsWith("SM")) {
            return "min"
        }

        // For completely unknown devices, use min
        return "min"
    }

    /**
     * Check if the current device has a known Qualcomm Snapdragon chipset.
     */
    fun isQualcommDevice(): Boolean {
        val soc = getDeviceSoc()
        return soc.startsWith("SM") || soc.startsWith("QCS") || soc.startsWith("QCM")
    }

    /**
     * Check if the current device supports 8gen2 QNN models.
     */
    fun supports8Gen2(): Boolean = getChipsetSuffix() == "8gen2"

    /**
     * Check if the current device supports 8gen1 QNN models.
     */
    fun supports8Gen1(): Boolean = getChipsetSuffix() == "8gen1"

    /**
     * Get recommended QNN model suffix for this device.
     */
    fun getRecommendedModelSuffix(): String = getChipsetSuffix()

    /**
     * Get display name for the chipset suffix.
     */
    fun getChipsetDisplayName(suffix: String = getChipsetSuffix()): String {
        return when (suffix) {
            "8gen2" -> "Snapdragon 8 Gen 2/3/4"
            "8gen1" -> "Snapdragon 8 Gen 1/+"
            "min" -> "Other Snapdragon"
            else -> "Unknown"
        }
    }
}
