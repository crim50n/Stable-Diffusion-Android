package dev.minios.pdaiv1.feature.auth.di

import dev.minios.pdaiv1.domain.feature.auth.AuthorizationStore
import dev.minios.pdaiv1.feature.auth.AuthorizationStoreImpl
import dev.minios.pdaiv1.feature.auth.crypto.CryptoProvider
import dev.minios.pdaiv1.feature.auth.crypto.CryptoProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val authModule = module {
    factory<CryptoProvider> { CryptoProviderImpl(androidContext()) }
    factory<AuthorizationStore> {
        val encryptedPreferences = get<CryptoProvider>().getAuthorizationPreferences()
        AuthorizationStoreImpl(encryptedPreferences)
    }
}
