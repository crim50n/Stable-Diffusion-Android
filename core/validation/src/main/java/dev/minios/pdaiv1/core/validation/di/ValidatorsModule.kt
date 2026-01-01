package dev.minios.pdaiv1.core.validation.di

import dev.minios.pdaiv1.core.validation.common.CommonStringValidator
import dev.minios.pdaiv1.core.validation.common.CommonStringValidatorImpl
import dev.minios.pdaiv1.core.validation.dimension.DimensionValidator
import dev.minios.pdaiv1.core.validation.dimension.DimensionValidatorImpl
import dev.minios.pdaiv1.core.validation.path.FilePathValidator
import dev.minios.pdaiv1.core.validation.path.FilePathValidatorImpl
import dev.minios.pdaiv1.core.validation.url.UrlValidator
import dev.minios.pdaiv1.core.validation.url.UrlValidatorImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val validatorsModule = module {
    // !!! Do not use [factoryOf] for DimensionValidatorImpl, it has 2 default Ints in constructor
    factory<DimensionValidator> { DimensionValidatorImpl() }
    factory<UrlValidator> { UrlValidatorImpl() }

    factoryOf(::CommonStringValidatorImpl) bind CommonStringValidator::class
    factoryOf(::FilePathValidatorImpl) bind FilePathValidator::class
}
