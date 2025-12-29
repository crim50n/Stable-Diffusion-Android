package com.shifthackz.aisdv1.presentation.widget.falai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.model.FalAiPropertyUi
import com.shifthackz.aisdv1.presentation.model.InPaintModel
import com.shifthackz.aisdv1.presentation.screen.inpaint.components.InPaintComponent
import com.shifthackz.aisdv1.presentation.utils.uriToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Combined image + mask drawing field for inpainting endpoints.
 * Shows the image and allows drawing a mask on top.
 */
@Composable
fun FalAiInpaintField(
    property: FalAiPropertyUi,
    currentImageValue: Any?,
    currentMaskValue: Any?,
    onImageChange: (String) -> Unit,
    onMaskChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var inPaintModel by remember { mutableStateOf(InPaintModel()) }
    var brushSize by remember { mutableFloatStateOf(24f) }

    // Restore image from currentValue if it's a base64 string
    LaunchedEffect(currentImageValue) {
        val imageUrl = currentImageValue?.toString() ?: ""
        if (imageUrl.startsWith("data:image") && imageBitmap == null) {
            withContext(Dispatchers.IO) {
                val bitmap = base64DataUriToBitmap(imageUrl)
                withContext(Dispatchers.Main) {
                    imageBitmap = bitmap
                }
            }
        } else if (imageUrl.isBlank()) {
            imageBitmap = null
            inPaintModel = InPaintModel()
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val bitmap = uri?.let { uriToBitmap(context, it) } ?: return@rememberLauncherForActivityResult
        imageBitmap = bitmap
        // Clear existing mask when new image is selected
        inPaintModel = InPaintModel()
        onMaskChange("")
        // Convert to base64 data URI
        val base64 = bitmapToBase64DataUri(bitmap)
        onImageChange(base64)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = property.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (property.description.isNotBlank()) {
            Text(
                text = property.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (imageBitmap != null) {
            // Show inpainting canvas
            InPaintComponent(
                modifier = Modifier.fillMaxWidth(),
                drawMode = true,
                inPaint = inPaintModel,
                bitmap = imageBitmap,
                capWidth = brushSize.toInt(),
                onPathDrawn = { path ->
                    inPaintModel = inPaintModel.copy(
                        paths = inPaintModel.paths + (path to brushSize.toInt())
                    )
                },
                onPathBitmapDrawn = { maskBitmap ->
                    if (maskBitmap != null) {
                        val maskBase64 = bitmapToBase64DataUri(maskBitmap)
                        onMaskChange(maskBase64)
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Brush size slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Brush size: ${brushSize.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 4f..64f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = {
                        if (inPaintModel.paths.isNotEmpty()) {
                            inPaintModel = inPaintModel.copy(
                                paths = inPaintModel.paths.dropLast(1)
                            )
                        }
                    },
                    enabled = inPaintModel.paths.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "Undo",
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        inPaintModel = InPaintModel()
                        onMaskChange("")
                    },
                    enabled = inPaintModel.paths.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clear",
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "Clear",
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
            // No image selected - show picker button
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
                Text("Select Image for Inpainting")
            }
        }
    }
}

private fun bitmapToBase64DataUri(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    return "data:image/png;base64,$base64"
}

private fun base64DataUriToBitmap(dataUri: String): Bitmap? {
    return try {
        val base64Image = dataUri.substringAfter("base64,")
        val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
