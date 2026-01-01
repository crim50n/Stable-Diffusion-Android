@file:OptIn(ExperimentalMaterial3Api::class)

package dev.minios.pdaiv1.presentation.widget.falai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.domain.entity.FalAiPropertyType
import dev.minios.pdaiv1.presentation.model.FalAiPropertyUi
import dev.minios.pdaiv1.presentation.theme.textFieldColors
import dev.minios.pdaiv1.presentation.utils.uriToBitmap
import dev.minios.pdaiv1.presentation.widget.input.DropdownTextField
import dev.minios.pdaiv1.presentation.widget.input.SliderTextInputField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val CUSTOM_SIZE_OPTION = "custom"

@Composable
fun FalAiPropertyField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        when (property.type) {
            FalAiPropertyType.STRING -> FalAiStringField(property, onValueChange)
            FalAiPropertyType.INTEGER -> FalAiIntegerField(property, onValueChange)
            FalAiPropertyType.NUMBER -> FalAiNumberField(property, onValueChange)
            FalAiPropertyType.BOOLEAN -> FalAiBooleanField(property, onValueChange)
            FalAiPropertyType.ENUM -> FalAiEnumField(property, onValueChange)
            FalAiPropertyType.IMAGE_URL -> FalAiImageUrlField(property, onValueChange)
            FalAiPropertyType.IMAGE_URL_ARRAY -> FalAiImageUrlArrayField(property, onValueChange)
            FalAiPropertyType.IMAGE_SIZE -> FalAiImageSizeField(property, onValueChange)
            FalAiPropertyType.ARRAY -> {
                if (property.arrayItemProperties != null) {
                    FalAiArrayField(property, onValueChange)
                } else {
                    // Fallback to text field for unsupported array types
                    FalAiStringField(property, onValueChange)
                }
            }
            FalAiPropertyType.OBJECT -> {
                // For now, show as text field for JSON input
                FalAiStringField(property, onValueChange)
            }
            FalAiPropertyType.INPAINT -> {
                // INPAINT is handled separately in FalAiDynamicForm with FalAiInpaintField
                // This fallback shows a simple image picker if used standalone
                FalAiImageUrlField(property, onValueChange)
            }
        }
        if (property.description.isNotBlank()) {
            Text(
                text = property.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun FalAiStringField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    var text by remember { mutableStateOf(property.currentValue?.toString() ?: "") }

    LaunchedEffect(property.currentValue) {
        text = property.currentValue?.toString() ?: ""
    }

    val isPromptField = property.name == "prompt"

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = text,
        onValueChange = { newValue ->
            text = newValue
            onValueChange(newValue)
        },
        label = { Text(property.title) },
        minLines = if (isPromptField) 3 else 1,
        maxLines = if (isPromptField) 6 else 3,
        singleLine = !isPromptField,
        keyboardOptions = KeyboardOptions(
            imeAction = if (isPromptField) ImeAction.Default else ImeAction.Done,
        ),
        colors = textFieldColors,
    )
}

@Composable
private fun FalAiIntegerField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    val min = property.minimum ?: 0.0
    val max = property.maximum ?: 100.0
    val hasRange = property.minimum != null && property.maximum != null

    if (hasRange) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = property.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            SliderTextInputField(
                value = (property.currentValue as? Number)?.toFloat() ?: min.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = min.toFloat()..max.toFloat(),
                fractionDigits = 0,
            )
        }
    } else {
        var text by remember { mutableStateOf(property.currentValue?.toString() ?: "") }

        LaunchedEffect(property.currentValue) {
            text = property.currentValue?.toString() ?: ""
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { newValue ->
                text = newValue
                newValue.toIntOrNull()?.let { onValueChange(it) }
            },
            label = { Text(property.title) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            colors = textFieldColors,
        )
    }
}

@Composable
private fun FalAiNumberField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    val min = property.minimum ?: 0.0
    val max = property.maximum ?: 100.0
    val hasRange = property.minimum != null && property.maximum != null

    if (hasRange) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = property.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            SliderTextInputField(
                value = (property.currentValue as? Number)?.toDouble() ?: min,
                onValueChange = { onValueChange(it) },
                valueRange = min..max,
                fractionDigits = 2,
            )
        }
    } else {
        var text by remember { mutableStateOf(property.currentValue?.toString() ?: "") }

        LaunchedEffect(property.currentValue) {
            text = property.currentValue?.toString() ?: ""
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { newValue ->
                text = newValue
                newValue.toDoubleOrNull()?.let { onValueChange(it) }
            },
            label = { Text(property.title) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            colors = textFieldColors,
        )
    }
}

@Composable
private fun FalAiBooleanField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = property.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = property.currentValue as? Boolean ?: false,
            onCheckedChange = { onValueChange(it) },
        )
    }
}

@Composable
private fun FalAiEnumField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    DropdownTextField(
        modifier = Modifier.fillMaxWidth(),
        label = property.title.asUiText(),
        value = property.currentValue?.toString() ?: property.enumValues.firstOrNull(),
        items = property.enumValues,
        onItemSelected = { onValueChange(it) },
    )
}

@Composable
private fun FalAiImageUrlField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf(property.currentValue?.toString() ?: "") }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(property.currentValue) {
        val newUrl = property.currentValue?.toString() ?: ""
        imageUrl = newUrl
        if (newUrl.startsWith("data:image") && previewBitmap == null) {
            withContext(Dispatchers.IO) {
                val bitmap = base64DataUriToBitmap(newUrl)
                withContext(Dispatchers.Main) {
                    previewBitmap = bitmap
                }
            }
        } else if (newUrl.isBlank()) {
            previewBitmap = null
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val bitmap = uri?.let { uriToBitmap(context, it) } ?: return@rememberLauncherForActivityResult
        previewBitmap = bitmap
        // Convert to base64 data URI
        val base64 = bitmapToBase64DataUri(bitmap)
        imageUrl = base64
        onValueChange(base64)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = property.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Show preview if we have an image (compact row layout like multi-image)
        if (previewBitmap != null || imageUrl.startsWith("data:image") || imageUrl.startsWith("http")) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Image preview (compact)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp),
                        ),
                ) {
                    previewBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop,
                        )
                    } ?: run {
                        // For URL-based images, just show icon
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Image info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = if (imageUrl.startsWith("data:image")) "Local image" else imageUrl.take(30) + if (imageUrl.length > 30) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Remove button
                IconButton(
                    onClick = {
                        previewBitmap = null
                        imageUrl = ""
                        onValueChange("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove image",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Change image button
            OutlinedButton(
                onClick = {
                    mediaPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Change Image")
            }
        } else {
            // Add image button (like multi-image)
            OutlinedButton(
                onClick = {
                    mediaPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Add Image")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // URL input field (for pasting URLs)
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = if (imageUrl.startsWith("data:image")) "" else imageUrl,
            onValueChange = { newValue ->
                if (!newValue.startsWith("data:image")) {
                    imageUrl = newValue
                    previewBitmap = null
                    onValueChange(newValue)
                }
            },
            label = { Text("Or paste image URL") },
            placeholder = { Text("https://...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            colors = textFieldColors,
        )
    }
}

/**
 * Converts a Bitmap to a base64 data URI string.
 */
private fun bitmapToBase64DataUri(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    return "data:image/png;base64,$base64"
}

/**
 * Field for an array of image URLs (e.g., image_urls for flux-2/edit).
 * Supports adding multiple images via picker or URL input.
 */
@Composable
private fun FalAiImageUrlArrayField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    val context = LocalContext.current

    // Parse current value as list of strings
    @Suppress("UNCHECKED_CAST")
    val initialImages = when (val current = property.currentValue) {
        is List<*> -> current.filterIsInstance<String>().toMutableList()
        else -> mutableListOf()
    }

    var images by remember { mutableStateOf(initialImages) }
    var bitmaps by remember { mutableStateOf<Map<Int, Bitmap>>(emptyMap()) }

    LaunchedEffect(property.currentValue) {
        @Suppress("UNCHECKED_CAST")
        val newImages = when (val current = property.currentValue) {
            is List<*> -> current.filterIsInstance<String>().toMutableList()
            else -> mutableListOf()
        }
        if (newImages != images) {
            images = newImages
        }

        // Restore bitmaps if missing or out of sync
        if (bitmaps.size != newImages.size || bitmaps.keys.any { it >= newImages.size }) {
            withContext(Dispatchers.IO) {
                val newBitmaps = mutableMapOf<Int, Bitmap>()
                newImages.forEachIndexed { index, url ->
                    if (url.startsWith("data:image")) {
                        base64DataUriToBitmap(url)?.let {
                            newBitmaps[index] = it
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    bitmaps = newBitmaps
                }
            }
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4),
    ) { uris ->
        val newBitmaps = mutableMapOf<Int, Bitmap>()
        val newUrls = uris.mapNotNull { uri ->
            uriToBitmap(context, uri)?.let { bitmap ->
                val index = images.size + newBitmaps.size
                newBitmaps[index] = bitmap
                bitmapToBase64DataUri(bitmap)
            }
        }
        if (newUrls.isNotEmpty()) {
            bitmaps = bitmaps + newBitmaps
            images = (images + newUrls).toMutableList()
            onValueChange(images)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = property.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Show existing images in a grid-like layout
        if (images.isNotEmpty()) {
            Column {
                images.forEachIndexed { index, imageUrl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Image preview
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp),
                                ),
                        ) {
                            bitmaps[index]?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Image ${index + 1}",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop,
                                )
                            } ?: run {
                                // For URL-based images, just show icon
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Image info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Image ${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = if (imageUrl.startsWith("data:image")) "Local image" else imageUrl.take(30) + "...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Remove button
                        IconButton(
                            onClick = {
                                bitmaps = bitmaps - index
                                images = images.toMutableList().apply { removeAt(index) }
                                onValueChange(images)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add image button
        if (images.size < 4) {
            OutlinedButton(
                onClick = {
                    mediaPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Add Image${if (images.isEmpty()) "s" else ""} (${images.size}/4)")
            }
        } else {
            Text(
                text = "Maximum 4 images reached",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun FalAiImageSizeField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    // Determine initial state from currentValue
    val initialValue = property.currentValue
    val initialIsCustom = initialValue is Map<*, *>
    val initialPreset = if (initialIsCustom) {
        CUSTOM_SIZE_OPTION
    } else {
        initialValue?.toString() ?: property.defaultValue?.toString() ?: property.enumValues.firstOrNull() ?: ""
    }
    val initialWidth = if (initialValue is Map<*, *>) {
        (initialValue["width"] as? Number)?.toInt() ?: 1024
    } else 1024
    val initialHeight = if (initialValue is Map<*, *>) {
        (initialValue["height"] as? Number)?.toInt() ?: 1024
    } else 1024

    var selectedPreset by remember { mutableStateOf(initialPreset) }
    var isCustom by remember { mutableStateOf(initialIsCustom) }
    var width by remember { mutableIntStateOf(initialWidth) }
    var height by remember { mutableIntStateOf(initialHeight) }

    // Build dropdown options: presets + custom
    val allOptions = remember(property.enumValues) {
        property.enumValues + CUSTOM_SIZE_OPTION
    }

    // Format preset names for display
    val formatPresetName: (String) -> String = { preset ->
        when (preset) {
            CUSTOM_SIZE_OPTION -> "Custom"
            else -> preset
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = property.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Preset dropdown
        DropdownTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Size preset".asUiText(),
            value = selectedPreset,
            items = allOptions,
            onItemSelected = { preset ->
                selectedPreset = preset
                isCustom = preset == CUSTOM_SIZE_OPTION
                if (isCustom) {
                    onValueChange(mapOf("width" to width, "height" to height))
                } else {
                    onValueChange(preset)
                }
            },
            displayDelegate = { formatPresetName(it).asUiText() },
        )

        // Custom dimensions inputs (shown when custom is selected)
        AnimatedVisibility(
            visible = isCustom,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Width input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Width",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = width.toString(),
                            onValueChange = { newValue ->
                                newValue.toIntOrNull()?.let { w ->
                                    width = w.coerceIn(1, 14142)
                                    onValueChange(mapOf("width" to width, "height" to height))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                            colors = textFieldColors,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Height input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Height",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = height.toString(),
                            onValueChange = { newValue ->
                                newValue.toIntOrNull()?.let { h ->
                                    height = h.coerceIn(1, 14142)
                                    onValueChange(mapOf("width" to width, "height" to height))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            colors = textFieldColors,
                        )
                    }
                }

                Text(
                    text = "Range: 1 - 14142 pixels",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FalAiArrayField(
    property: FalAiPropertyUi,
    onValueChange: (Any?) -> Unit,
) {
    val itemProperties = property.arrayItemProperties ?: return

    // Parse current value as list of maps
    @Suppress("UNCHECKED_CAST")
    val initialItems = when (val current = property.currentValue) {
        is List<*> -> current.filterIsInstance<Map<String, Any?>>().toMutableList()
        else -> mutableListOf()
    }

    var items by remember { mutableStateOf(initialItems) }

    // Update items when currentValue changes externally
    LaunchedEffect(property.currentValue) {
        @Suppress("UNCHECKED_CAST")
        val newItems = when (val current = property.currentValue) {
            is List<*> -> current.filterIsInstance<Map<String, Any?>>().toMutableList()
            else -> mutableListOf()
        }
        if (newItems != items) {
            items = newItems
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header with title and add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = property.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = {
                    // Create new item with default values
                    val newItem = itemProperties.associate { prop ->
                        prop.name to prop.defaultValue
                    }
                    items = (items + newItem).toMutableList()
                    onValueChange(items)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(text = "Add ${if (property.name == "loras") "LoRA" else "Item"}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of items
        items.forEachIndexed { index, item ->
            FalAiArrayItem(
                index = index,
                item = item,
                itemProperties = itemProperties,
                isLora = property.name == "loras",
                onItemChange = { updatedItem ->
                    items = items.toMutableList().apply {
                        set(index, updatedItem)
                    }
                    onValueChange(items)
                },
                onRemove = {
                    items = items.toMutableList().apply {
                        removeAt(index)
                    }
                    onValueChange(items)
                },
            )
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (items.isEmpty()) {
            Text(
                text = "No items added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun FalAiArrayItem(
    index: Int,
    item: Map<String, Any?>,
    itemProperties: List<FalAiPropertyUi>,
    isLora: Boolean,
    onItemChange: (Map<String, Any?>) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
    ) {
        // Item header with number and delete button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isLora) "LoRA ${index + 1}" else "Item ${index + 1}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        // Render each property in the item
        itemProperties.forEach { prop ->
            val currentValue = item[prop.name]
            val propertyWithValue = prop.copy(currentValue = currentValue)

            when (prop.type) {
                FalAiPropertyType.STRING -> {
                    FalAiStringField(propertyWithValue) { newValue ->
                        onItemChange(item + (prop.name to newValue))
                    }
                }
                FalAiPropertyType.NUMBER -> {
                    FalAiNumberField(propertyWithValue) { newValue ->
                        onItemChange(item + (prop.name to newValue))
                    }
                }
                FalAiPropertyType.INTEGER -> {
                    FalAiIntegerField(propertyWithValue) { newValue ->
                        onItemChange(item + (prop.name to newValue))
                    }
                }
                FalAiPropertyType.BOOLEAN -> {
                    FalAiBooleanField(propertyWithValue) { newValue ->
                        onItemChange(item + (prop.name to newValue))
                    }
                }
                FalAiPropertyType.ENUM -> {
                    FalAiEnumField(propertyWithValue) { newValue ->
                        onItemChange(item + (prop.name to newValue))
                    }
                }
                else -> {
                    // Default to string field for unsupported types
                    FalAiStringField(propertyWithValue) { newValue ->
                        onItemChange(item + (prop.name to newValue))
                    }
                }
            }
        }
    }
}

private fun base64DataUriToBitmap(dataUri: String): Bitmap? {
    try {
        val base64Image = dataUri.substringAfter("base64,")
        val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

