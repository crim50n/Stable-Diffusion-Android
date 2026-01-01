package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapToDomain
import dev.minios.pdaiv1.data.mappers.mapToRequest
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.ServerConfigurationDataSource
import dev.minios.pdaiv1.domain.entity.ServerConfiguration
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_SD_OPTIONS
import dev.minios.pdaiv1.network.model.ServerConfigurationRaw

internal class ServerConfigurationRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : ServerConfigurationDataSource.Remote {

    override fun fetchConfiguration() = serverUrlProvider(PATH_SD_OPTIONS)
        .flatMap(api::fetchConfiguration)
        .map(ServerConfigurationRaw::mapToDomain)

    override fun updateConfiguration(configuration: ServerConfiguration) =
        serverUrlProvider(PATH_SD_OPTIONS)
            .flatMapCompletable { url ->
                api.updateConfiguration(url, configuration.mapToRequest())
            }
}
