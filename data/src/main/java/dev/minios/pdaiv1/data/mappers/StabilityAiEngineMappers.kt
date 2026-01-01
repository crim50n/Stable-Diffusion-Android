package dev.minios.pdaiv1.data.mappers

import dev.minios.pdaiv1.domain.entity.StabilityAiEngine
import dev.minios.pdaiv1.network.model.StabilityAiEngineRaw

//region RAW --> DOMAIN
fun List<StabilityAiEngineRaw>.mapRawToCheckpointDomain(): List<StabilityAiEngine> =
    map(StabilityAiEngineRaw::mapRawToCheckpointDomain)

fun StabilityAiEngineRaw.mapRawToCheckpointDomain(): StabilityAiEngine = with(this) {
    StabilityAiEngine(id ?: "", name ?: "")
}
//endregion
