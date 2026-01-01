package dev.minios.pdaiv1.core.common.schedulers

import kotlinx.coroutines.CoroutineDispatcher

interface DispatchersProvider {
    val io: CoroutineDispatcher
    val ui: CoroutineDispatcher
    val immediate: CoroutineDispatcher
}
