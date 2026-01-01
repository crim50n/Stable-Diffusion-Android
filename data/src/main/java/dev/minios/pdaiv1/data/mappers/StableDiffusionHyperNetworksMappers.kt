package dev.minios.pdaiv1.data.mappers

import dev.minios.pdaiv1.domain.entity.StableDiffusionHyperNetwork
import dev.minios.pdaiv1.network.model.StableDiffusionHyperNetworkRaw
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionHyperNetworkEntity

//region RAW -> DOMAIN
fun List<StableDiffusionHyperNetworkRaw>.mapRawToCheckpointDomain(): List<StableDiffusionHyperNetwork> =
    map(StableDiffusionHyperNetworkRaw::mapRawToCheckpointDomain)

fun StableDiffusionHyperNetworkRaw.mapRawToCheckpointDomain(): StableDiffusionHyperNetwork = with(this) {
    StableDiffusionHyperNetwork(
        name = name ?: "",
        path = path ?: "",
    )
}
//endregion

//region DOMAIN -> ENTITY
fun List<StableDiffusionHyperNetwork>.mapDomainToEntity(): List<StableDiffusionHyperNetworkEntity> =
    map(StableDiffusionHyperNetwork::mapDomainToEntity)

fun StableDiffusionHyperNetwork.mapDomainToEntity(): StableDiffusionHyperNetworkEntity = with(this) {
    StableDiffusionHyperNetworkEntity(
        id = name,
        name = name,
        path = path,
    )
}
//endregion

//region ENTITY -> DOMAIN
fun List<StableDiffusionHyperNetworkEntity>.mapEntityToDomain(): List<StableDiffusionHyperNetwork> =
    map(StableDiffusionHyperNetworkEntity::mapEntityToDomain)

fun StableDiffusionHyperNetworkEntity.mapEntityToDomain(): StableDiffusionHyperNetwork = with(this) {
    StableDiffusionHyperNetwork(name, path)
}
//endregion
