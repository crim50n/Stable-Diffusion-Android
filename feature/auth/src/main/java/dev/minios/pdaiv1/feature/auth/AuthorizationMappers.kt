package dev.minios.pdaiv1.feature.auth

import dev.minios.pdaiv1.domain.feature.auth.AuthorizationCredentials
import dev.minios.pdaiv1.feature.auth.credentials.Credentials
import dev.minios.pdaiv1.feature.auth.credentials.EmptyCredentials
import dev.minios.pdaiv1.feature.auth.credentials.HttpBasicCredentials

internal fun AuthorizationCredentials.toRaw(): Credentials = when (this) {
    is AuthorizationCredentials.HttpBasic -> HttpBasicCredentials(
        login = login,
        password = password,
    )
    else -> EmptyCredentials()
}

internal fun Credentials.toDomain(): AuthorizationCredentials = when {
    this is HttpBasicCredentials -> AuthorizationCredentials.HttpBasic(
        login = login,
        password = password,
    )
    else -> AuthorizationCredentials.None
}

internal fun parseByKeyValueToRaw(
    key: AuthorizationCredentials.Key,
    rawValue: String
): Credentials = when (key) {
    AuthorizationCredentials.Key.NONE -> EmptyCredentials()
    AuthorizationCredentials.Key.HTTP_BASIC -> HttpBasicCredentials.fromJson(rawValue)
}
