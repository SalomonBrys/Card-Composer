package fr.sb.card_composer

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberWindowState
import fr.sb.card_composer.theme.AppDarkScheme
import fr.sb.card_composer.theme.AppLightScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow


public enum class ComposerTheme {
    Light,
    Dark,
}

public enum class ComposerKeyEvent {
    Next, Previous,
}

public fun MutableSharedFlow<ComposerKeyEvent>.keyEventHandler(): (KeyEvent) -> Boolean = {
    if (it.type == KeyEventType.KeyDown) {
        when (it.key) {
            Key.DirectionRight, Key.DirectionDown -> {
                tryEmit(ComposerKeyEvent.Next)
                true
            }
            Key.DirectionLeft, Key.DirectionUp -> {
                tryEmit(ComposerKeyEvent.Previous)
                true
            }
            else -> false
        }
    } else false
}

public fun cardComposerApplication(
    title: String,
    cards: @Composable () -> List<Card>,
): Unit = application {
    val keyEventFlow = remember { MutableSharedFlow<ComposerKeyEvent>(1) }

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1400.dp, height = 800.dp),
        title = title,
        onKeyEvent = keyEventFlow.keyEventHandler()
    ) {
        CardComposer(
            cards = cards(),
            keyEvents = keyEventFlow,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun CardComposer(
    cards: List<Card>,
    keyEvents: Flow<ComposerKeyEvent>,
    defaultTheme: ComposerTheme = ComposerTheme.Dark,
) {
    remember(cards) {
        val distinctNames = cards.mapTo(HashSet()) { it.fullName }
        if (distinctNames.size != cards.size) {
            val duplicate = distinctNames.first { name -> cards.count { it.fullName == name } > 1 }
            error("Duplicate card name '$duplicate'")
        }
    }

    val groups = remember(cards) { cards.groupByTo(LinkedHashMap()) { it.group ?: "" } }

    var theme by remember { mutableStateOf(defaultTheme) }
    MaterialTheme(
        colorScheme = when (theme) {
            ComposerTheme.Light -> AppLightScheme
            ComposerTheme.Dark -> AppDarkScheme
        }
    ) {
        var showExportDialog by remember { mutableStateOf(false) }
        Surface {
            Column(
                Modifier
                    .fillMaxSize()
            ) {
                var cardGroup by remember { mutableStateOf("") }
                var cardIndex by remember { mutableStateOf(0) }

                val cardList = remember(cards, cardGroup) {
                    if (cardGroup == "") cards
                    else groups[cardGroup]!!
                }

                val updatedCardList by rememberUpdatedState(cardList)
                LaunchedEffect(keyEvents) {
                    keyEvents.collect {
                        cardIndex = when (it) {
                            ComposerKeyEvent.Next -> (cardIndex + 1).coerceAtMost(updatedCardList.lastIndex)
                            ComposerKeyEvent.Previous -> (cardIndex - 1).coerceAtLeast(0)
                        }
                    }
                }

                val (hideBleed, setHideBleed) = remember { mutableStateOf(false) }
                val (showSafeArea, setShowSafeArea) = remember { mutableStateOf(true) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val horizontalScrollState = rememberScrollState()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        IconButton(
                            onClick = { theme = if (theme == ComposerTheme.Light) ComposerTheme.Dark else ComposerTheme.Light },
                        ) {
                            Icon(
                                when (theme) {
                                    ComposerTheme.Light -> Icons.Filled.DarkMode
                                    ComposerTheme.Dark -> Icons.Filled.LightMode
                                },
                                null
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = hideBleed,
                                onCheckedChange = setHideBleed
                            )
                            Text("Hide bleed")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = showSafeArea,
                                onCheckedChange = setShowSafeArea
                            )
                            Text("Show safe area")
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    cardIndex = (cardIndex - 1).coerceAtLeast(0)
                                },
                                enabled = cardIndex > 0,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateBefore, null)
                            }
                            var groupsExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = groupsExpanded,
                                onExpandedChange = { groupsExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = cardGroup.takeIf(String::isNotEmpty) ?: "All",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupsExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                        .width(192.dp)
                                        .pointerHoverIcon(PointerIcon.Default, true)
                                )
                                ExposedDropdownMenu(
                                    expanded = groupsExpanded,
                                    onDismissRequest = { groupsExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All") },
                                        onClick = {
                                            cardIndex = 0
                                            cardGroup = ""
                                            groupsExpanded = false
                                        },
                                    )
                                    HorizontalDivider()
                                    groups.keys.forEach { groupName ->
                                        DropdownMenuItem(
                                            text = { Text(groupName) },
                                            onClick = {
                                                cardIndex = 0
                                                cardGroup = groupName
                                                groupsExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                            var cardsExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = cardsExpanded,
                                onExpandedChange = { cardsExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = cardList[cardIndex].let { if (cardGroup == "") it.fullName else it.name },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardsExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                        .width(384.dp)
                                        .pointerHoverIcon(PointerIcon.Default, true)
                                )
                                ExposedDropdownMenu(
                                    expanded = cardsExpanded,
                                    onDismissRequest = { cardsExpanded = false },
                                ) {
                                    cardList.forEachIndexed { index, card ->
                                        DropdownMenuItem(
                                            text = { Text(card.let { if (cardGroup == "") it.fullName else it.name }) },
                                            onClick = {
                                                cardIndex = index
                                                cardsExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    cardIndex = (cardIndex + 1).coerceAtMost(cardList.lastIndex)
                                },
                                enabled = cardIndex < cardList.lastIndex,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
                            }
                        }
                        IconButton(
                            onClick = { showExportDialog = true },
                        ) {
                            Icon(Icons.Filled.SaveAlt, null)
                        }
                    }
                    HorizontalScrollbar(
                        adapter = rememberScrollbarAdapter(horizontalScrollState),
                        style = LocalScrollbarStyle.current.copy(
                            unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                }
                Row {
                    CardPreview(
                        face = cardList[cardIndex].frontFace,
                        hideBleed = hideBleed,
                        showSafeArea = showSafeArea,
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    CardPreview(
                        face = cardList[cardIndex].backFace,
                        hideBleed = hideBleed,
                        showSafeArea = showSafeArea,
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                }
            }
        }

        if (showExportDialog) {
            DialogWindow(
                onCloseRequest = { showExportDialog = false },
                state = rememberDialogState(width = 500.dp, height = 500.dp),
                title = "Export",
            ) {
                Export(
                    cards = cards
                )
            }
        }
    }
}

