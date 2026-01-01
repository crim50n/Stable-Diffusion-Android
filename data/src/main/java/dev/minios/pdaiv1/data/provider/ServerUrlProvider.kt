package dev.minios.pdaiv1.data.provider

import io.reactivex.rxjava3.core.Single

fun interface ServerUrlProvider {
    operator fun invoke(endpoint: String): Single<String>
}
