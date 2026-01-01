package dev.minios.pdaiv1.data.mappers

import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.network.response.DownloadableModelResponse
import dev.minios.pdaiv1.storage.db.persistent.entity.LocalModelEntity

//region RAW --> DOMAIN
fun List<DownloadableModelResponse>.mapRawToCheckpointDomain(
    type: LocalAiModel.Type,
): List<LocalAiModel> = map { it.mapRawToCheckpointDomain(type) }

fun DownloadableModelResponse.mapRawToCheckpointDomain(
    type: LocalAiModel.Type,
): LocalAiModel = with(this) {
    LocalAiModel(
        id = id ?: "",
        type = type,
        name = name ?: "",
        size = size ?: "",
        sources = sources ?: emptyList(),
        chipsetSuffix = metadata?.chipset,
        runOnCpu = metadata?.type == "cpu",
    )
}
//endregion

//region DOMAIN --> ENTITY
fun List<LocalAiModel>.mapDomainToEntity(): List<LocalModelEntity> =
    map(LocalAiModel::mapDomainToEntity)

fun LocalAiModel.mapDomainToEntity(): LocalModelEntity = with(this) {
    LocalModelEntity(id, type.key, name, size, sources, chipsetSuffix, runOnCpu)
}
//endregion

//region ENTITY --> DOMAIN
fun List<LocalModelEntity>.mapEntityToDomain(): List<LocalAiModel> =
    map(LocalModelEntity::mapEntityToDomain)

fun LocalModelEntity.mapEntityToDomain(): LocalAiModel = with(this) {
    LocalAiModel(
        id = id,
        type = LocalAiModel.Type.parse(type),
        name = name,
        size = size,
        sources = sources,
        chipsetSuffix = chipsetSuffix,
        runOnCpu = runOnCpu,
    )
}
//endregion
