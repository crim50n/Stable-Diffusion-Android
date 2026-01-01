package dev.minios.pdaiv1.network.api.pdai

import dev.minios.pdaiv1.network.model.SupporterRaw
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface DonateApi {

    @GET("/supporters.json")
    fun fetchSupporters(): Single<List<SupporterRaw>>
}
