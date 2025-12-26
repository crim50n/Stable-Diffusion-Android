@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.widget.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.theme.textFieldColors

@Composable
fun <T : Any> MultiSelectDropdownField(
    modifier: Modifier = Modifier,
    label: UiText = UiText.empty,
    selectedItems: List<T> = emptyList(),
    availableItems: List<T> = emptyList(),
    onSelectionChanged: (List<T>) -> Unit = {},
    displayDelegate: (T) -> UiText = { t -> t.toString().asUiText() },
) {
    var expanded by remember { mutableStateOf(false) }
    val unselectedItems = availableItems.filter { it !in selectedItems }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = if (selectedItems.isEmpty()) "" else "${selectedItems.size} selected",
                onValueChange = {},
                readOnly = true,
                label = { Text(label.asString()) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = textFieldColors,
            )

            if (unselectedItems.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    unselectedItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(displayDelegate(item).asString()) },
                            onClick = {
                                onSelectionChanged(selectedItems + item)
                            },
                        )
                    }
                }
            }
        }

        if (selectedItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                selectedItems.forEach { item ->
                    InputChip(
                        modifier = Modifier.padding(end = 4.dp),
                        selected = false,
                        onClick = { onSelectionChanged(selectedItems - item) },
                        label = { Text(displayDelegate(item).asString()) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                            )
                        },
                    )
                }
            }
        }
    }
}
