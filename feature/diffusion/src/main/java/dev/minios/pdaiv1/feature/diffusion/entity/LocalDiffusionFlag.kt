@file:Suppress("unused")

package dev.minios.pdaiv1.feature.diffusion.entity

enum class LocalDiffusionFlag(val value: Int) {
    CPU(0),
    NN_API(1);
}
