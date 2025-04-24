package com.diaryCMP.uimodule.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.no
import diary.composeapp.utilsmodule.generated.resources.yes
import es.diaryCMP.modelsModule.models.BooleanDiaryComponent
import es.diaryCMP.modelsModule.models.DiaryEntryComponent
import es.diaryCMP.modelsModule.models.FiveOptionDiaryComponent
import es.diaryCMP.modelsModule.models.LongTextDiaryComponent
import es.diaryCMP.modelsModule.models.ShortTextDiaryComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShortTextDiaryComposable(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    size: Int = 16,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    showsTitle: Boolean = true,
    enabled: Boolean = true,
    isExhibit: Boolean = false
) {
    var isFocussed by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        if (showsTitle) Text(
            text = title, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.padding(4.dp)) {
            if (value.isEmpty()) {
                BasicTextField(
                    value = title,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = style.merge(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                        fontSize = size.sp,
                        lineHeight = (size + 8).sp
                    ),
                    readOnly = true
                )
            }

            val focusManager = LocalFocusManager.current

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocussed = focusState.isFocused
                    }
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter || it.key == Key.NumPadEnter && it.type == KeyEventType.KeyUp) {
                            focusManager.clearFocus()
                            true
                        } else false
                    },
                textStyle = style.merge(
                    color = if (!enabled && !isExhibit) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = size.sp,
                    lineHeight = (size + 8).sp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                singleLine = true,
                readOnly = !enabled
            )
        }
        HorizontalDivider(
            color = if (!enabled) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            } else if (isFocussed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun LongTextDiaryComposable(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    size: Int = 16,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    showsTitle: Boolean = true,
    enabled: Boolean = true,
    isExhibit: Boolean = false
) {
    var isFocussed by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        if (showsTitle) Text(
            text = title, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.padding(4.dp)) {
            if (value.isEmpty()) {
                BasicTextField(
                    value = title,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = style.merge(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                        fontSize = size.sp,
                        lineHeight = (size + 8).sp
                    ),
                    readOnly = true
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocussed = focusState.isFocused
                    },
                textStyle = style.merge(
                    color = if (!enabled && !isExhibit) {

                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = size.sp,
                    lineHeight = (size + 8).sp,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = true,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                readOnly = !enabled
            )
        }
        HorizontalDivider(
            color = if (!enabled) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            } else if (isFocussed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooleanDiaryComposable(
    title: String,
    value: Boolean?,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showsTitle: Boolean = true,
    enabled: Boolean = true,
    isExhibit: Boolean = false
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        if (showsTitle) Text(
            text = title, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val options = listOf(stringResource(Res.string.no), stringResource(Res.string.yes))
        val icons = listOf(
            Icons.Filled.Close,
            Icons.Filled.Check,
        )

        MultiChoiceSegmentedButtonRow(modifier = Modifier) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = false) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    onCheckedChange = {
                        onValueChange.invoke(index == 1)
                    },
                    checked = value != null && (index == 0 && value == false || index == 1 && value == true),
                    colors = if (isExhibit) {
                        SegmentedButtonDefaults.colors(
                            disabledInactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledInactiveBorderColor = MaterialTheme.colorScheme.outline,
                            disabledInactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledActiveContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledActiveContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledActiveBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    } else {
                        SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledInactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledInactiveBorderColor = MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.12f
                            ),
                            disabledInactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    },
                    enabled = enabled
                ) {
                    Text(label)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiveOptionDiaryComposable(
    title: String,
    value: Int?,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showsTitle: Boolean = true,
    enabled: Boolean = true,
    isExhibit: Boolean = false
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        if (showsTitle) Text(
            text = title, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val options = listOf("", "", "", "", "")
        val icons = listOf(
            Icons.AutoMirrored.Filled.TrendingDown,
            Icons.Filled.ExpandMore,
            Icons.Filled.HorizontalRule,
            Icons.Filled.ExpandLess,
            Icons.AutoMirrored.Filled.TrendingUp,
        )

        MultiChoiceSegmentedButtonRow(modifier = Modifier) {
            options.forEachIndexed { index, _ ->
                SegmentedButton(
                    label = {
                        SegmentedButtonDefaults.Icon(active = false) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = { },
                    onCheckedChange = {
                        onValueChange.invoke(index)
                    },
                    checked = value != null && (value == index),
                    colors = if (isExhibit) {
                        SegmentedButtonDefaults.colors(
                            disabledInactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledInactiveBorderColor = MaterialTheme.colorScheme.outline,
                            disabledInactiveContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledActiveContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledActiveContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledActiveBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    } else {
                        SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            disabledInactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledInactiveBorderColor = MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.12f
                            ),
                            disabledInactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun ComponentByType(
    item: DiaryEntryComponent,
    showTitle: Boolean = true,
    enabled: Boolean = true,
    isExhibit: Boolean = false,
    onValueChange: (Any) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (item) {
        is ShortTextDiaryComponent -> {
            var mutableText by remember { mutableStateOf(item.text) }
            if (!enabled) mutableText = item.text

            ShortTextDiaryComposable(
                value = mutableText,
                onValueChange = { text ->
                    mutableText = text
                    onValueChange.invoke(text)
                },
                title = item.title,
                enabled = enabled,
                isExhibit = isExhibit,
                showsTitle = showTitle,
                size = item.textSize,
                modifier = modifier
            )
        }

        is LongTextDiaryComponent -> {
            var mutableText by remember { mutableStateOf(item.text) }
            if (!enabled) mutableText = item.text

            LongTextDiaryComposable(
                value = mutableText,
                onValueChange = { text ->
                    mutableText = text
                    onValueChange.invoke(text)
                },
                title = item.title,
                enabled = enabled,
                isExhibit = isExhibit,
                showsTitle = showTitle,
                size = item.textSize,
                modifier = modifier
            )
        }

        is BooleanDiaryComponent -> {
            var mutableBoolean by remember { mutableStateOf(item.isChecked) }
            if (!enabled) mutableBoolean = item.isChecked

            BooleanDiaryComposable(
                title = item.title,
                value = mutableBoolean,
                onValueChange = { boolean ->
                    mutableBoolean = boolean
                    onValueChange.invoke(boolean)
                },
                enabled = enabled,
                isExhibit = isExhibit,
                showsTitle = showTitle,
                modifier = modifier
            )
        }

        is FiveOptionDiaryComponent -> {
            var mutableIndex by remember { mutableStateOf(item.index) }
            if (!enabled) mutableIndex = item.index

            FiveOptionDiaryComposable(
                title = item.title,
                value = mutableIndex,
                onValueChange = { index ->
                    mutableIndex = index
                    onValueChange.invoke(index)
                },
                enabled = enabled,
                isExhibit = isExhibit,
                showsTitle = showTitle,
                modifier = modifier
            )
        }
    }
}
