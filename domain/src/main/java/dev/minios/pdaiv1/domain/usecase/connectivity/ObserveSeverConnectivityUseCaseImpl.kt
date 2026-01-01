package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.gateway.ServerConnectivityGateway

internal class ObserveSeverConnectivityUseCaseImpl(
    private val serverConnectivityGateway: ServerConnectivityGateway,
) : ObserveSeverConnectivityUseCase {

    override fun invoke() = serverConnectivityGateway
        .observe()
        .distinctUntilChanged()
}
