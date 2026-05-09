package fr.sb.card_composer.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import fr.sb.card_composer.Card
import fr.sb.card_composer.CardFace
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.CardTheme
import fr.sb.card_composer.LocalCardSize
import fr.sb.card_composer.PT_PER_INCH
import fr.sb.card_composer.backFace
import fr.sb.card_composer.export.PngExporter.Bleed
import fr.sb.card_composer.export.PngExporter.Rotation
import fr.sb.card_composer.frontFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Path
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

internal object PngExporter : Exporter {

    override val name get() = "PNG"

    private var withBleed by mutableStateOf(true)
    private var forcePortrait by mutableStateOf(false)
    private var pixelPerInch by mutableStateOf("300")

    @Composable
    override fun ColumnScope.Config() {
        OutlinedTextField(
            value = pixelPerInch,
            onValueChange = { pixelPerInch = it },
            label = { Text("Pixel per inch") },
            modifier = Modifier
                .width(128.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = withBleed,
                onCheckedChange = { withBleed = it }
            )
            Text("With bleed")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = forcePortrait,
                onCheckedChange = { forcePortrait = it }
            )
            Text("Force Portrait")
        }
    }

    override fun isConfigValid(): Boolean = pixelPerInch.toIntOrNull()?.takeIf { it > 0 } != null

    @OptIn(ExperimentalAtomicApi::class)
    override fun export(
        cards: List<Card>,
    ): Flow<Exporter.Progress> =
        channelFlow {
            val directory = requestDirectoryBlocking() ?: return@channelFlow
            val faces = ArrayList<Pair<Path, CardFace>>()
            val backIds = HashSet<String>()
            cards.forEach { card ->
                val groupDirectory = if (card.group != null) {
                    directory.resolve(card.group).also { it.createDirectories() }
                } else directory

                val frontPath = groupDirectory.resolve("${card.name} - Front.png")
                faces.add(frontPath to card.frontFace)

                when (card.back.id) {
                    null -> {
                        val backPath = groupDirectory.resolve("${card.name} - Back.png")
                        faces.add(backPath to card.backFace)
                    }

                    !in backIds -> {
                        val backPath = groupDirectory.resolve("Back - ${card.back.id}.png")
                        faces.add(backPath to card.backFace)
                        backIds.add(card.back.id)
                    }
                }
            }
            channel.send(Exporter.Progress(0, faces.count()))

            coroutineScope {
                val countFlow = MutableStateFlow(0)
                val all = faces.map { (path, face) ->
                    launch(Dispatchers.Default) {
                        val pngBytes = renderCardFaceBitmap(
                            face = face,
                            bleed = if (withBleed) Bleed.All else Bleed.None,
                            rotation = if (forcePortrait && CardSize.isLandscape(face.size)) Rotation.Clockwise else null,
                            pixelPerInch = pixelPerInch.toInt(),
                        )
                        path.writeBytes(pngBytes)
                        countFlow.update { it + 1 }
                    }
                }

                val transmit = launch {
                    countFlow.collect { count ->
                        channel.send(Exporter.Progress(count, faces.count()))
                    }
                }

                all.joinAll()
                transmit.cancel()
            }

        }.flowOn(Dispatchers.IO)

    internal data class Bleed(
        val top: Boolean = false,
        val bottom: Boolean = false,
        val left: Boolean = false,
        val right: Boolean = false  ,
    ) {
        companion object {
            val None = Bleed()
            val All = Bleed(top = true, bottom = true, left = true, right = true)
        }

        val horizontalCrops: Int = (if (left) 0 else 1) + (if (right) 0 else 1)
        val verticalCrops: Int = (if (top) 0 else 1) + (if (bottom) 0 else 1)

        val horizontalBleeds: Int = (if (left) 1 else 0) + (if (right) 1 else 0)
        val verticalBleeds: Int = (if (top) 1 else 0) + (if (bottom) 1 else 0)
    }

    enum class Rotation {
        Clockwise, CounterClockwise
    }

    private fun requestDirectoryBlocking(): Path? {
        val p = System.getProperty("apple.awt.fileDialogForDirectories")
        System.setProperty("apple.awt.fileDialogForDirectories", "true")
        val dialog = FileDialog(null as Frame?, "Output Directory", FileDialog.LOAD)
        dialog.isVisible = true
        val file = dialog.files.singleOrNull() ?: return null
        if (p != null) {
            System.setProperty("apple.awt.fileDialogForDirectories", p)
        } else {
            System.clearProperty("apple.awt.fileDialogForDirectories")
        }
        return Path(file.absolutePath)
    }

}

internal suspend fun renderCardFaceBitmap(
    face: CardFace,
    bleed: Bleed,
    rotation: Rotation?,
    pixelPerInch: Int,
): ByteArray =
    withContext(Dispatchers.Default) {
        repeat(10) {
            try {
                return@withContext renderCardFaceBitmapBlocking(face, bleed, rotation, pixelPerInch)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(50.milliseconds)
        }
        error("Failed to render card face bitmap")
    }

private fun renderCardFaceBitmapBlocking(
    face: CardFace,
    bleed: Bleed,
    rotation: Rotation?,
    pixelPerInch: Int,
): ByteArray {
    val pixelPerPt = pixelPerInch / PT_PER_INCH
    val density = Density(pixelPerPt)
    val fullCardSize = CardSize.fullSize(face.size)
    ImageComposeScene(
        width = (fullCardSize.width.value * pixelPerPt).roundToInt(),
        height = (fullCardSize.height.value * pixelPerPt).roundToInt(),
    ) {
        CompositionLocalProvider(
            LocalDensity provides density,
            CardTheme.Companion provides face.theme,
            LocalCardSize provides face.size,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(CardTheme.cardBackground)
            ) {
                face.content(this)
            }
        }
    }.use { scene ->
        // https://youtrack.jetbrains.com/issue/CMP-6227
        scene.render()
        val image = scene.render(0)
        val bitmap = Bitmap().also { bitmap ->
            val outerBleeding = with(density) { CardSize.standardSafeMargin.toPx().roundToInt() }
            bitmap.allocPixels(
                ImageInfo(
                    colorInfo = image.imageInfo.colorInfo,
                    width = image.imageInfo.width - outerBleeding * bleed.horizontalCrops,
                    height = image.imageInfo.height - outerBleeding * bleed.verticalCrops,
                )
            )
            image.readPixels(
                dst = bitmap,
                srcX = if (bleed.left) 0 else outerBleeding,
                srcY = if (bleed.top) 0 else outerBleeding,
            )
        }

        val rotatedBitmap = if (rotation != null) {
            val rotatedBitmap = Bitmap()
            rotatedBitmap.allocPixels(ImageInfo(width = bitmap.height, height = bitmap.width, colorInfo = bitmap.colorInfo))
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    when (rotation) {
                        Rotation.Clockwise -> rotatedBitmap.erase(bitmap.getColor(x, y), IRect.makeXYWH(bitmap.height - 1 - y, x, 1, 1))
                        Rotation.CounterClockwise -> rotatedBitmap.erase(bitmap.getColor(x, y), IRect.makeXYWH(y, bitmap.width - 1 - x, 1, 1))
                    }
                }
            }
            rotatedBitmap
        } else bitmap

        val data = Image.makeFromBitmap(rotatedBitmap).use { image ->
            image.encodeToData(EncodedImageFormat.PNG)!!
        }
        return data.bytes
    }
}
