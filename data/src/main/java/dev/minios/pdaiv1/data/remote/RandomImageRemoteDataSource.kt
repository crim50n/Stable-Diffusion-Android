package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.domain.datasource.RandomImageDataSource
import dev.minios.pdaiv1.network.api.imagecdn.ImageCdnRestApi

internal class RandomImageRemoteDataSource(
    private val api: ImageCdnRestApi,
) : RandomImageDataSource.Remote {

    override fun fetch() = api.fetchRandomImage()
}
