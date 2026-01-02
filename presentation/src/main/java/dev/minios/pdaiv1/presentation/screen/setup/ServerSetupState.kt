package dev.minios.pdaiv1.presentation.screen.setup

import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.core.common.links.LinksProvider
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.domain.entity.Configuration
import dev.minios.pdaiv1.domain.entity.DownloadState
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.feature.auth.AuthorizationCredentials
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.screen.setup.mappers.withNewState
import dev.minios.pdaiv1.presentation.utils.Constants
import com.shifthackz.android.core.mvi.MviState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Immutable
data class ServerSetupState(
    val showBackNavArrow: Boolean = false,
    val onBoardingDemo: Boolean = false,
    val step: Step = Step.SOURCE,
    val mode: ServerSource = ServerSource.AUTOMATIC1111,
    val allowedModes: List<ServerSource> = ServerSource.entries,
    val screenModal: Modal = Modal.None,
    val serverUrl: String = "",
    val swarmUiUrl: String = "",
    val hordeApiKey: String = "",
    val huggingFaceApiKey: String = "",
    val openAiApiKey: String = "",
    val stabilityAiApiKey: String = "",
    val falAiApiKey: String = "",
    val falAiEndpoints: List<FalAiEndpointUi> = emptyList(),
    val falAiSelectedEndpoint: String = "",
    val hordeDefaultApiKey: Boolean = false,
    val demoMode: Boolean = false,
    val authType: AuthType = AuthType.ANONYMOUS,
    val login: String = "",
    val password: String = "",
    val huggingFaceModels: List<String> = emptyList(),
    val huggingFaceModel: String = "",
    val localOnnxModels: List<LocalModel> = emptyList(),
    val localOnnxCustomModel: Boolean = false,
    val localOnnxCustomModelPath: String = "",
    val scannedOnnxCustomModels: List<LocalModel> = emptyList(),
    val localMediaPipeModels: List<LocalModel> = emptyList(),
    val localMediaPipeCustomModel: Boolean = false,
    val localMediaPipeCustomModelPath: String = "",
    val scannedMediaPipeCustomModels: List<LocalModel> = emptyList(),
    val localQnnModels: List<LocalModel> = emptyList(),
    val localQnnCustomModel: Boolean = false,
    val localQnnCustomModelPath: String = "",
    val scannedQnnCustomModels: List<LocalModel> = emptyList(),
    val passwordVisible: Boolean = false,
    val serverUrlValidationError: UiText? = null,
    val swarmUiUrlValidationError: UiText? = null,
    val loginValidationError: UiText? = null,
    val passwordValidationError: UiText? = null,
    val hordeApiKeyValidationError: UiText? = null,
    val huggingFaceApiKeyValidationError: UiText? = null,
    val openAiApiKeyValidationError: UiText? = null,
    val stabilityAiApiKeyValidationError: UiText? = null,
    val falAiApiKeyValidationError: UiText? = null,
    val localCustomOnnxPathValidationError: UiText? = null,
    val localCustomMediaPipePathValidationError: UiText? = null,
    val localCustomQnnPathValidationError: UiText? = null,
) : MviState, KoinComponent {

    val localCustomModel: Boolean
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxCustomModel
            ServerSource.LOCAL_QUALCOMM_QNN -> localQnnCustomModel
            else -> localMediaPipeCustomModel
        }

    val localCustomModelPath: String
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxCustomModelPath
            ServerSource.LOCAL_QUALCOMM_QNN -> localQnnCustomModelPath
            else -> localMediaPipeCustomModelPath
        }

    val localModels: List<LocalModel>
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxModels
            ServerSource.LOCAL_QUALCOMM_QNN -> localQnnModels
            else -> localMediaPipeModels
        }

    val localCustomModelPathValidationError: UiText?
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localCustomOnnxPathValidationError
            ServerSource.LOCAL_QUALCOMM_QNN -> localCustomQnnPathValidationError
            else -> localCustomMediaPipePathValidationError
        }

    val demoModeUrl: String
        get() {
            val linksProvider: LinksProvider by inject()
            return linksProvider.demoModeUrl
        }

    fun withHordeApiKey(value: String) = this.copy(
        hordeApiKey = value,
        hordeDefaultApiKey = value == Constants.HORDE_DEFAULT_API_KEY,
    )

    fun withCredentials(value: AuthorizationCredentials) = when (value) {
        is AuthorizationCredentials.HttpBasic -> copy(
            login = value.login,
            password = value.password,
        )

        AuthorizationCredentials.None -> this
    }

    fun withLocalCustomModelPath(value: String): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            localOnnxCustomModelPath = value,
            localCustomOnnxPathValidationError = null,
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            localMediaPipeCustomModelPath = value,
            localCustomMediaPipePathValidationError = null,
        )

        ServerSource.LOCAL_QUALCOMM_QNN -> copy(
            localQnnCustomModelPath = value,
            localCustomQnnPathValidationError = null,
        )

        else -> this
    }

    fun withUpdatedLocalModel(value: LocalModel): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            localOnnxModels = localOnnxModels.withNewState(value)
        )
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            localMediaPipeModels = localMediaPipeModels.withNewState(value)
        )
        ServerSource.LOCAL_QUALCOMM_QNN -> copy(
            localQnnModels = localQnnModels.withNewState(value)
        )
        else -> this
    }

    fun withDeletedLocalModel(value: LocalModel): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            screenModal = Modal.None,
            localOnnxModels = localOnnxModels.withNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            )
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            screenModal = Modal.None,
            localMediaPipeModels = localMediaPipeModels.withNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            )
        )

        ServerSource.LOCAL_QUALCOMM_QNN -> copy(
            screenModal = Modal.None,
            localQnnModels = localQnnModels.withNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            )
        )

        else -> copy(screenModal = Modal.None)
    }

    fun withSelectedLocalModel(value: LocalModel): ServerSetupState {
        fun List<LocalModel>.selectModel(): List<LocalModel> {
            // Find existing model to preserve its downloaded/downloadState
            val existing = find { it.id == value.id }
            val updatedModel = value.copy(
                selected = true,
                downloaded = existing?.downloaded ?: value.downloaded,
                downloadState = existing?.downloadState ?: value.downloadState,
            )
            return withNewState(updatedModel)
        }

        // Check if this is a scanned custom model (ID starts with CUSTOM_)
        val isScannedCustomModel = value.id.startsWith("CUSTOM_QNN:") ||
            value.id.startsWith("CUSTOM_ONNX:") ||
            value.id.startsWith("CUSTOM_MP:")

        return when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> if (isScannedCustomModel) {
                copy(
                    scannedOnnxCustomModels = scannedOnnxCustomModels.selectModel(),
                    localOnnxModels = localOnnxModels.map { it.copy(selected = false) },
                )
            } else {
                copy(
                    localOnnxModels = localOnnxModels.selectModel(),
                    scannedOnnxCustomModels = scannedOnnxCustomModels.map { it.copy(selected = false) },
                )
            }

            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> if (isScannedCustomModel) {
                copy(
                    scannedMediaPipeCustomModels = scannedMediaPipeCustomModels.selectModel(),
                    localMediaPipeModels = localMediaPipeModels.map { it.copy(selected = false) },
                )
            } else {
                copy(
                    localMediaPipeModels = localMediaPipeModels.selectModel(),
                    scannedMediaPipeCustomModels = scannedMediaPipeCustomModels.map { it.copy(selected = false) },
                )
            }

            ServerSource.LOCAL_QUALCOMM_QNN -> if (isScannedCustomModel) {
                copy(
                    scannedQnnCustomModels = scannedQnnCustomModels.selectModel(),
                    localQnnModels = localQnnModels.map { it.copy(selected = false) },
                )
            } else {
                copy(
                    localQnnModels = localQnnModels.selectModel(),
                    scannedQnnCustomModels = scannedQnnCustomModels.map { it.copy(selected = false) },
                )
            }

            else -> this
        }
    }

    fun withAllowCustomModel(value: Boolean): ServerSetupState {
        fun List<LocalModel>.updateCustomModelSelection(id: String) = withNewState(
            find { m -> m.id == id }?.copy(selected = value)
        )
        return when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> this.copy(
                localOnnxCustomModel = value,
                localOnnxModels = localOnnxModels.updateCustomModelSelection(
                    id = LocalAiModel.CustomOnnx.id,
                ),
            )

            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> this.copy(
                localMediaPipeCustomModel = value,
                localMediaPipeModels = localMediaPipeModels.updateCustomModelSelection(
                    id = LocalAiModel.CustomMediaPipe.id,
                ),
            )

            ServerSource.LOCAL_QUALCOMM_QNN -> this.copy(
                localQnnCustomModel = value,
                localQnnModels = localQnnModels.updateCustomModelSelection(
                    id = LocalAiModel.CustomQnn.id,
                ),
            )

            else -> this
        }
    }

    enum class Step {
        SOURCE,
        CONFIGURE;
    }

    enum class AuthType {
        ANONYMOUS,
        HTTP_BASIC;
    }

    data class LocalModel(
        val id: String,
        val name: String,
        val size: String,
        val downloaded: Boolean = false,
        val downloadState: DownloadState = DownloadState.Unknown,
        val selected: Boolean = false,
        val runOnCpu: Boolean = false,
    )

    data class FalAiEndpointUi(
        val id: String,
        val endpointId: String,
        val title: String,
        val category: String,
        val isCustom: Boolean,
    )
}

val Configuration.authType: ServerSetupState.AuthType
    get() {
        val noCredentials = ServerSetupState.AuthType.ANONYMOUS
        if (this.demoMode) return noCredentials
        if (this.source != ServerSource.AUTOMATIC1111) return noCredentials
        return when (this.authCredentials.key) {
            AuthorizationCredentials.Key.NONE -> noCredentials
            AuthorizationCredentials.Key.HTTP_BASIC -> ServerSetupState.AuthType.HTTP_BASIC
        }
    }

fun ServerSetupState.credentialsDomain(): AuthorizationCredentials {
    return when (this.authType) {
        ServerSetupState.AuthType.ANONYMOUS -> AuthorizationCredentials.None
        ServerSetupState.AuthType.HTTP_BASIC -> AuthorizationCredentials.HttpBasic(login, password)
    }
}
