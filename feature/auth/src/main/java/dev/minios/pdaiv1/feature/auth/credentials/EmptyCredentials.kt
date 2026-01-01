package dev.minios.pdaiv1.feature.auth.credentials

internal class EmptyCredentials : Credentials {
    override fun toJson(): String = ""
}
