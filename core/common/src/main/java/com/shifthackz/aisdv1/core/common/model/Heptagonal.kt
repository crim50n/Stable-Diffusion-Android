package com.shifthackz.aisdv1.core.common.model

import java.io.Serializable

data class Heptagonal<out A, out B, out C, out D, out E, out F, out G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
) : Serializable {

    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh)"
}
