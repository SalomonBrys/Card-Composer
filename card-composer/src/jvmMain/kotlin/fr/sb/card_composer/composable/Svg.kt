package fr.sb.card_composer.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readBytes


@Composable
public fun rememberSvgPainter(path: Path): Painter {
    val density = LocalDensity.current
    return remember(path, density) {
        path.readBytes().decodeToSvgPainter(density)
    }
}

@Composable
public fun rememberSvgPainter(path: String): Painter = rememberSvgPainter(Path(path))
