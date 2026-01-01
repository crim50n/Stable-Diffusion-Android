package dev.minios.pdaiv1.presentation.screen.setup.mappers

import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.domain.entity.ServerSource

val BuildInfoProvider.allowedModes: List<ServerSource>
    get() = ServerSource
        .entries
        .filter { it.allowedInBuilds.contains(type) }
