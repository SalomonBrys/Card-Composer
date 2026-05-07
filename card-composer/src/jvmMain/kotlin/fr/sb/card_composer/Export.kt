package fr.sb.card_composer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import fr.sb.card_composer.export.Exporter
import fr.sb.card_composer.export.PdfExporter
import fr.sb.card_composer.export.PngExporter
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun Export(
    cards: List<Card>,
) {
    val selected = remember { mutableStateSetOf(*cards.toTypedArray()) }

    Surface(Modifier.fillMaxSize()) {
        var progress: Exporter.Progress? by remember { mutableStateOf(null) }
        Box {
            val lazyListState = rememberLazyListState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .width(192.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        var exporter: Exporter by remember { mutableStateOf(PngExporter) }
                        var exporterExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = exporterExpanded,
                            onExpandedChange = { exporterExpanded = it },
                            modifier = Modifier.width(120.dp)
                        ) {
                            OutlinedTextField(
                                value = exporter.name,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exporterExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .pointerHoverIcon(PointerIcon.Default, true)
                            )
                            ExposedDropdownMenu(
                                expanded = exporterExpanded,
                                onDismissRequest = { exporterExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(PngExporter.name) },
                                    onClick = {
                                        exporter = PngExporter
                                        exporterExpanded = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(PdfExporter.name) },
                                    onClick = {
                                        exporter = PdfExporter
                                        exporterExpanded = false
                                    },
                                )
                            }
                        }
                        with(exporter) { Config() }

                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                scope.launch {
                                    progress = Exporter.Progress.Initial
                                    exporter.export(cards.filter { it in selected })
                                        .collect { progress = it }
                                    progress = null
                                }
                            },
                            enabled = selected.isNotEmpty(),
                        ) {
                            Text("Export")
                        }
                    }
                }
                val groups = remember(cards) { cards.groupByTo(LinkedHashMap()) { it.group ?: "" } }
                val expandedGroups = remember { mutableStateSetOf(*groups.keys.toTypedArray()) }
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TriStateCheckbox(
                                    state = when {
                                        selected.isEmpty() -> ToggleableState.Off
                                        selected.size == cards.size -> ToggleableState.On
                                        else -> ToggleableState.Indeterminate
                                    },
                                    onClick = {
                                        if (selected.isEmpty()) {
                                            selected.addAll(cards)
                                        } else {
                                            selected.clear()
                                        }
                                    }
                                )
                                Text("Select all", Modifier.weight(1f))
                                IconButton(
                                    onClick = { expandedGroups.addAll(groups.keys) }
                                ) {
                                    Icon(Icons.Default.Expand, "Expand all")
                                }
                                IconButton(
                                    onClick = { expandedGroups.clear() }
                                ) {
                                    Icon(Icons.Default.Compress, "Compress all")
                                }
                            }
                        }
                    }
                    groups.forEach { (groupName, group) ->
                        if (groups.size > 1) {
                            stickyHeader {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceBright,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (groupName in expandedGroups) {
                                                    expandedGroups.remove(groupName)
                                                } else {
                                                    expandedGroups.add(groupName)
                                                }
                                            }
                                        ) {
                                            if (groupName in expandedGroups) {
                                                Icon(Icons.Filled.ExpandLess, null)
                                            } else {
                                                Icon(Icons.Filled.ExpandMore, null)
                                            }
                                        }
                                        val groupSelected = selected.filter { it.group == groupName }
                                        TriStateCheckbox(
                                            state = when {
                                                groupSelected.isEmpty() -> ToggleableState.Off
                                                groupSelected.size == group.size -> ToggleableState.On
                                                else -> ToggleableState.Indeterminate
                                            },
                                            onClick = {
                                                if (groupSelected.isEmpty()) {
                                                    selected.addAll(group)
                                                } else {
                                                    selected.removeAll(group)
                                                }
                                            }
                                        )
                                        Text(
                                            groupName,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                }
                            }
                        }
                        if (groupName in expandedGroups) {
                            items(group) { card ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                ) {
                                    Checkbox(
                                        checked = card in selected,
                                        onCheckedChange = {
                                            if (it) {
                                                selected.add(card)
                                            } else {
                                                selected.remove(card)
                                            }
                                        },
                                    )
                                    Text(card.name)
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(lazyListState),
                style = LocalScrollbarStyle.current.copy(
                    unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            )
        }

        if (progress != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .onClick {}
            ) {
                CircularProgressIndicator(
                    progress = { if (progress == null || progress == Exporter.Progress.Initial) 0f else progress!!.progress() },
                    strokeWidth = 8.dp,
                    modifier = Modifier
                        .size(128.dp)
                )
            }
        }
    }

}

