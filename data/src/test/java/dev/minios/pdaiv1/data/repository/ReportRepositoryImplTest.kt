package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.ReportDataSource
import dev.minios.pdaiv1.domain.entity.ReportReason
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import org.junit.Test

class ReportRepositoryImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubRemoteDataSource = mockk<ReportDataSource.Remote>()
    private val stubPreferenceManager = mockk<PreferenceManager>()

    private val repository = ReportRepositoryImpl(
        rds = stubRemoteDataSource,
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given attempt to send report with HuggingFace source, expected remote called with correct model`() {
        val text = "Bug report"
        val reason = ReportReason.Other
        val image = "base64image"
        val huggingFaceModel = "stabilityai/sdxl-turbo"

        every { stubPreferenceManager.source } returns ServerSource.HUGGING_FACE
        every { stubPreferenceManager.huggingFaceModel } returns huggingFaceModel
        every {
            stubRemoteDataSource.send(text, reason, image, ServerSource.HUGGING_FACE.toString(), huggingFaceModel)
        } returns Completable.complete()

        repository
            .send(text, reason, image)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify {
            stubRemoteDataSource.send(text, reason, image, ServerSource.HUGGING_FACE.toString(), huggingFaceModel)
        }
    }

    @Test
    fun `given attempt to send report with StabilityAI source, expected remote called with correct engine id`() {
        val text = "Feature request"
        val reason = ReportReason.InappropriateContent
        val image = ""
        val engineId = "stable-diffusion-xl-1024-v1-0"

        every { stubPreferenceManager.source } returns ServerSource.STABILITY_AI
        every { stubPreferenceManager.stabilityAiEngineId } returns engineId
        every {
            stubRemoteDataSource.send(text, reason, image, ServerSource.STABILITY_AI.toString(), engineId)
        } returns Completable.complete()

        repository
            .send(text, reason, image)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()

        verify {
            stubRemoteDataSource.send(text, reason, image, ServerSource.STABILITY_AI.toString(), engineId)
        }
    }

    @Test
    fun `given attempt to send report with ONNX source, expected remote called with correct model id`() {
        val text = "Crash report"
        val reason = ReportReason.Other
        val image = "base64"
        val modelId = "onnx-model-id"

        every { stubPreferenceManager.source } returns ServerSource.LOCAL_MICROSOFT_ONNX
        every { stubPreferenceManager.localOnnxModelId } returns modelId
        every {
            stubRemoteDataSource.send(text, reason, image, ServerSource.LOCAL_MICROSOFT_ONNX.toString(), modelId)
        } returns Completable.complete()

        repository
            .send(text, reason, image)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to send report with MediaPipe source, expected remote called with correct model id`() {
        val text = "MediaPipe issue"
        val reason = ReportReason.Other
        val image = ""
        val modelId = "mediapipe-model-id"

        every { stubPreferenceManager.source } returns ServerSource.LOCAL_GOOGLE_MEDIA_PIPE
        every { stubPreferenceManager.localMediaPipeModelId } returns modelId
        every {
            stubRemoteDataSource.send(text, reason, image, ServerSource.LOCAL_GOOGLE_MEDIA_PIPE.toString(), modelId)
        } returns Completable.complete()

        repository
            .send(text, reason, image)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to send report with Automatic1111 source, expected remote called with empty model`() {
        val text = "A1111 issue"
        val reason = ReportReason.Other
        val image = ""

        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        every {
            stubRemoteDataSource.send(text, reason, image, ServerSource.AUTOMATIC1111.toString(), "")
        } returns Completable.complete()

        repository
            .send(text, reason, image)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to send report, remote throws exception, expected error value`() {
        val text = "Bug report"
        val reason = ReportReason.Other
        val image = ""

        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        every {
            stubRemoteDataSource.send(any(), any(), any(), any(), any())
        } returns Completable.error(stubException)

        repository
            .send(text, reason, image)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
