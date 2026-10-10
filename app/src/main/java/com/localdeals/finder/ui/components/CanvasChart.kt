package com.localdeals.finder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.localdeals.finder.ui.screens.safeDrawText

@Composable
fun CanvasChart(
    data: List<Int>,
    modifier: Modifier = Modifier.fillMaxSize(),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val maxData = data.maxOrNull() ?: 1
    val ySteps = 4

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftPadding = 40.dp.toPx()
        val bottomPadding = 30.dp.toPx()

        val graphWidth = width - leftPadding
        val graphHeight = height - bottomPadding

        // Draw Y Axis Labels
        for (i in 0..ySteps) {
            val yValue = maxData * i / ySteps
            val yPos = graphHeight - (i.toFloat() / ySteps) * graphHeight
            
            safeDrawText(
                textMeasurer = textMeasurer,
                text = yValue.toString(),
                topLeft = Offset(0f, yPos - 10.dp.toPx()),
                style = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp))
            )
            
            // Draw grid line
            drawLine(
                color = textColor.copy(alpha = 0.2f),
                start = Offset(leftPadding, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw Line Chart
        if (data.size > 1) {
            val xStep = graphWidth / (data.size - 1)
            val path = Path()

            data.forEachIndexed { index, value ->
                val x = leftPadding + index * xStep
                val y = graphHeight - (value.toFloat() / maxData) * graphHeight

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw data point
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
