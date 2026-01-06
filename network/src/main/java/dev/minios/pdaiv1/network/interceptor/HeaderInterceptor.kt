package dev.minios.pdaiv1.network.interceptor

import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.common.extensions.applyIf
import dev.minios.pdaiv1.network.qualifiers.NetworkHeaders
import dev.minios.pdaiv1.network.qualifiers.ApiKeyProvider
import okhttp3.Interceptor
import okhttp3.Response

internal class HeaderInterceptor(
    private val buildInfoProvider: BuildInfoProvider,
    private val apiKeyProvider: ApiKeyProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain
        .request()
        .newBuilder()
        .addHeader(NetworkHeaders.APP_VERSION, buildInfoProvider.version.toString())
        .applyIf(apiKeyProvider() != null) {
            val (header, key) = apiKeyProvider.invoke() ?: ("" to "")
            // Sanitize key - remove control characters that OkHttp doesn't allow
            val sanitizedKey = key.filter { it.code >= 0x20 || it == '\t' }
            if (header.isNotEmpty() && sanitizedKey.isNotEmpty()) {
                addHeader(header, sanitizedKey)
            }
            this
        }
        .build()
        .let(chain::proceed)
}
