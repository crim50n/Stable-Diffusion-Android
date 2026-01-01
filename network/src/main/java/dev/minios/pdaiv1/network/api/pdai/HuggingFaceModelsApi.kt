package dev.minios.pdaiv1.network.api.pdai

import dev.minios.pdaiv1.network.model.HuggingFaceModelRaw
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface HuggingFaceModelsApi {

    @GET("/hf-models.json")
    fun fetchHuggingFaceModels(): Single<List<HuggingFaceModelRaw>>
}
