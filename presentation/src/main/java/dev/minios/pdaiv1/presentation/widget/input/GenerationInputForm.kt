package dev.minios.pdaiv1.presentation.widget.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.minios.pdaiv1.core.common.math.roundTo
import dev.minios.pdaiv1.core.model.asString
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.domain.entity.ADetailerConfig
import dev.minios.pdaiv1.domain.entity.ForgeModule
import dev.minios.pdaiv1.domain.entity.ModelType
import dev.minios.pdaiv1.domain.entity.OpenAiModel
import dev.minios.pdaiv1.domain.entity.OpenAiQuality
import dev.minios.pdaiv1.domain.entity.OpenAiSize
import dev.minios.pdaiv1.domain.entity.OpenAiStyle
import dev.minios.pdaiv1.domain.entity.Scheduler
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.StabilityAiClipGuidance
import dev.minios.pdaiv1.domain.entity.StabilityAiSampler
import dev.minios.pdaiv1.domain.entity.StabilityAiStylePreset
import dev.minios.pdaiv1.presentation.core.GenerationMviIntent
import dev.minios.pdaiv1.presentation.core.GenerationMviState
import dev.minios.pdaiv1.presentation.model.AspectRatio
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.model.QnnResolution
import dev.minios.pdaiv1.presentation.theme.sliderColors
import dev.minios.pdaiv1.presentation.theme.textFieldColors
import dev.minios.pdaiv1.presentation.utils.Constants
import dev.minios.pdaiv1.presentation.utils.Constants.BATCH_RANGE_MAX
import dev.minios.pdaiv1.presentation.utils.Constants.BATCH_RANGE_MIN
import dev.minios.pdaiv1.presentation.utils.Constants.CFG_SCALE_RANGE_MAX
import dev.minios.pdaiv1.presentation.utils.Constants.CFG_SCALE_RANGE_MIN
import dev.minios.pdaiv1.presentation.utils.Constants.SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
import dev.minios.pdaiv1.presentation.utils.Constants.SAMPLING_STEPS_RANGE_MAX
import dev.minios.pdaiv1.presentation.utils.Constants.SAMPLING_STEPS_RANGE_MIN
import dev.minios.pdaiv1.presentation.utils.Constants.SAMPLING_STEPS_RANGE_STABILITY_AI_MAX
import dev.minios.pdaiv1.presentation.utils.Constants.SUB_SEED_STRENGTH_MAX
import dev.minios.pdaiv1.presentation.utils.Constants.SUB_SEED_STRENGTH_MIN
import dev.minios.pdaiv1.presentation.widget.engine.EngineSelectionComponent
import dev.minios.pdaiv1.presentation.widget.engine.QnnRuntimeSelectionComponent
import dev.minios.pdaiv1.presentation.widget.input.chip.ChipTextFieldEvent
import dev.minios.pdaiv1.presentation.widget.input.chip.ChipTextFieldWithItem
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random
import dev.minios.pdaiv1.core.localization.R as LocalizationR

@Composable
fun GenerationInputForm(
    modifier: Modifier = Modifier,
    state: GenerationMviState,
    isImg2Img: Boolean = false,
    promptChipTextFieldState: MutableState<TextFieldValue>,
    negativePromptChipTextFieldState: MutableState<TextFieldValue>,
    processIntent: (GenerationMviIntent) -> Unit = {},
    afterSlidersSection: @Composable () -> Unit = {},
) {
    @Composable
    fun batchComponent() {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(
                id = LocalizationR.string.hint_batch,
                "${state.batchCount}",
            ),
        )
        SliderTextInputField(
            value = state.batchCount * 1f,
            valueRange = (BATCH_RANGE_MIN * 1f)..(BATCH_RANGE_MAX * 1f),
            valueDiff = 1f,
            fractionDigits = 0,
            steps = abs(BATCH_RANGE_MIN - BATCH_RANGE_MAX) - 1,
            sliderColors = sliderColors,
            onValueChange = { processIntent(GenerationMviIntent.Update.Batch(it.roundToInt())) },
        )
    }

    @Composable
    fun RowScope.sizeTextFieldsComponent(modifier: Modifier = Modifier) {
        TextField(
            modifier = modifier.padding(end = 4.dp),
            value = state.width,
            onValueChange = { value ->
                if (value.length <= 4) {
                    value
                        .filter { it.isDigit() }
                        .let(GenerationMviIntent.Update.Size::Width)
                        .let(processIntent::invoke)
                }
            },
            isError = state.widthValidationError != null,
            supportingText = {
                state.widthValidationError?.let {
                    Text(
                        it.asString(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            label = { Text(stringResource(id = LocalizationR.string.width)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = textFieldColors,
        )
        TextField(
            modifier = modifier.padding(start = 4.dp),
            value = state.height,
            onValueChange = { value ->
                if (value.length <= 4) {
                    value
                        .filter { it.isDigit() }
                        .let(GenerationMviIntent.Update.Size::Height)
                        .let(processIntent::invoke)
                }
            },
            isError = state.heightValidationError != null,
            supportingText = {
                state.heightValidationError?.let {
                    Text(
                        it.asString(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            label = { Text(stringResource(id = LocalizationR.string.height)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = textFieldColors,
        )
    }

    @Composable
    fun sizeButtonsComponent() {
        var aspectRatioMenuExpanded by remember { mutableStateOf(false) }

        IconButton(
            onClick = { processIntent(GenerationMviIntent.Update.Size.Swap) },
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = stringResource(id = LocalizationR.string.swap_dimensions),
            )
        }

        Box {
            IconButton(
                onClick = { aspectRatioMenuExpanded = true },
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = stringResource(id = LocalizationR.string.aspect_ratio),
                )
            }
            DropdownMenu(
                expanded = aspectRatioMenuExpanded,
                onDismissRequest = { aspectRatioMenuExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                AspectRatio.entries.forEach { ratio ->
                    DropdownMenuItem(
                        text = { Text(ratio.label) },
                        onClick = {
                            aspectRatioMenuExpanded = false
                            processIntent(GenerationMviIntent.Update.Size.AspectRatio(ratio))
                        },
                    )
                }
            }
        }
    }

    Column(modifier = modifier) {
        if (!state.onBoardingDemo) {
            when (state.mode) {
                ServerSource.AUTOMATIC1111,
                ServerSource.SWARM_UI,
                ServerSource.STABILITY_AI,
                ServerSource.HUGGING_FACE,
                ServerSource.LOCAL_MICROSOFT_ONNX,
                ServerSource.LOCAL_QUALCOMM_QNN -> EngineSelectionComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )

                ServerSource.OPEN_AI -> DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = LocalizationR.string.hint_model_open_ai.asUiText(),
                    value = state.openAiModel,
                    items = OpenAiModel.entries,
                    onItemSelected = { processIntent(GenerationMviIntent.Update.OpenAi.Model(it)) },
                )

                else -> Unit
            }
            // Model type selection (SD/SDXL/Flux) for A1111
            if (state.mode == ServerSource.AUTOMATIC1111) {
                DropdownTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = LocalizationR.string.hint_model_type.asUiText(),
                    value = state.modelType,
                    items = ModelType.entries,
                    onItemSelected = { processIntent(GenerationMviIntent.Update.ModelTypeChange(it)) },
                    displayDelegate = { it.displayName.asUiText() },
                )
                // VAE / Text Encoder multi-select (Forge only)
                if (state.availableForgeModules.isNotEmpty()) {
                    MultiSelectDropdownField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = LocalizationR.string.hint_vae_text_encoder.asUiText(),
                        selectedItems = state.selectedForgeModules,
                        availableItems = state.availableForgeModules,
                        onSelectionChanged = { modules ->
                            processIntent(GenerationMviIntent.Update.ForgeModules(modules))
                        },
                        displayDelegate = { it.name.asUiText() },
                    )
                }
            }
        }
        if (state.formPromptTaggedInput) {
            ChipTextFieldWithItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textFieldValueState = promptChipTextFieldState,
                label = LocalizationR.string.hint_prompt,
                list = state.promptKeywords,
                onItemClick = { _, tag ->
                    processIntent(
                        GenerationMviIntent.SetModal(
                            Modal.EditTag(
                                prompt = state.prompt,
                                negativePrompt = state.negativePrompt,
                                tag = tag,
                                isNegative = false,
                            )
                        )
                    )
                },
            ) { event ->
                val prompt = processTaggedPrompt(state.promptKeywords, event)
                processIntent(GenerationMviIntent.Update.Prompt(prompt))
            }
        } else {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                value = state.prompt,
                onValueChange = { processIntent(GenerationMviIntent.Update.Prompt(it)) },
                label = { Text(stringResource(id = LocalizationR.string.hint_prompt)) },
                colors = textFieldColors,
            )
        }

        // Horde does not support "negative prompt", Flux models also don't support it
        // Show negative prompt only for non-Flux model types
        val showNegativePrompt = when (state.mode) {
            ServerSource.AUTOMATIC1111 -> state.modelType != ModelType.FLUX
            ServerSource.SWARM_UI,
            ServerSource.HUGGING_FACE,
            ServerSource.STABILITY_AI,
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_QUALCOMM_QNN -> true
            else -> false
        }
        if (showNegativePrompt) {
            if (state.formPromptTaggedInput) {
                ChipTextFieldWithItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textFieldValueState = negativePromptChipTextFieldState,
                    label = LocalizationR.string.hint_prompt_negative,
                    list = state.negativePromptKeywords,
                    onItemClick = { _, tag ->
                        processIntent(
                            GenerationMviIntent.SetModal(
                                Modal.EditTag(
                                    prompt = state.prompt,
                                    negativePrompt = state.negativePrompt,
                                    tag = tag,
                                    isNegative = true,
                                )
                            )
                        )
                    },
                ) { event ->
                    val prompt = processTaggedPrompt(state.negativePromptKeywords, event)
                    processIntent(GenerationMviIntent.Update.NegativePrompt(prompt))
                }
            } else {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    value = state.negativePrompt,
                    onValueChange = { processIntent(GenerationMviIntent.Update.NegativePrompt(it)) },
                    label = { Text(stringResource(id = LocalizationR.string.hint_prompt_negative)) },
                    colors = textFieldColors,
                )
            }
        }

        // Size input fields with control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (state.mode) {
                ServerSource.HORDE,
                ServerSource.LOCAL_MICROSOFT_ONNX -> {
                    DropdownTextField(
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        label = LocalizationR.string.width.asUiText(),
                        value = state.width,
                        items = Constants.sizes,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.Size.Width(it)) },
                    )
                    DropdownTextField(
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        label = LocalizationR.string.height.asUiText(),
                        value = state.height,
                        items = Constants.sizes,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.Size.Height(it)) },
                    )
                }

                ServerSource.AUTOMATIC1111,
                ServerSource.SWARM_UI,
                ServerSource.HUGGING_FACE -> {
                    sizeTextFieldsComponent(Modifier.weight(1f))
                    sizeButtonsComponent()
                }

                ServerSource.STABILITY_AI -> {
                    if (!isImg2Img) {
                        sizeTextFieldsComponent(Modifier.weight(1f))
                        sizeButtonsComponent()
                    }
                }

                ServerSource.OPEN_AI -> {
                    DropdownTextField(
                        label = LocalizationR.string.hint_image_size.asUiText(),
                        value = state.openAiSize,
                        items = OpenAiSize.entries.filter {
                            it.supportedModels.contains(state.openAiModel)
                        },
                        onItemSelected = { processIntent(GenerationMviIntent.Update.OpenAi.Size(it)) },
                        displayDelegate = { it.key.asUiText() },
                    )
                }

                ServerSource.LOCAL_QUALCOMM_QNN -> {
                    val availableResolutions = QnnResolution.forModelType(state.qnnRunOnCpu)
                    val defaultResolution = QnnResolution.defaultForModelType(state.qnnRunOnCpu)
                    val currentResolution = QnnResolution.fromDimensions(
                        state.width.toIntOrNull() ?: defaultResolution.width,
                        state.height.toIntOrNull() ?: defaultResolution.height
                    ) ?: defaultResolution
                    DropdownTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = LocalizationR.string.hint_image_size.asUiText(),
                        value = currentResolution,
                        items = availableResolutions,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.Qnn.Resolution(it)) },
                        displayDelegate = { it.displayName.asUiText() },
                    )
                }

                ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
                ServerSource.FAL_AI -> Unit
            }
        }

        if (state.mode == ServerSource.OPEN_AI) {
            if (state.openAiModel == OpenAiModel.DALL_E_3) {
                DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = LocalizationR.string.hint_quality.asUiText(),
                    value = state.openAiQuality,
                    items = OpenAiQuality.entries,
                    onItemSelected = { processIntent(GenerationMviIntent.Update.OpenAi.Quality(it)) },
                )
                DropdownTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    label = LocalizationR.string.hint_style.asUiText(),
                    value = state.openAiStyle,
                    items = OpenAiStyle.entries,
                    onItemSelected = { processIntent(GenerationMviIntent.Update.OpenAi.Style(it)) },
                )
            }
            batchComponent()
        }

        if (state.advancedToggleButtonVisible && state.mode != ServerSource.OPEN_AI) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    processIntent(
                        GenerationMviIntent.SetAdvancedOptionsVisibility(!state.advancedOptionsVisible)
                    )
                },
            ) {
                Icon(
                    imageVector = if (state.advancedOptionsVisible) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(
                        id = if (state.advancedOptionsVisible) LocalizationR.string.action_options_hide
                        else LocalizationR.string.action_options_show
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = state.advancedOptionsVisible && state.mode != ServerSource.OPEN_AI,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sampler selection only supported for A1111, STABILITY AI, QNN
                when (state.mode) {
                    ServerSource.STABILITY_AI,
                    ServerSource.AUTOMATIC1111,
                    ServerSource.LOCAL_QUALCOMM_QNN -> DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = LocalizationR.string.hint_sampler.asUiText(),
                        value = state.selectedSampler,
                        items = state.availableSamplers,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.Sampler(it)) },
                        displayDelegate = { value ->
                            if (value == StabilityAiSampler.NONE.toString()) {
                                LocalizationR.string.hint_autodetect.asUiText()
                            } else {
                                value.asUiText()
                            }
                        }
                    )

                    else -> Unit
                }

                // Runtime selection for QNN CPU models (CPU/GPU via OpenCL)
                if (state.mode == ServerSource.LOCAL_QUALCOMM_QNN && state.qnnRunOnCpu) {
                    QnnRuntimeSelectionComponent(
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Scheduler selection only for A1111 (Flux models need specific schedulers)
                if (state.mode == ServerSource.AUTOMATIC1111) {
                    DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = LocalizationR.string.hint_scheduler.asUiText(),
                        value = state.selectedScheduler,
                        items = Scheduler.entries,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.Scheduler(it)) },
                        displayDelegate = { value -> value.displayName.asUiText() },
                    )
                }

                // Style-preset only for Stablity AI
                if (state.mode == ServerSource.STABILITY_AI) {
                    DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = LocalizationR.string.hint_style_preset.asUiText(),
                        value = state.selectedStylePreset,
                        items = StabilityAiStylePreset.entries,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.StabilityAi.Style(it)) },
                        displayDelegate = { value ->
                            if (value == StabilityAiStylePreset.NONE) {
                                LocalizationR.string.hint_autodetect.asUiText()
                            } else {
                                value.key.asUiText()
                            }
                        },
                    )
                    DropdownTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = LocalizationR.string.hint_clip_guidance_preset.asUiText(),
                        value = state.selectedClipGuidancePreset,
                        items = StabilityAiClipGuidance.entries,
                        onItemSelected = { processIntent(GenerationMviIntent.Update.StabilityAi.ClipGuidance(it)) },
                    )
                }

                // Seed is not available for Hugging Face
                if (state.mode != ServerSource.OPEN_AI) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = state.seed,
                        onValueChange = { value ->
                            value
                                .filter { it.isDigit() }
                                .let(GenerationMviIntent.Update::Seed)
                                .let(processIntent::invoke)
                        },
                        label = { Text(stringResource(id = LocalizationR.string.hint_seed)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Row {
                                if (state.seed.isNotEmpty()) {
                                    IconButton(onClick = {
                                        processIntent(GenerationMviIntent.Update.Seed(""))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    processIntent(GenerationMviIntent.Update.Seed("${Random.nextLong().absoluteValue}"))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = "Random",
                                    )
                                }
                            }
                        },
                        colors = textFieldColors,
                    )
                }
                // NSFW flag specifically for Horde API
                if (state.mode == ServerSource.HORDE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = state.nsfw,
                            onCheckedChange = { processIntent(GenerationMviIntent.Update.Nsfw(it)) },
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = stringResource(id = LocalizationR.string.hint_nsfw),
                        )
                    }
                }
                // Variation seed supported for A1111, SwarmUI
                when (state.mode) {
                    ServerSource.AUTOMATIC1111,
                    ServerSource.SWARM_UI -> TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        value = state.subSeed,
                        onValueChange = { value ->
                            value
                                .filter { it.isDigit() }
                                .let(GenerationMviIntent.Update::SubSeed)
                                .let(processIntent::invoke)
                        },
                        label = { Text(stringResource(id = LocalizationR.string.hint_sub_seed)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Row {
                                if (state.subSeed.isNotEmpty()) {
                                    IconButton(onClick = {
                                        processIntent(GenerationMviIntent.Update.SubSeed(""))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    processIntent(GenerationMviIntent.Update.SubSeed("${Random.nextLong().absoluteValue}"))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = "Random",
                                    )
                                }
                            }
                        },
                        colors = textFieldColors,
                    )

                    else -> Unit
                }
                // Sub-seed strength is not available for Local Diffusion
                when (state.mode) {
                    ServerSource.AUTOMATIC1111,
                    ServerSource.SWARM_UI,
                    ServerSource.HORDE -> {
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = stringResource(
                                id = LocalizationR.string.hint_sub_seed_strength,
                                "${state.subSeedStrength.roundTo(2)}",
                            ),
                        )
                        SliderTextInputField(
                            value = state.subSeedStrength,
                            valueRange = SUB_SEED_STRENGTH_MIN..SUB_SEED_STRENGTH_MAX,
                            valueDiff = 0.01f,
                            sliderColors = sliderColors,
                            onValueChange = {
                                processIntent(GenerationMviIntent.Update.SubSeedStrength(it))
                            },
                        )
                    }

                    else -> Unit
                }

                //Steps not available for open ai
                if (state.mode != ServerSource.OPEN_AI) {
                    val stepsMax = when (state.mode) {
                        ServerSource.LOCAL_MICROSOFT_ONNX -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> SAMPLING_STEPS_LOCAL_DIFFUSION_MAX
                        ServerSource.STABILITY_AI -> SAMPLING_STEPS_RANGE_STABILITY_AI_MAX
                        else -> SAMPLING_STEPS_RANGE_MAX
                    }
                    val steps = state.samplingSteps.coerceIn(SAMPLING_STEPS_RANGE_MIN, stepsMax)
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = LocalizationR.string.hint_sampling_steps, "$steps"),
                    )
                    SliderTextInputField(
                        value = steps * 1f,
                        valueRange = (SAMPLING_STEPS_RANGE_MIN * 1f)..(stepsMax * 1f),
                        valueDiff = 1f,
                        steps = abs(stepsMax - SAMPLING_STEPS_RANGE_MIN) - 1,
                        sliderColors = sliderColors,
                        fractionDigits = 0,
                        onValueChange = {
                            processIntent(GenerationMviIntent.Update.SamplingSteps(it.roundToInt()))
                        },
                    )
                }

                // CFG scale not available on open ai and google media pipe
                // For Flux models, show Distilled CFG Scale instead
                when (state.mode) {
                    ServerSource.OPEN_AI,
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> Unit
                    ServerSource.AUTOMATIC1111 -> {
                        // CFG Scale for all model types
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = stringResource(
                                LocalizationR.string.hint_cfg_scale,
                                "${state.cfgScale.roundTo(2)}",
                            ),
                        )
                        SliderTextInputField(
                            value = state.cfgScale,
                            valueRange = (CFG_SCALE_RANGE_MIN * 1f)..(CFG_SCALE_RANGE_MAX * 1f),
                            valueDiff = 0.5f,
                            steps = abs(CFG_SCALE_RANGE_MAX - CFG_SCALE_RANGE_MIN) * 2 - 1,
                            sliderColors = sliderColors,
                            onValueChange = {
                                processIntent(GenerationMviIntent.Update.CfgScale(it))
                            },
                        )
                        // Flux also needs Distilled CFG Scale
                        if (state.modelType == ModelType.FLUX) {
                            Text(
                                modifier = Modifier.padding(top = 8.dp),
                                text = stringResource(
                                    LocalizationR.string.hint_distilled_cfg_scale,
                                    "${state.distilledCfgScale.roundTo(2)}",
                                ),
                            )
                            SliderTextInputField(
                                value = state.distilledCfgScale,
                                valueRange = 1f..10f,
                                valueDiff = 0.5f,
                                steps = 17,
                                sliderColors = sliderColors,
                                onValueChange = {
                                    processIntent(GenerationMviIntent.Update.DistilledCfgScale(it))
                                },
                            )
                        }
                    }
                    else -> {
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = stringResource(
                                LocalizationR.string.hint_cfg_scale,
                                "${state.cfgScale.roundTo(2)}",
                            ),
                        )
                        SliderTextInputField(
                            value = state.cfgScale,
                            valueRange = (CFG_SCALE_RANGE_MIN * 1f)..(CFG_SCALE_RANGE_MAX * 1f),
                            valueDiff = 0.5f,
                            steps = abs(CFG_SCALE_RANGE_MAX - CFG_SCALE_RANGE_MIN) * 2 - 1,
                            sliderColors = sliderColors,
                            onValueChange = {
                                processIntent(GenerationMviIntent.Update.CfgScale(it))
                            },
                        )
                    }
                }

                when (state.mode) {
                    ServerSource.AUTOMATIC1111,
                    ServerSource.SWARM_UI,
                    ServerSource.STABILITY_AI,
                    ServerSource.HORDE,
                    ServerSource.LOCAL_QUALCOMM_QNN -> afterSlidersSection()

                    else -> Unit
                }

                // Batch is not available for any Local
                when (state.mode) {
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE, ServerSource.LOCAL_MICROSOFT_ONNX -> Unit
                    else -> batchComponent()
                }
                // Hires, ADetailer, Restore faces - only for A1111
                if (state.mode == ServerSource.AUTOMATIC1111) {
                    HiresSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        config = state.hiresConfig,
                        onConfigChange = { processIntent(GenerationMviIntent.Update.Hires(it)) },
                    )
                    ADetailerSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        config = state.aDetailerConfig,
                        onConfigChange = { processIntent(GenerationMviIntent.Update.ADetailer(it)) },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(id = LocalizationR.string.hint_restore_faces),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(
                            checked = state.restoreFaces,
                            onCheckedChange = {
                                processIntent(GenerationMviIntent.Update.RestoreFaces(it))
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun processTaggedPrompt(keywords: List<String>, event: ChipTextFieldEvent<String>): String {
    val newKeywords = when (event) {
        is ChipTextFieldEvent.Add -> buildList {
            addAll(keywords)
            add(event.item)
        }

        is ChipTextFieldEvent.AddBatch -> buildList {
            addAll(keywords)
            addAll(event.items)
        }

        is ChipTextFieldEvent.Remove -> keywords.filterIndexed { i, _ -> i != event.index }
        is ChipTextFieldEvent.Update -> keywords.mapIndexed { i, s -> if (i == event.index) event.item else s }
    }
    return newKeywords.joinToString(", ")
}
