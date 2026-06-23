package com.fidit.memberlog.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GrowthChart(
    values: List<Int>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(values) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val r = reveal.value
        val maxV = (values.maxOrNull() ?: 1).coerceAtLeast(1)
        val points = if (values.size == 1) {
            val y = h - (values[0].toFloat() / maxV) * h * r
            listOf(Offset(0f, y), Offset(w, y))
        } else {
            val stepX = w / (values.size - 1)
            values.mapIndexed { i, v -> Offset(i * stepX, h - (v.toFloat() / maxV) * h * r) }
        }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(points.last().x, h)
            lineTo(points.first().x, h)
            close()
        }
        drawPath(fill, fillColor)
        drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}
