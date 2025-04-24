package com.diaryCMP.uimodule.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diaryCMP.uimodule.ui.theme.extraSmallPadding
import com.diaryCMP.uimodule.ui.theme.interiorCardPadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.lengthAgainstStatisticsComponentLineGraphHeight
import com.diaryCMP.uimodule.ui.theme.noPadding
import com.diaryCMP.uimodule.ui.theme.pillBoxHeight
import com.diaryCMP.uimodule.ui.theme.pillsGraphInternalPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.surfaceCornerRadius
import diary.composeapp.utilsmodule.generated.resources.Res
import diary.composeapp.utilsmodule.generated.resources.error_graphic
import diary.composeapp.utilsmodule.generated.resources.false_
import diary.composeapp.utilsmodule.generated.resources.graphic_of
import diary.composeapp.utilsmodule.generated.resources.length_of
import diary.composeapp.utilsmodule.generated.resources.number_percent
import diary.composeapp.utilsmodule.generated.resources.number_words
import diary.composeapp.utilsmodule.generated.resources.register_time
import diary.composeapp.utilsmodule.generated.resources.summary_example_component_title
import diary.composeapp.utilsmodule.generated.resources.tooltip_long_text_of_length
import diary.composeapp.utilsmodule.generated.resources.tooltip_register_time
import diary.composeapp.utilsmodule.generated.resources.true_
import es.diaryCMP.utilsModule.utils.statistics.convertSeconds
import es.diaryCMP.utilsModule.utils.statistics.getDaysOfWeekSevenLatest
import es.diaryCMP.utilsModule.utils.statistics.interpolateColor
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
fun GenericCard(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(interiorCardPadding)
    ) {
        content()
    }
}

@Composable
fun LengthAgainstStatisticsComponentPreview(
    numberWordsSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    LineGraph(
        points = numberWordsSevenDays.map { it?.toFloat() },
        height = lengthAgainstStatisticsComponentLineGraphHeight - largePadding,
        errorText = stringResource(Res.string.error_graphic),
        color = MaterialTheme.colorScheme.secondary,
        accentColor = MaterialTheme.colorScheme.tertiary,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        showAverage = true,
        showXAxisLabel = false,
        animatedProgress = animatedProgress,
        modifier = modifier.padding(smallPadding)
    )
}


@Composable
fun TimeAgainstStatisticsComponentPreview(
    secondsSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    LengthAgainstStatisticsComponentPreview(
        numberWordsSevenDays = secondsSevenDays,
        animatedProgress = animatedProgress,
        modifier = modifier
    )
}


@Composable
fun YesNoStatisticsComponentPreview(
    yesNoSevenDays: List<Boolean?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    BooleanGraph(
        yesNoSevenDays = yesNoSevenDays,
        modifier = modifier.width(300.dp),
        showDays = false,
        isPreview = true,
        animatedProgress = animatedProgress
    )
}


@Composable
fun NumberStatisticsComponentPreview(
    numbersSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    NumberGraph(
        numbersSevenDays = numbersSevenDays,
        animatedProgress = animatedProgress,
        showDays = false,
        isPreview = true,
        modifier = modifier.width(300.dp)
    )
}

@Composable
fun LengthAgainstStatisticsComponent(
    componentTitle: String = stringResource(Res.string.summary_example_component_title),
    numberWordsSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    GenericAgainstStatisticsComponent(
        title = stringResource(Res.string.length_of, componentTitle),
        tooltipString = stringResource(Res.string.tooltip_long_text_of_length, componentTitle),
        numberText = { number ->
            Text(
                pluralStringResource(
                    Res.plurals.number_words,
                    number ?: 0,
                    number ?: "?"
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        numberSevenDays = numberWordsSevenDays,
        animatedProgress = animatedProgress,
        modifier = modifier
    )
}

@Composable
fun TimeAgainstStatisticsComponent(
    secondsSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    GenericAgainstStatisticsComponent(
        title = stringResource(Res.string.register_time),
        tooltipString = stringResource(Res.string.tooltip_register_time),
        numberText = { number ->
            Text(
                convertSeconds(number ?: 0),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        numberSevenDays = secondsSevenDays,
        animatedProgress = animatedProgress,
        modifier = modifier
    )
}

@Composable
private fun GenericAgainstStatisticsComponent(
    title: String,
    tooltipString: String?,
    numberText: @Composable (Int?) -> Unit,
    numberSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    if (numberSevenDays.isEmpty()) return

    GenericCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(vertical = interiorCardPadding)
        ) {
            @Suppress("NAME_SHADOWING") val numberSevenDays = numberSevenDays.takeLast(7)

            Row(
                modifier = Modifier
                    .padding(horizontal = interiorCardPadding)
                    .padding(horizontal = smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                )

                if (tooltipString != null)
                    GenericPlainTooltip(
                        text = tooltipString,
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = tooltipString,
                                modifier = Modifier.fillMaxHeight()
                                    .padding(top = extraSmallPadding, start = extraSmallPadding)
                            )
                        },
                        isOnButton = false
                    )
            }

            var selectedIndex: Int by rememberSaveable {
                mutableStateOf(
                    if (numberSevenDays.filterNotNull().isEmpty()) 0
                    else (numberSevenDays.indexOf(numberSevenDays.last { it != null }))
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = interiorCardPadding)
                    .padding(horizontal = smallPadding),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(Modifier.weight(1f, fill = false).animateContentSize()) {
                    numberText(numberSevenDays[selectedIndex])
                }

                AnimatedContent(targetState = selectedIndex == numberSevenDays.size - 1,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) +
                                scaleIn(initialScale = 0.52f, animationSpec = tween(220))
                                ).togetherWith(
                                fadeOut(animationSpec = tween(90)) +
                                        scaleOut(
                                            targetScale = 0.52f,
                                            animationSpec = tween(220)
                                        )
                            )
                    }
                ) { targetState ->
                    when (targetState) {
                        true -> {
                            val averageNumber = numberSevenDays.filterNotNull().average()
                            val difference: Float =
                                (numberSevenDays.last()?.toFloat()
                                    ?: 0f) - averageNumber.toFloat()
                            val percent: Float = difference / averageNumber.toFloat()
                            val sign = if (percent > 0) "+" else ""

                            if (percent != -1f)
                                Text(
                                    stringResource(
                                        Res.string.number_percent,
                                        (sign + (percent * 100 * 100).toInt() / 100f)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(start = extraSmallPadding)
                                )
                        }

                        false -> {
                            val secondary = MaterialTheme.colorScheme.secondary
                            val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
                            Box(Modifier.padding(start = extraSmallPadding)) { // Padding changes form the canvas because this is the size and the other is the radius
                                Box(
                                    Modifier.clip(CircleShape).background(secondary)
                                        .size(smallPadding * 2)
                                ) {}
                                Box(
                                    Modifier.padding(
                                        top = extraSmallPadding,
                                        start = extraSmallPadding
                                    ).clip(CircleShape).background(surfaceContainer)
                                        .size(extraSmallPadding * 2)
                                ) {}
                            }
                        }
                    }
                }
            }

            Spacer(modifier.height(largePadding))

            LineGraph(
                points = numberSevenDays.map { it?.toFloat() },
                height = lengthAgainstStatisticsComponentLineGraphHeight - largePadding * 2,
                errorText = stringResource(Res.string.error_graphic),
                color = MaterialTheme.colorScheme.secondary,
                accentColor = MaterialTheme.colorScheme.tertiary,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                animatedProgress = animatedProgress,
                showAverage = true,
                selectedPointIndex = selectedIndex,
                onPointClick = {
                    selectedIndex = it
                },
                modifier = Modifier.padding(horizontal = interiorCardPadding)
            )

            Spacer(modifier.height(smallPadding))
        }
    }
}


@Composable
fun YesNoStatisticsComponent(
    componentTitle: String,
    yesNoSevenDays: List<Boolean?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    if (yesNoSevenDays.isEmpty()) return

    GenericCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(interiorCardPadding)
                .padding(horizontal = smallPadding)
        ) {
            if (yesNoSevenDays.size < 2) {
                Text(stringResource(Res.string.error_graphic))
                return@Column
            }
            @Suppress("NAME_SHADOWING") val yesNoSevenDays =
                yesNoSevenDays.takeLast(7)

            Text(
                stringResource(Res.string.graphic_of, componentTitle),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier.height(largePadding))

            BooleanGraph(yesNoSevenDays = yesNoSevenDays, animatedProgress = animatedProgress)

            Spacer(modifier.height(smallPadding))
        }
    }
}

@Composable
fun NumberStatisticsComponent(
    componentTitle: String,
    numbersSevenDays: List<Int?>,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    modifier: Modifier = Modifier
) {
    if (numbersSevenDays.isEmpty()) return

    GenericCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(interiorCardPadding)
                .padding(horizontal = smallPadding)
        ) {
            if (numbersSevenDays.size < 2) {
                Text(stringResource(Res.string.error_graphic))
                return@Column
            }

            @Suppress("NAME_SHADOWING") val numbersSevenDays =
                numbersSevenDays.takeLast(7)

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Text(
                    stringResource(Res.string.graphic_of, componentTitle),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier.height(largePadding))

            NumberGraph(
                numbersSevenDays = numbersSevenDays,
                animatedProgress = animatedProgress
            )

            Spacer(modifier.height(smallPadding))
        }
    }
}

@Composable
private fun BooleanGraph(
    yesNoSevenDays: List<Boolean?>,
    showDays: Boolean = true,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    GenericPillsGraph(
        valuesSevenDays = yesNoSevenDays.map {
            when (it) {
                true -> 0
                false -> 1
                else -> null
            }
        },
        numberOfStates = 2,
        showDays = showDays,
        animatedProgress = animatedProgress,
        touchableBoxContent = { value ->
            if (value == 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary).fillMaxSize()
                ) {
                    Text(
                        text = stringResource(Res.string.true_).take(1),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = stringResource(Res.string.false_).take(1),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        },
        isPreview = isPreview,
        modifier = modifier
    )
}

@Composable
private fun NumberGraph(
    numbersSevenDays: List<Int?>,
    showDays: Boolean = true,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    GenericPillsGraph(
        valuesSevenDays = numbersSevenDays.map { it?.minus(4)?.let { it1 -> abs(it1) } },
        numberOfStates = 5,
        showDays = showDays,
        animatedProgress = animatedProgress,
        touchableBoxContent = { value ->
            Icon(
                imageVector = when (value) {
                    0 -> Icons.AutoMirrored.Filled.TrendingUp
                    1 -> Icons.Filled.ExpandLess
                    2 -> Icons.Filled.HorizontalRule
                    3 -> Icons.Filled.ExpandMore
                    4 -> Icons.AutoMirrored.Filled.TrendingDown
                    else -> {
                        Icons.Filled.QuestionMark
                    }
                },
                contentDescription = null,
                tint = interpolateColor(
                    MaterialTheme.colorScheme.onPrimary,
                    MaterialTheme.colorScheme.onSecondary,
                    value / 4f
                )
            )
        },
        isPreview = isPreview,
        modifier = modifier
    )
}

@Composable
fun GenericPillsGraph(
    valuesSevenDays: List<Int?>,
    numberOfStates: Int,
    showDays: Boolean = true,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    touchableBoxContent: @Composable (Int) -> Unit,
    spacingDivisor: Float = 1.25f,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = getDaysOfWeekSevenLatest()

    val density = LocalDensity.current.density
    var graphWidth by remember { mutableStateOf(noPadding) }

    Row(
        modifier = modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
            graphWidth = Dp(coordinates.size.width / density)
        },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(daysOfWeek.size) {
            Box(
                Modifier.height(IntrinsicSize.Max).padding(horizontal = pillsGraphInternalPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                val pillWidth =
                    (graphWidth / valuesSevenDays.size) - pillsGraphInternalPadding.times(
                        2
                    )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                ) {
                    valuesSevenDays[it].let { value ->
                        if (value == null) {
                            return@let
                        }

                        for (i in 0..<value) {
                            Spacer(
                                Modifier.height(
                                    pillBoxHeight.div(spacingDivisor)
                                        .div(numberOfStates.minus(1).toFloat())
                                )
                            )
                        }

                        TouchableBox(
                            height = pillBoxHeight,
                            width = pillWidth,
                            value = value,
                            animatedVisibility = it.toFloat() / valuesSevenDays.size <= animatedProgress.value,
                            isPreview = isPreview,
                            content = { touchableBoxContent(value) }
                        )
                    }
                }

                if (showDays) {
                    Column(
                        modifier = Modifier.width(
                            pillWidth
                        )
                    ) {
                        Spacer(modifier = Modifier.height(pillBoxHeight))
                        Spacer(modifier = Modifier.height(pillBoxHeight.div(spacingDivisor)))

                        Spacer(modifier = Modifier.height(extraSmallPadding))

                        Text(
                            daysOfWeek[it], style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Suppress("NAME_SHADOWING")
@Composable
private fun TouchableBox(
    height: Dp,
    width: Dp,
    value: Int,
    animatedVisibility: Boolean,
    content: @Composable () -> Unit,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = animatedVisibility,
        enter = scaleIn(tween(durationMillis = 300)),
        modifier = modifier,
        label = "Touchable Box"
    ) {
        var isPressed by remember { mutableStateOf(false) }
        val transition = updateTransition(targetState = isPressed, label = "Transition")

        val animatedBoxHeight by transition.animateDp(label = "Height Animation") { isPressed ->
            if (isPressed) height / 1.10f else height
        }

        val animatedCornerRadius by transition.animateDp(label = "Corner Radius Animation") { isPressed ->
            if (isPressed) surfaceCornerRadius * 4 else surfaceCornerRadius
        }

        Box(
            modifier = Modifier
                .padding(top = (height - animatedBoxHeight) / 2)
                .height(animatedBoxHeight)
                .width(width)
                .clip(RoundedCornerShape(animatedCornerRadius))
                .background(
                    interpolateColor(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        value / 4f
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (!isPreview) {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}