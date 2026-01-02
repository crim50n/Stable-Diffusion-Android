package dev.minios.pdaiv1.core.common.model

import java.io.Serializable

data class Octagonal<out A, out B, out C, out D, out E, out F, out G, out H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
) : Serializable {

    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth)"
}
