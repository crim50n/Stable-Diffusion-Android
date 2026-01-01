package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.StableDiffusionModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface StableDiffusionModelsRepository {
    fun fetchModels(): Completable
    fun fetchAndGetModels(): Single<List<StableDiffusionModel>>
    fun getModels(): Single<List<StableDiffusionModel>>
}
