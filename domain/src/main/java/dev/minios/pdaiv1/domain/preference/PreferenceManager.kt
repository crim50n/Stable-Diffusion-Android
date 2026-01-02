package dev.minios.pdaiv1.domain.preference

import dev.minios.pdaiv1.core.common.schedulers.SchedulersToken
import dev.minios.pdaiv1.domain.entity.Grid
import dev.minios.pdaiv1.domain.entity.ModelType
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.Settings
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

interface PreferenceManager {
    var automatic1111ServerUrl: String
    var swarmUiServerUrl: String
    var swarmUiModel: String
    var demoMode: Boolean
    var developerMode: Boolean
    var localMediaPipeCustomModelPath: String
    var localQnnCustomModelPath: String
    var localOnnxCustomModelPath: String
    var localOnnxAllowCancel: Boolean
    var localOnnxSchedulerThread: SchedulersToken
    var monitorConnectivity: Boolean
    var autoSaveAiResults: Boolean
    var saveToMediaStore: Boolean
    var formAdvancedOptionsAlwaysShow: Boolean
    var formPromptTaggedInput: Boolean
    var source: ServerSource
    var sdModel: String
    var modelType: ModelType
    var hordeApiKey: String
    var openAiApiKey: String
    var huggingFaceApiKey: String
    var huggingFaceModel: String
    var stabilityAiApiKey: String
    var stabilityAiEngineId: String
    var falAiApiKey: String
    var falAiSelectedEndpointId: String
    var onBoardingComplete: Boolean
    var forceSetupAfterUpdate: Boolean
    var localOnnxModelId: String
    var localOnnxUseNNAPI: Boolean
    var localOnnxLastPrompt: String
    var localOnnxLastNegativePrompt: String
    var localMediaPipeModelId: String
    var localMediaPipeLastPrompt: String
    var localMediaPipeLastNegativePrompt: String
    var localQnnModelId: String
    var localQnnRunOnCpu: Boolean
    var localQnnUseOpenCL: Boolean
    var localQnnScheduler: String
    var localQnnShowDiffusionProcess: Boolean
    var localQnnLastPrompt: String
    var localQnnLastNegativePrompt: String
    var designUseSystemColorPalette: Boolean
    var designUseSystemDarkTheme: Boolean
    var designDarkTheme: Boolean
    var designColorToken: String
    var designDarkThemeToken: String
    var backgroundGeneration: Boolean
    var backgroundProcessCount: Int
    var galleryGrid: Grid

    fun observe(): Flowable<Settings>
    fun refresh(): Completable

    fun saveFalAiEndpointParams(endpointId: String, params: Map<String, Any?>)
    fun getFalAiEndpointParams(endpointId: String): Map<String, Any?>
}
