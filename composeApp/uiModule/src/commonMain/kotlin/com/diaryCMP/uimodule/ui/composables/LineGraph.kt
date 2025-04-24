package com.diaryCMP.uimodule.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.diaryCMP.uimodule.ui.theme.buttonGraphSize
import com.diaryCMP.uimodule.ui.theme.defaultHeightLineGraph
import com.diaryCMP.uimodule.ui.theme.extraSmallPadding
import com.diaryCMP.uimodule.ui.theme.largePadding
import com.diaryCMP.uimodule.ui.theme.mediumPadding
import com.diaryCMP.uimodule.ui.theme.smallPadding
import com.diaryCMP.uimodule.ui.theme.widthGridCells
import es.diaryCMP.utilsModule.utils.DpSaver
import es.diaryCMP.utilsModule.utils.DpSizeSaver
import es.diaryCMP.utilsModule.utils.statistics.getDaysOfWeekSevenLatest

@Composable
fun LineGraph(
    points: List<Float?>,

    xAxisLabel: List<String> = getDaysOfWeekSevenLatest(),

    color: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,

    showAverage: Boolean = false,
    showXAxisLabel: Boolean = true,

    selectedPointIndex: Int? = null,
    onPointClick: (index: Int) -> Unit = {},

    height: Dp = defaultHeightLineGraph,

    errorText: String = "Error",

    animatedProgress: Animatable<Float, AnimationVector1D>,

    modifier: Modifier = Modifier
) {
    var showError by rememberSaveable { mutableStateOf(false) }

    if (points.isEmpty() || points.filterNotNull().count() < 2) showError = true

    if (showError) {
        Box(modifier = modifier.height(IntrinsicSize.Min).padding(horizontal = largePadding)) {
            Column(
                Modifier
                    .blur(radius = extraSmallPadding)
                    .padding(extraSmallPadding)
                    .blur(mediumPadding)
            ) {
                LineChart(
                    height = height,
                    values = listOf(1.2f, 0.4f, 1f),
                    color = color,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.height(extraSmallPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysOfWeek = getDaysOfWeekSevenLatest()

                    repeat(daysOfWeek.size) {
                        Text(daysOfWeek[it], style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                backgroundColor.copy(alpha = 0.6f)
                            )
                        )
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorText, textAlign = TextAlign.Center, modifier = Modifier.padding(largePadding))
            }
        }
        return
    }

    Column(modifier = modifier.widthIn(min = widthGridCells)) {
        val density = LocalDensity.current
        var dayWidth by rememberSaveable(stateSaver = DpSaver) { mutableStateOf(0.dp) }
        AnimatedVisibility(
            visible = animatedProgress.value > 0f,
            enter = expandHorizontally(
                expandFrom = Alignment.Start,
                animationSpec = tween(durationMillis = 450)
            )
        ) {
            LineChart(
                height = height,
                values = points,
                color = color,
                accentColor = accentColor,
                showAverage = showAverage,
                backgroundColor = backgroundColor,
                modifier = Modifier.padding(horizontal = dayWidth / 2),
                selectedPointIndex = selectedPointIndex,
                onPointClick = onPointClick
            )
        }

        Spacer(modifier = Modifier.height(smallPadding))

        if (showXAxisLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                xAxisLabel.forEach { day ->
                    Text(
                        day,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).onGloballyPositioned {
                            dayWidth = with(density) { it.size.width.toDp() }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    modifier: Modifier = Modifier,
    height: Dp,
    values: List<Float?>,
    color: Color,
    accentColor: Color = Color.Red,
    backgroundColor: Color = Color.Black,
    showAverage: Boolean = false,
    selectedPointIndex: Int? = null,
    onPointClick: (index: Int) -> Unit = {}
) {
    val nonNullValues = values.filterNotNull()
    val minValue = nonNullValues.minOrNull() ?: return
    val maxValue = nonNullValues.maxOrNull() ?: return

    val normalizedValues = values.map {
        it?.let { value ->
            if (maxValue - minValue != 0f) (value - minValue) / (maxValue - minValue) + 0.1f
            else 1f
        }
    }

    val averageNormalized = normalizedValues.filterNotNull().average().toFloat()

    val density = LocalDensity.current
    var canvasSize by rememberSaveable(stateSaver = DpSizeSaver) {
        mutableStateOf(
            DpSize(
                0.dp,
                0.dp
            )
        )
    }


    Box(modifier = modifier.fillMaxWidth().height(height)) {
        Canvas(
            modifier = Modifier.fillMaxSize()
                .onGloballyPositioned {
                    canvasSize =
                        with(density) { DpSize(it.size.width.toDp(), it.size.height.toDp()) }
                }
        ) {
            val linePath = Path()
            val fillPath = Path()
            val stepX = size.width / (normalizedValues.size - 1)
            var prevPoint: Offset? = null

            normalizedValues.forEachIndexed { index, dataPoint ->
                if (dataPoint == null) return@forEachIndexed

                val currentPoint = Offset(
                    x = index * stepX,
                    y = size.height - (dataPoint * (size.height - size.height / 9.5f))
                )

                if (prevPoint == null) {
                    linePath.moveTo(currentPoint.x, currentPoint.y)
                    fillPath.moveTo(currentPoint.x, size.height)
                    fillPath.lineTo(currentPoint.x, currentPoint.y)
                } else {
                    val controlPoint1 = Offset(
                        (prevPoint!!.x + currentPoint.x) / 2,
                        prevPoint!!.y
                    )
                    val controlPoint2 = Offset(
                        (prevPoint!!.x + currentPoint.x) / 2,
                        currentPoint.y
                    )

                    linePath.cubicTo(
                        x1 = controlPoint1.x, y1 = controlPoint1.y,
                        x2 = controlPoint2.x, y2 = controlPoint2.y,
                        x3 = currentPoint.x, y3 = currentPoint.y
                    )

                    fillPath.cubicTo(
                        x1 = controlPoint1.x, y1 = controlPoint1.y,
                        x2 = controlPoint2.x, y2 = controlPoint2.y,
                        x3 = currentPoint.x, y3 = currentPoint.y
                    )
                }

                prevPoint = currentPoint
            }

            fillPath.lineTo(prevPoint!!.x, size.height)
            fillPath.close()

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent)
            )

            drawPath(
                path = linePath, color = color, style = Stroke(width = extraSmallPadding.toPx())
            )

            drawPath(
                path = fillPath, brush = gradientBrush
            )

            if (showAverage) {
                val averageY =
                    size.height - (averageNormalized * (size.height - size.height / 9.5f))
                drawLine(
                    color = accentColor,
                    start = Offset(0f, averageY),
                    end = Offset(size.width, averageY),
                    strokeWidth = extraSmallPadding.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            normalizedValues.forEachIndexed { index, dataPoint ->
                if (dataPoint == null) return@forEachIndexed

                val x = index * stepX
                val y = size.height - (dataPoint * (size.height - size.height / 9.5f))

                if (index == selectedPointIndex) {
                    drawCircle(
                        color = color,
                        radius = smallPadding.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = backgroundColor,
                        radius = extraSmallPadding.toPx(),
                        center = Offset(x, y)
                    )
                } else {
                    drawCircle(
                        color = backgroundColor,
                        radius = smallPadding.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = color,
                        radius = extraSmallPadding.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        normalizedValues.forEachIndexed { index, dataPoint ->
            if (dataPoint != null) {
                val x = index * (canvasSize.width / (normalizedValues.size - 1))
                val y =
                    canvasSize.height - (dataPoint * (canvasSize.height - canvasSize.height / 9.5f))

                Button(
                    onClick = { onPointClick(index) },
                    modifier = Modifier
                        .height(height).width(buttonGraphSize)
                        .absoluteOffset(x = x - buttonGraphSize / 2, y = y - buttonGraphSize / 2)
                        .alpha(0f)
                ) {
                }
            }
        }
    }
}
