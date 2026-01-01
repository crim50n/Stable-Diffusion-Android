package dev.minios.pdaiv1.data.di

val dataModule = (remoteDataSourceModule + localDataSourceModule + repositoryModule)
    .toTypedArray()
