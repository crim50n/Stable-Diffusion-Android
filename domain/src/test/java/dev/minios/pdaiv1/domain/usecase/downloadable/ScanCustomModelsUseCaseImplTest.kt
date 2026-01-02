package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ScanCustomModelsUseCaseImplTest {

    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val useCase = ScanCustomModelsUseCaseImpl(stubPreferenceManager)

    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = createTempDirectory("scan_custom_models_test").toFile()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `given base path does not exist, expected empty list`() {
        every { stubPreferenceManager.localOnnxCustomModelPath } returns "/nonexistent/path"

        useCase(LocalAiModel.Type.ONNX)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given base path is not a directory, expected empty list`() {
        val file = File(tempDir, "not_a_directory.txt")
        file.createNewFile()

        every { stubPreferenceManager.localOnnxCustomModelPath } returns file.absolutePath

        useCase(LocalAiModel.Type.ONNX)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given empty directory, expected empty list`() {
        every { stubPreferenceManager.localOnnxCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.ONNX)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given valid ONNX model directory, expected model in list`() {
        val modelDir = File(tempDir, "my_custom_model")
        modelDir.mkdirs()

        File(modelDir, "text_encoder").mkdirs()
        File(modelDir, "text_encoder/model.ort").createNewFile()
        File(modelDir, "unet").mkdirs()
        File(modelDir, "unet/model.ort").createNewFile()
        File(modelDir, "vae_decoder").mkdirs()
        File(modelDir, "vae_decoder/model.ort").createNewFile()
        File(modelDir, "tokenizer").mkdirs()
        File(modelDir, "tokenizer/vocab.json").createNewFile()
        File(modelDir, "tokenizer/merges.txt").createNewFile()

        every { stubPreferenceManager.localOnnxCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.ONNX)
            .test()
            .assertNoErrors()
            .assertValue { models ->
                models.size == 1 &&
                models[0].id == "CUSTOM_ONNX:my_custom_model" &&
                models[0].name == "my_custom_model" &&
                models[0].type == LocalAiModel.Type.ONNX &&
                models[0].downloaded
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given invalid ONNX model directory (missing files), expected empty list`() {
        val modelDir = File(tempDir, "incomplete_model")
        modelDir.mkdirs()

        File(modelDir, "text_encoder").mkdirs()
        File(modelDir, "text_encoder/model.ort").createNewFile()

        every { stubPreferenceManager.localOnnxCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.ONNX)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given valid QNN model directory with NPU files, expected model in list`() {
        val modelDir = File(tempDir, "qnn_model")
        modelDir.mkdirs()

        File(modelDir, "clip.bin").createNewFile()
        File(modelDir, "unet.bin").createNewFile()
        File(modelDir, "vae_decoder.bin").createNewFile()
        File(modelDir, "tokenizer.json").createNewFile()

        every { stubPreferenceManager.localQnnCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.QNN)
            .test()
            .assertNoErrors()
            .assertValue { models ->
                models.size == 1 &&
                models[0].id == "CUSTOM_QNN:qnn_model" &&
                models[0].type == LocalAiModel.Type.QNN
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given valid QNN model directory with CPU files, expected model in list`() {
        val modelDir = File(tempDir, "qnn_cpu_model")
        modelDir.mkdirs()

        File(modelDir, "clip.mnn").createNewFile()
        File(modelDir, "unet.mnn").createNewFile()
        File(modelDir, "vae_decoder.mnn").createNewFile()
        File(modelDir, "tokenizer.json").createNewFile()

        every { stubPreferenceManager.localQnnCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.QNN)
            .test()
            .assertNoErrors()
            .assertValue { models ->
                models.size == 1 &&
                models[0].id == "CUSTOM_QNN:qnn_cpu_model" &&
                models[0].type == LocalAiModel.Type.QNN
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given valid MediaPipe model directory, expected model in list`() {
        val modelDir = File(tempDir, "mediapipe_model")
        modelDir.mkdirs()

        val binsDir = File(modelDir, "bins")
        binsDir.mkdirs()
        File(binsDir, "model.bin").createNewFile()

        every { stubPreferenceManager.localMediaPipeCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.MediaPipe)
            .test()
            .assertNoErrors()
            .assertValue { models ->
                models.size == 1 &&
                models[0].id == "CUSTOM_MP:mediapipe_model" &&
                models[0].type == LocalAiModel.Type.MediaPipe
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given MediaPipe directory with QNN-specific files, expected empty list`() {
        val modelDir = File(tempDir, "wrong_model")
        modelDir.mkdirs()

        val binsDir = File(modelDir, "bins")
        binsDir.mkdirs()
        File(binsDir, "clip.bin").createNewFile()
        File(binsDir, "unet.bin").createNewFile()

        every { stubPreferenceManager.localMediaPipeCustomModelPath } returns tempDir.absolutePath

        useCase(LocalAiModel.Type.MediaPipe)
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }
}
