package it.cynomys.cfmandroid.silo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SiloVisualRepresentation(
    silo: Silo,
    modifier: Modifier = Modifier
) {
    // Clean, simple colors
    val siloColor = Color(0xFFB0B0B0)
    val outlineColor = Color(0xFF404040)
    val roofColor = Color(0xFF808080)
    val supportColor = Color(0xFF606060)

    Box(
        modifier = modifier
            .widthIn(max = 110.dp)        // 🔥 Smaller overall size (previously 200dp)
            .aspectRatio(1f / 1.3f)
    ) {
        Canvas(modifier = Modifier.matchParentSize().align(Alignment.Center)) {
            val totalHeight = size.height
            val totalWidth = size.width

            // Compact proportions
            val siloBodyWidth = totalWidth * 0.5f
            val roofHeight = totalHeight * 0.15f
            val siloBodyHeight = totalHeight * 0.55f

            val startX = (totalWidth - siloBodyWidth) / 2f
            val endX = startX + siloBodyWidth
            val centerX = (startX + endX) / 2f
            val bodyTopY = roofHeight
            val bodyBottomY = bodyTopY + siloBodyHeight

            // === CYLINDRICAL BODY ===
            drawRect(
                color = siloColor,
                topLeft = Offset(startX, bodyTopY),
                size = Size(siloBodyWidth, siloBodyHeight)
            )

            // Simple horizontal bands
            val numBands = 5
            for (i in 1 until numBands) {
                val bandY = bodyTopY + (siloBodyHeight / numBands) * i
                drawLine(
                    color = outlineColor.copy(alpha = 0.3f),
                    start = Offset(startX, bandY),
                    end = Offset(endX, bandY),
                    strokeWidth = 1f
                )
            }

            // Body outline
            drawRect(
                color = outlineColor,
                topLeft = Offset(startX, bodyTopY),
                size = Size(siloBodyWidth, siloBodyHeight),
                style = Stroke(width = 2f)
            )

            // === ROOF ===
            val coneApexY = roofHeight * 0.3f

            val roofPath = Path().apply {
                moveTo(startX, bodyTopY)
                lineTo(endX, bodyTopY)
                lineTo(centerX, coneApexY)
                close()
            }

            drawPath(roofPath, color = roofColor)
            drawPath(roofPath, color = outlineColor, style = Stroke(width = 2f))

            // Small vent cap
            val ventWidth = siloBodyWidth * 0.15f
            val ventHeight = roofHeight * 0.25f

            drawRect(
                color = supportColor,
                topLeft = Offset(centerX - ventWidth / 2, coneApexY),
                size = Size(ventWidth, ventHeight)
            )

            drawRect(
                color = outlineColor,
                topLeft = Offset(centerX - ventWidth / 2, coneApexY),
                size = Size(ventWidth, ventHeight),
                style = Stroke(width = 1.5f)
            )

            // === BOTTOM STRUCTURE ===
            when (silo.shape) {
                SiloShape.FULL_CYLINDRICAL, SiloShape.FLAT_BOTTOM -> {
                    // Bottom line
                    drawLine(
                        color = outlineColor,
                        start = Offset(startX, bodyBottomY),
                        end = Offset(endX, bodyBottomY),
                        strokeWidth = 2f
                    )

                    // Support legs
                    val legHeight = totalHeight - bodyBottomY - 5.dp.toPx()
                    val legWidth = 4.dp.toPx()
                    val numLegs = 4

                    for (i in 0 until numLegs) {
                        val legX = startX + (siloBodyWidth / (numLegs + 1)) * (i + 1) - legWidth / 2

                        drawRect(
                            color = supportColor,
                            topLeft = Offset(legX, bodyBottomY),
                            size = Size(legWidth, legHeight)
                        )

                        drawRect(
                            color = outlineColor,
                            topLeft = Offset(legX, bodyBottomY),
                            size = Size(legWidth, legHeight),
                            style = Stroke(width = 1f)
                        )
                    }

                    // Ground line
                    drawLine(
                        color = outlineColor,
                        start = Offset(startX - 10.dp.toPx(), totalHeight - 2f),
                        end = Offset(endX + 10.dp.toPx(), totalHeight - 2f),
                        strokeWidth = 2f
                    )
                }

                SiloShape.CONICAL_BOTTOM -> {
                    val coneBottomY = totalHeight * 0.85f

                    // Hopper cone
                    val conePath = Path().apply {
                        moveTo(startX, bodyBottomY)
                        lineTo(endX, bodyBottomY)
                        lineTo(centerX, coneBottomY)
                        close()
                    }

                    drawPath(path = conePath, color = siloColor)
                    drawPath(path = conePath, color = outlineColor, style = Stroke(width = 2f))

                    // Support lines
                    drawLine(
                        color = supportColor,
                        start = Offset(startX, bodyBottomY),
                        end = Offset(centerX - siloBodyWidth * 0.05f, coneBottomY),
                        strokeWidth = 2f
                    )

                    drawLine(
                        color = supportColor,
                        start = Offset(endX, bodyBottomY),
                        end = Offset(centerX + siloBodyWidth * 0.05f, coneBottomY),
                        strokeWidth = 2f
                    )

                    // Discharge pipe
                    val pipeWidth = siloBodyWidth * 0.1f
                    val pipeHeight = totalHeight - coneBottomY - 5.dp.toPx()

                    drawRect(
                        color = supportColor,
                        topLeft = Offset(centerX - pipeWidth / 2, coneBottomY),
                        size = Size(pipeWidth, pipeHeight)
                    )

                    drawRect(
                        color = outlineColor,
                        topLeft = Offset(centerX - pipeWidth / 2, coneBottomY),
                        size = Size(pipeWidth, pipeHeight),
                        style = Stroke(width = 1.5f)
                    )

                    // Ground line
                    drawLine(
                        color = outlineColor,
                        start = Offset(startX - 10.dp.toPx(), totalHeight - 2f),
                        end = Offset(endX + 10.dp.toPx(), totalHeight - 2f),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}
