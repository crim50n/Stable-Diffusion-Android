package dev.minios.pdaiv1.data.gateway

import dev.minios.pdaiv1.domain.gateway.DatabaseClearGateway
import dev.minios.pdaiv1.storage.gateway.GatewayClearCacheDb
import dev.minios.pdaiv1.storage.gateway.GatewayClearPersistentDb
import io.reactivex.rxjava3.core.Completable

internal class DatabaseClearGatewayImpl(
    private val gatewayClearCacheDb: GatewayClearCacheDb,
    private val gatewayClearPersistentDb: GatewayClearPersistentDb,
) : DatabaseClearGateway {

    override fun clearSessionScopeDb(): Completable = Completable.fromAction {
        gatewayClearCacheDb()
    }

    override fun clearStorageScopeDb(): Completable = Completable.fromAction {
        gatewayClearPersistentDb()
    }
}
