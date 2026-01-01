package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToDomain
import dev.minios.pdaiv1.domain.datasource.SupportersDataSource
import dev.minios.pdaiv1.domain.entity.Supporter
import dev.minios.pdaiv1.network.api.pdai.DonateApi
import dev.minios.pdaiv1.network.model.SupporterRaw
import io.reactivex.rxjava3.core.Single

internal class SupportersRemoteDataSource(
    private val api: DonateApi,
) : SupportersDataSource.Remote {

    override fun fetch(): Single<List<Supporter>> = api
        .fetchSupporters()
        .map(List<SupporterRaw>::mapRawToDomain)
}
