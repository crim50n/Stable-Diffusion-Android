package dev.minios.pdaiv1.domain.feature.auth

interface AuthorizationStore {
    fun getAuthorizationCredentials(): AuthorizationCredentials
    fun storeAuthorizationCredentials(credentials: AuthorizationCredentials)
}
