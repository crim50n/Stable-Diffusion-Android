package dev.minios.pdaiv1.domain.usecase.downloadable

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.datasource.DownloadableModelDataSource
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class GetLocalModelUseCaseImplTest {

    private val stubLocalDataSource = mock<DownloadableModelDataSource.Local>()

    private val useCase = GetLocalModelUseCaseImpl(stubLocalDataSource)

    private val stubModel = LocalAiModel(
        id = "test-model-id",
        type = LocalAiModel.Type.ONNX,
        name = "Test Model",
        size = "5 GB",
        sources = listOf("https://example.com/model"),
        downloaded = true,
        selected = false,
    )

    @Test
    fun `given local data source returned model, expected valid model value`() {
        whenever(stubLocalDataSource.getById("test-model-id"))
            .thenReturn(Single.just(stubModel))

        useCase("test-model-id")
            .test()
            .assertNoErrors()
            .assertValue(stubModel)
            .await()
            .assertComplete()
    }

    @Test
    fun `given local data source thrown exception, expected error value`() {
        val stubException = Throwable("Model not found.")

        whenever(stubLocalDataSource.getById("nonexistent-id"))
            .thenReturn(Single.error(stubException))

        useCase("nonexistent-id")
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
