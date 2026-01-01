package dev.minios.pdaiv1.domain.usecase.caching

import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.common.log.FileLoggingTree
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Completable

internal class ClearAppCacheUseCaseImpl(
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val repository: GenerationResultRepository,
) : ClearAppCacheUseCase {

    override fun invoke() = Completable.concatArray(
        repository.deleteAll(),
        Completable.fromAction { FileLoggingTree.clearLog(fileProviderDescriptor) },
    )
}
