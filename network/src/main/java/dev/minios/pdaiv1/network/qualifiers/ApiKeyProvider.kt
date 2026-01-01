package dev.minios.pdaiv1.network.qualifiers

fun interface ApiKeyProvider {
    operator fun invoke(): Pair<String, String>?
}
