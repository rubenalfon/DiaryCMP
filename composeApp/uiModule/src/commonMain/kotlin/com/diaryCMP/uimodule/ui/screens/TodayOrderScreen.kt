package com.diaryCMP.uimodule.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.diaryCMP.uimodule.ui.composables.ComponentByType
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorLoading
import com.diaryCMP.uimodule.ui.composables.GenericAlertErrorSaving
import com.diaryCMP.uimodule.ui.composables.GenericDialog
import com.diaryCMP.uimodule.ui.composables.GenericPlainTooltip
import com.diaryCMP.uimodule.ui.composables.LoadingWrapper
import com.diaryCMP.uimodule.ui.composables.ResponsiveScreenHeader
import com.diaryCMP.uimodule.ui.theme.halfListSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.interiorCardPadding
import com.diaryCMP.uimodule.ui.theme.listSpacedByPadding
import com.diaryCMP.uimodule.ui.theme.mediumPadding
import com.diaryCMP.uimodule.ui.theme.pageListHorizontalPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import com.diaryCMP.uimodule.ui.theme.surfaceToWindowPadding
import com.diaryCMP.uimodule.ui.theme.widthRightSupportPane
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.add_component
import diary.composeapp.utilsmodule.generated.resources.are_you_sure
import diary.composeapp.utilsmodule.generated.resources.components_order
import diary.composeapp.utilsmodule.generated.resources.delete_component
import diary.composeapp.utilsmodule.generated.resources.delete_component_body
import diary.composeapp.utilsmodule.generated.resources.move_down_component
import diary.composeapp.utilsmodule.generated.resources.move_up_component
import diary.composeapp.utilsmodule.generated.resources.no_rename
import diary.composeapp.utilsmodule.generated.resources.okey
import diary.composeapp.utilsmodule.generated.resources.reorder_component
import diary.composeapp.utilsmodule.generated.resources.today
import diary.composeapp.utilsmodule.generated.resources.tutorial_today_order_body
import diary.composeapp.utilsmodule.generated.resources.tutorial_today_order_body_addon_screen_large
import diary.composeapp.utilsmodule.generated.resources.tutorial_today_order_body_addon_screen_not_large
import diary.composeapp.utilsmodule.generated.resources.tutorial_today_order_title
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.utilsModule.utils.KeyboardAware
import es.diaryCMP.utilsModule.utils.Platform
import es.diaryCMP.utilsModule.utils.ScreenClass
import es.diaryCMP.utilsModule.utils.TextFieldValueSaver
import es.diaryCMP.utilsModule.utils.getPlatform
import es.diaryCMP.utilsModule.utils.getScreenClass
import es.diaryCMP.utilsModule.utils.keyboardDismissOnSwipeIOS
import es.diaryCMP.viewmodelsModule.viewmodels.TodayOrderScreenViewModel
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn

@Composable
fun TodayOrderScreen(
    modifier: Modifier = Modifier, popBackStack: () -> Unit, viewModel: TodayOrderScreenViewModel
) {
    LaunchedEffect(viewModel) {
        viewModel.fakeInit()
    }
    val components by viewModel.components.collectAsState()
    val isAddingComponents by viewModel.isAddingComponents.collectAsState()
    val errorSaving by viewModel.errorSaving.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorLoading by viewModel.errorLoading.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()

    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        val animatedColor by animateColorAsState(if (scrollState.value > 5) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer)

        TodayOrderScreenHeader(
            containerColor = if (getScreenClass() == ScreenClass.Compact) {
                animatedColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            popBackStack = popBackStack,
            onAddTapped = { viewModel.setAddingComponents(true) }
        )

        LoadingWrapper(isLoading = isLoading, content = {
            TodayOrderScreenContent(
                modifier = if (getScreenClass() != ScreenClass.Compact) {
                    Modifier.clip(RoundedCornerShape(surfaceCornerRadius))
                } else {
                    Modifier.fillMaxSize()
                }.background(MaterialTheme.colorScheme.surfaceContainer),
                scrollState = scrollState,
                components = components,
                allComponents = viewModel.allComponents,
                isAddingComponents = isAddingComponents,
                onDismissRequest = { viewModel.setAddingComponents(false) },
                onAddEntry = { id ->
                    viewModel.addEntry(id)
                },
                onReorderItem = { fromIndex, toIndex ->
                    viewModel.reorderItem(fromIndex, toIndex)
                },
                onTitleEntryChange = { id, text ->
                    viewModel.editEntryTitle(id, text)
                },
                onDeleteEntry = { id ->
                    viewModel.deleteEntry(id)
                }
            )
        })
    }

    if (errorSaving) {
        GenericAlertErrorSaving(onDismiss = { viewModel.dismissErrorSaving() })
    }

    if (errorLoading) {
        GenericAlertErrorLoading(onDismiss = {
            viewModel.dismissLoadingError()
            popBackStack.invoke()
        })
    }

    if (showTutorial) {
        TutorialDialog(
            onDismiss = { viewModel.dismissTutorial() },
            onConfirm = { viewModel.dismissTutorial() })
    }
}

@Composable
private fun TodayOrderScreenHeader(
    modifier: Modifier = Modifier,
    containerColor: Color, popBackStack: () -> Unit, onAddTapped: () -> Unit
) {
    ResponsiveScreenHeader(title = stringResource(Res.string.components_order),
        modifier = modifier,
        containerColor = containerColor,
        popBackStack = popBackStack,
        titleOfParent = stringResource(Res.string.today),
        trailingButtons = {
            if (getScreenClass() == ScreenClass.Compact || getScreenClass() == ScreenClass.Medium) {
                GenericPlainTooltip(
                    text = stringResource(Res.string.add_component),
                    content = {
                        IconButton(onClick = { onAddTapped.invoke() }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(Res.string.add_component)
                            )
                        }
                    }
                )

            }
        }
    )
}

@Composable
private fun TodayOrderScreenContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    components: List<DiaryEntryComponent>,
    allComponents: List<DiaryEntryComponent>,
    isAddingComponents: Boolean,
    onDismissRequest: () -> Unit,
    onAddEntry: (String) -> Unit,
    onReorderItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onTitleEntryChange: (String, String) -> Unit,
    onDeleteEntry: (String) -> Unit

) {
    Row {
        Column(modifier = modifier.weight(1f)) {
            KeyboardAware(content = {
                ReordenableRowPane(
                    scrollState = scrollState,
                    components = components,
                    onReorderItem = onReorderItem,
                    onTitleEntryChange = onTitleEntryChange,
                    onDeleteEntry = onDeleteEntry
                )
            })
        }

        if (getScreenClass() == ScreenClass.Large) {
            AddComponentsSupportPane(
                components = allComponents,
                onAddEntry = onAddEntry,
                modifier = Modifier
                    .padding(start = surfaceToWindowPadding)
                    .width(widthRightSupportPane)
            )
        } else {
            if (isAddingComponents) {
                AddComponentsBottomSheet(
                    components = allComponents,
                    onAddEntry = onAddEntry,
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
private fun ReordenableRowPane(
    scrollState: ScrollState,
    components: List<DiaryEntryComponent>,
    onReorderItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onTitleEntryChange: (String, String) -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    ReorderableColumn(
        modifier = Modifier.verticalScroll(scrollState)
            .keyboardDismissOnSwipeIOS { keyboardController?.hide() }
            .animateContentSize(),
        list = components,
        onSettle = { fromIndex, toIndex ->
            onReorderItem.invoke(fromIndex, toIndex)
        }
    ) { index, item, _ ->
        key(item.id) {
            val interactionSource = remember { MutableInteractionSource() }

            var showDeleteAlert by rememberSaveable { mutableStateOf(false) }
            if (showDeleteAlert) {
                GenericDialog(
                    title = stringResource(Res.string.are_you_sure),
                    body = stringResource(Res.string.delete_component_body),
                    onDismiss = { showDeleteAlert = false },
                    onConfirm = { onDeleteEntry.invoke(item.id) }
                )
            }

            val moveUpLabel = stringResource(Res.string.move_up_component)
            val moveDownLabel = stringResource(Res.string.move_down_component)

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ), onClick = {},
                modifier = Modifier.fillMaxWidth().semantics {
                    customActions = listOf(
                        CustomAccessibilityAction(label = moveUpLabel, action = {
                            if (index > 0) {
                                onReorderItem.invoke(index - 1, index)
                                true
                            } else {
                                false
                            }
                        }),
                        CustomAccessibilityAction(label = moveDownLabel, action = {
                            if (index < components.size - 1) {
                                onReorderItem.invoke(index + 1, index)
                                true
                            } else {
                                false
                            }
                        }),
                    )
                }, interactionSource = interactionSource
            ) {
                Row(
                    modifier =
                    if (getScreenClass() == ScreenClass.Compact) {
                        Modifier.padding(
                            pageListHorizontalPadding
                        ).padding(vertical = halfListSpacedByPadding)
                    } else {
                        Modifier.padding(vertical = halfListSpacedByPadding)
                            .padding(horizontal = interiorCardPadding)
                    }.padding(smallPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val focusManager = LocalFocusManager.current
                    val focusRequester = remember { FocusRequester() }


                    var title by rememberSaveable(stateSaver = TextFieldValueSaver) {
                        mutableStateOf(
                            TextFieldValue(item.title)
                        )
                    }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    LaunchedEffect(isFocused) {
                        if (getPlatform() == Platform.DESKTOP) return@LaunchedEffect

                        if (!isFocused) return@LaunchedEffect

                        title = title.copy(
                            selection = TextRange(0, title.text.length)
                        )
                    }


                    Column(modifier = Modifier.weight(4f)) {
                        if (!item.canBeDeleted) {
                            Row {
                                Text(title.text, color = MaterialTheme.colorScheme.primary)

                                GenericPlainTooltip(
                                    isOnButton = false,
                                    text = stringResource(Res.string.no_rename),
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(Res.string.no_rename),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        } else {
                            BasicTextField(
                                value = title,
                                onValueChange = { text ->
                                    title = text
                                    onTitleEntryChange.invoke(item.id, text.text)
                                },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                interactionSource = interactionSource,
                                textStyle = LocalTextStyle.current.merge(
                                    color = MaterialTheme.colorScheme.primary,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus() // Dismiss the keyboard
                                }),
                                enabled = item.canBeDeleted
                            )
                        }
                        ComponentByType(
                            item = item, showTitle = false, enabled = false
                        )
                    }

                    val buttonsModifier = Modifier.draggableHandle(
                        onDragStarted = {},
                        onDragStopped = {},
                        interactionSource = interactionSource,
                    ).clearAndSetSemantics { }

                    ResponsiveButtons(
                        buttonsModifier = buttonsModifier,
                        onDeleteButtonTapped = { showDeleteAlert = true },
                        item = item
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponsiveButtons(
    buttonsModifier: Modifier, onDeleteButtonTapped: () -> Unit, item: DiaryEntryComponent
) {
    if (getScreenClass() == ScreenClass.Compact) {
        Column {
            Buttons(
                modifier = buttonsModifier, onDeleteButtonTapped = onDeleteButtonTapped, item = item
            )
        }
    } else {
        Row {
            Buttons(
                modifier = buttonsModifier, onDeleteButtonTapped = onDeleteButtonTapped, item = item
            )
        }
    }
}

@Composable
private fun Buttons(
    modifier: Modifier, onDeleteButtonTapped: () -> Unit, item: DiaryEntryComponent
) {
    GenericPlainTooltip(
        text = stringResource(Res.string.reorder_component),
        content = {
            IconButton(
                modifier = modifier,
                onClick = {},
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = stringResource(Res.string.reorder_component),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
    if (item.canBeDeleted) {
        GenericPlainTooltip(
            text = stringResource(Res.string.delete_component),
            content = {
                IconButton(
                    onClick = { onDeleteButtonTapped.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(Res.string.delete_component),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddComponentsBottomSheet(
    components: List<DiaryEntryComponent>,
    onAddEntry: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(containerColor = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = { onDismissRequest.invoke() }) {
        AddComponentsSharedList(
            components = components, onAddEntry = { id ->
                onAddEntry.invoke(id)
                onDismissRequest.invoke()
            }, modifier = Modifier.padding(pageListHorizontalPadding)
        )
    }
}

@Composable
private fun AddComponentsSupportPane(
    modifier: Modifier = Modifier,
    components: List<DiaryEntryComponent>,
    onAddEntry: (String) -> Unit
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(listSpacedByPadding)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(Res.string.add_component),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.padding(mediumPadding)
            )
        }

        AddComponentsSharedList(
            components = components,
            onAddEntry = onAddEntry
        )
    }
}

@Composable
private fun AddComponentsSharedList(
    modifier: Modifier = Modifier,
    components: List<DiaryEntryComponent>,
    onAddEntry: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(listSpacedByPadding),
    ) {
        items(components.size) { index ->
            val item = components[index]

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(interiorCardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(4f)) {
                        ComponentByType(item, enabled = false)
                    }
                    if (item.canBeDeleted) IconButton(
                        onClick = { onAddEntry.invoke(item.id) },
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(Res.string.add_component),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(listSpacedByPadding))
        }
    }
}


@Composable
private fun TutorialDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    GenericDialog(
        title = stringResource(Res.string.tutorial_today_order_title),
        body = stringResource(
            resource = Res.string.tutorial_today_order_body,
            stringResource(
                if (getScreenClass() == ScreenClass.Large)
                    Res.string.tutorial_today_order_body_addon_screen_large
                else Res.string.tutorial_today_order_body_addon_screen_not_large
            )
        ),
        onDismiss = { onDismiss.invoke() },
        confirmButton = {
            Button(onClick = { onConfirm.invoke() }) {
                Text(stringResource(Res.string.okey))
            }
        }
    )
}
