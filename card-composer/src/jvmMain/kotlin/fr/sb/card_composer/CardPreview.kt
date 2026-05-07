package fr.sb.card_composer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
internal fun CardPreview(
    face: CardFace,
    hideBleed: Boolean,
    showSafeArea: Boolean,
    contentAlignment: Alignment = Alignment.Center,
    modifier: Modifier = Modifier.Companion,
) {
    var boxSize: IntSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        contentAlignment = contentAlignment,
        modifier =
            modifier
                .padding(16.dp)
                .onSizeChanged { boxSize = it }
    ) {
        if (boxSize != IntSize.Zero) {
            val fullCardSize = CardSize.fullSize(face.size)
            val widthRatio = boxSize.width.toFloat() / fullCardSize.width.value
            val heightRatio = boxSize.height.toFloat() / fullCardSize.height.value

            CompositionLocalProvider(
                LocalDensity provides Density(min(widthRatio, heightRatio)),
                CardTheme provides face.theme,
                LocalCardSize provides face.size,
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(fullCardSize.width)
                            .height(fullCardSize.height)
                            .clipToBounds()
                ) {
                    val density = LocalDensity.current

                    val primaryColor = MaterialTheme.colorScheme.primaryContainer
                    Box(
                        content = face.content,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardTheme.current.cardBackground)
                            .drawWithContent {
                                drawContent()
                                if (!showSafeArea) return@drawWithContent
                                val innerSafeAreaPx = with(density) { CardSize.safePadding.toPx() }
                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(innerSafeAreaPx, innerSafeAreaPx),
                                    size = Size(
                                        width = size.width - innerSafeAreaPx * 2,
                                        height = size.height - innerSafeAreaPx * 2,
                                    ),
                                    cornerRadius = CornerRadius(2.mm.toPx()),
                                    style = Stroke(
                                        width = .6.mm.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(
                                                1.mm.toPx(),
                                                1.mm.toPx()
                                            )
                                        ),
                                    ),
                                    alpha = .8f,
                                )
                            }
                    )

                    val bgColor = MaterialTheme.colorScheme.background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val outerBleeding = with(density) { CardSize.standardSafeMargin.toPx() }
                        val circlePath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = outerBleeding,
                                    top = outerBleeding,
                                    right = size.width - outerBleeding,
                                    bottom = size.height - outerBleeding,
                                    cornerRadius = CornerRadius(with(density) { 2.mm.toPx() })
                                )
                            )
                        }
                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(SolidColor(bgColor), alpha = if (hideBleed) 1f else 0.2f)
                        }
                    }
                }
            }
        }
    }

}