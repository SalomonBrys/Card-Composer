package fr.sb.card_composer.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import fr.sb.card_composer.mm
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readBytes


@Composable
private fun MissingImage(
    fileName: String,
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color.Gray.copy(alpha = 0.5f))
            .drawBehind {
                val pad = 2.mm.toPx()
                drawLine(Color.Black, Offset(0 + pad, 0 + pad), Offset(size.width - pad, size.height - pad), 0.5.mm.toPx(), StrokeCap.Round, alpha = 0.25f)
                drawLine(Color.Black, Offset(0 + pad, size.height - pad), Offset(size.width - pad, 0 + pad), 0.5.mm.toPx(), StrokeCap.Round, alpha = 0.25f)
            }
    ) {
        BasicText(fileName, style = TextStyle(fontSize = 10.sp))
    }
}

@Composable
public fun rememberSvgPainter(
    path: Path,
): Painter {
    val density = LocalDensity.current
    return remember(path, density) {
        path.readBytes().decodeToSvgPainter(density)
    }
}

@Composable
public fun rememberSvgPainter(
    path: String,
): Painter = rememberSvgPainter(Path(path))

@Composable
public fun Svg(
    path: Path,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    if (!path.exists()) {
        MissingImage(path.name, modifier)
        return
    }

    Image(
        painter = rememberSvgPainter(path),
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

@Composable
public fun rememberBitmapPainter(
    path: Path,
    filterQuality: FilterQuality = FilterQuality.High,
): Painter {
    return remember(path) {
        val bitmap = path.readBytes().decodeToImageBitmap()
        BitmapPainter(bitmap, filterQuality = filterQuality)
    }
}

@Composable
public fun rememberBitmapPainter(
    path: String,
    filterQuality: FilterQuality = FilterQuality.High,
): Painter = rememberBitmapPainter(Path(path), filterQuality)

@Composable
public fun Image(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.High,
) {
    Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

@Composable
public fun Image(
    path: Path,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.High,
) {
    if (!path.exists()) {
        MissingImage(path.name, modifier)
        return
    }

    Image(
        painter = if (path.extension == "svg") rememberSvgPainter(path) else rememberBitmapPainter(path, filterQuality),
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

@Composable
public fun Image(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    Image(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

@Composable
public fun Image(
    painter: Painter,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

