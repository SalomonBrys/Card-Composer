package fr.sb.card_composer.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import fr.sb.card_composer.CardTheme


@Composable
private fun Modifier.defaultSizeFor(painter: Painter): Modifier {
    val density = LocalDensity.current
    val fontSize = CardTheme.textStyle.fontSize
    return this.then(
        if (fontSize.isSp) {
            Modifier.size(with(density) { fontSize.toDp() })
        } else {
            Modifier.size(with(density) { 16.sp.toDp() })
        }
    )
}

@Composable
public fun Icon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = CardTheme.textStyle.color,
) {
    Icon(
        painter = rememberVectorPainter(icon),
        modifier = modifier,
        tint = tint,
    )
}

@Composable
public fun Icon(
    painter: Painter,
    modifier: Modifier = Modifier,
    tint: Color = CardTheme.textStyle.color,
) {
    val colorFilter =
        remember(tint) { if (tint == Color.Unspecified) null else ColorFilter.tint(tint) }
    Box(
        modifier
            .toolingGraphicsLayer()
            .defaultSizeFor(painter)
            .paint(painter, colorFilter = colorFilter, contentScale = ContentScale.Fit)
    )
}