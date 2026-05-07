package fr.sb.card_composer.demo_deck.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan


interface HatchedScope : BoxScope {
    fun Modifier.hatched(): Modifier
}

@Composable
fun HorizontallyHatched(
    verticalGap: Float,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    strokeWidth: Float = Stroke.HairlineWidth,
    pathEffect: PathEffect? = null,
    angle: Float = -45f,
    content: @Composable HatchedScope.() -> Unit,
) {

    Box(
        contentAlignment = contentAlignment,
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds()
                .drawBehind {
                    val h = tan(angle * PI / 180f).toFloat() * size.width
                    var y = -abs(h)
                    while (y < size.height + abs(h)) {
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, y),
                            end = Offset(size.width, y + h),
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect,
                        )
                        y += verticalGap
                    }
                }
        )
        object : HatchedScope, BoxScope by this {
            override fun Modifier.hatched(): Modifier =
                graphicsLayer {
                    blendMode = BlendMode.SrcIn
                }
        }.content()
    }
}
