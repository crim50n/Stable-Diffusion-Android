package dev.minios.pdaiv1.data.mappers

import dev.minios.pdaiv1.domain.entity.Embedding
import dev.minios.pdaiv1.domain.entity.LoRA
import dev.minios.pdaiv1.domain.entity.SwarmUiModel
import dev.minios.pdaiv1.network.model.SwarmUiModelRaw
import dev.minios.pdaiv1.network.response.SwarmUiModelsResponse
import dev.minios.pdaiv1.storage.db.cache.entity.SwarmUiModelEntity

//region RAW --> CHECKPOINT DOMAIN
fun SwarmUiModelsResponse.mapRawToCheckpointDomain(): List<SwarmUiModel> = with(this) {
    this.files?.mapRawToCheckpointDomain() ?: emptyList()
}

fun List<SwarmUiModelRaw>.mapRawToCheckpointDomain(): List<SwarmUiModel> = map(SwarmUiModelRaw::mapRawToCheckpointDomain)

fun SwarmUiModelRaw.mapRawToCheckpointDomain(): SwarmUiModel = with(this) {
    SwarmUiModel(
        name = name ?: "",
        title = title ?: "",
        author = author ?: "",
    )
}
//endregion

//region RAW --> LORA DOMAIN
fun SwarmUiModelsResponse.mapRawToLoraDomain(): List<LoRA> = with(this) {
    this.files?.mapRawToLoraDomain() ?: emptyList()
}

fun List<SwarmUiModelRaw>.mapRawToLoraDomain(): List<LoRA> = map(SwarmUiModelRaw::mapRawToLoraDomain)

fun SwarmUiModelRaw.mapRawToLoraDomain(): LoRA = with(this) {
    LoRA(
        name = name ?: "",
        alias = title ?: "",
        path = "",
    )
}
//endregion

//region RAW -> EMBEDDING DOMAIN
fun SwarmUiModelsResponse.mapRawToEmbeddingDomain(): List<Embedding> = with(this) {
    this.files?.mapRawToEmbeddingDomain() ?: emptyList()
}

fun List<SwarmUiModelRaw>.mapRawToEmbeddingDomain(): List<Embedding> = map(SwarmUiModelRaw::mapRawToEmbeddingDomain)

fun SwarmUiModelRaw.mapRawToEmbeddingDomain(): Embedding = with(this) {
    Embedding(title ?: "")
}
//endregion

//region CHECKPOINT DOMAIN --> ENTITY
fun List<SwarmUiModel>.mapDomainToEntity(): List<SwarmUiModelEntity> = map(SwarmUiModel::mapDomainToEntity)

fun SwarmUiModel.mapDomainToEntity(): SwarmUiModelEntity = with(this) {
    SwarmUiModelEntity(
        id = "${name}_${title}",
        name = name,
        title = title,
        author = author,
    )
}
//endregion

//region ENTITY --> CHECKPOINT DOMAIN
fun List<SwarmUiModelEntity>.mapEntityToDomain(): List<SwarmUiModel> = map(SwarmUiModelEntity::mapEntityToDomain)

fun SwarmUiModelEntity.mapEntityToDomain(): SwarmUiModel = with(this) {
    SwarmUiModel(
        name = name,
        title = title,
        author = author,
    )
}
//endregion
