package com.shifthackz.aisdv1.presentation.model

enum class AspectRatio(
    val widthRatio: Int,
    val heightRatio: Int,
    val label: String,
) {
    RATIO_1_1(1, 1, "1:1"),
    RATIO_4_3(4, 3, "4:3"),
    RATIO_3_4(3, 4, "3:4"),
    RATIO_16_9(16, 9, "16:9"),
    RATIO_9_16(9, 16, "9:16"),
    RATIO_3_2(3, 2, "3:2"),
    RATIO_2_3(2, 3, "2:3"),
    RATIO_21_9(21, 9, "21:9"),
    RATIO_9_21(9, 21, "9:21");

    fun calculateDimensions(baseSize: Int): Pair<Int, Int> {
        val gcd = gcd(widthRatio, heightRatio)
        val normalizedWidth = widthRatio / gcd
        val normalizedHeight = heightRatio / gcd

        return if (normalizedWidth >= normalizedHeight) {
            val width = baseSize
            val height = (baseSize * normalizedHeight) / normalizedWidth
            // Round to nearest 8 for better compatibility
            Pair(roundTo8(width), roundTo8(height))
        } else {
            val height = baseSize
            val width = (baseSize * normalizedWidth) / normalizedHeight
            Pair(roundTo8(width), roundTo8(height))
        }
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private fun roundTo8(value: Int): Int = ((value + 4) / 8) * 8

    override fun toString(): String = label
}
