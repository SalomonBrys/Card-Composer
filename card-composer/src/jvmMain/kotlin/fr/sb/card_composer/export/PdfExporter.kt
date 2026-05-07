package fr.sb.card_composer.export

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.sb.card_composer.Card
import fr.sb.card_composer.CardFace
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.PT_PER_MM
import fr.sb.card_composer.backFace
import fr.sb.card_composer.frontFace
import fr.sb.card_composer.fullName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.math.floor

internal object PdfExporter : Exporter {
    override val name: String get() = "PDF"

    private var orientation: Orientation by mutableStateOf(Orientation.PORTRAIT)

    private var withBacks by mutableStateOf(true)
    private var forcePageOrientation by mutableStateOf(false)

    enum class Orientation { PORTRAIT, LANDSCAPE }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ColumnScope.Config() {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.width(160.dp)
        ) {
            OutlinedTextField(
                value = orientation.name.lowercase().replaceFirstChar { it.uppercase() } ,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .pointerHoverIcon(PointerIcon.Default, true)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Portrait") },
                    onClick = {
                        orientation = Orientation.PORTRAIT
                        expanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Landscape") },
                    onClick = {
                        orientation = Orientation.LANDSCAPE
                        expanded = false
                    },
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = withBacks,
                onCheckedChange = { withBacks = it }
            )
            Text("Include backs")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = forcePageOrientation,
                onCheckedChange = { forcePageOrientation = it }
            )
            Text("Force image orientation")
        }

    }

    private val A4_MM = Size(210f, 297f)
    private val MARGIN_MM = 5

    override fun export(cards: List<Card>): Flow<Exporter.Progress> =
        flow {
            val groups = cards.groupByTo(LinkedHashMap()) { it.group ?: "" }

            groups.forEach { (groupName, group) ->
                require(group.map { it.size }.distinct().size == 1) { "All cards of group $groupName be of the same size" }
            }

            val outputPath = requestPDFBlocking() ?: return@flow

            var count = 0
            PDDocument().use { doc ->
                groups.forEach { (groupName, group) ->
                    doc.addGroupPages(groupName, group).collect { progress ->
                        count += progress.current
                        emit(Exporter.Progress(count, cards.size))
                    }
                }
                doc.save(outputPath.absolutePathString())
            }
        }.flowOn(Dispatchers.IO)

    private fun PDDocument.addGroupPages(
        groupName: String,
        cards: List<Card>,
    ): Flow<Exporter.Progress> =
        flow {
            val pageSizeMm = when (orientation) {
                Orientation.PORTRAIT -> Size(A4_MM.width, A4_MM.height)
                Orientation.LANDSCAPE -> Size(A4_MM.height, A4_MM.width)
            }
            val pageSafeSizeMm = Size(pageSizeMm.width - 2 * MARGIN_MM, pageSizeMm.height - 2 * MARGIN_MM)
            val originalCardSize = cards.first().size
            val originalCardOrientation = if (CardSize.isPortrait(originalCardSize)) Orientation.PORTRAIT else Orientation.LANDSCAPE
            val rotate = forcePageOrientation && (orientation != originalCardOrientation)
            val cardSizeMm =
                if (rotate) Size(originalCardSize.height.value, originalCardSize.width.value)
                else Size(originalCardSize.width.value, originalCardSize.height.value)
            val cardsPerRow = floor(pageSafeSizeMm.width / cardSizeMm.width).toInt()
            require(cardsPerRow > 0) { "$groupName's cards width is too large to fit on the requested page width (${pageSafeSizeMm.width})" }
            val rowsPerPage = floor(pageSafeSizeMm.height / cardSizeMm.height).toInt()
            require(rowsPerPage > 0) { "$groupName's cards height is too large to fit on the requested page width (${pageSafeSizeMm.height})" }

            val contentSizeMm = Size(
                width = cardsPerRow * cardSizeMm.width,
                height = rowsPerPage * cardSizeMm.height,
            )
            val topLeftMm = Offset(
                x = (pageSizeMm.width - contentSizeMm.width) / 2,
                y = (pageSizeMm.height - contentSizeMm.height) / 2,
            )
            val topLeftPt = Offset(
                x = topLeftMm.x * PT_PER_MM,
                y = topLeftMm.y * PT_PER_MM,
            )
            val cardSizePt = Size(
                width = cardSizeMm.width * PT_PER_MM,
                height = cardSizeMm.height * PT_PER_MM,
            )
            val bleedSizePt = CardSize.standardSafeMargin.value * PT_PER_MM
            val pageFaceMaker = PageFaceMaker(
                document = this@addGroupPages,
                pageSizeMm = pageSizeMm,
                topLeftPt = topLeftPt,
                cardSizePt = cardSizePt,
                bleedSizePt = bleedSizePt,
                cardsPerRow = cardsPerRow,
                rowsPerPage = rowsPerPage,
            )

            buildList {
                for (card in cards) {
                    repeat(card.count) {
                        add(card)
                    }
                }
            }.chunked(cardsPerRow).chunked(rowsPerPage).forEach { rows ->
                var count = 0
                pageFaceMaker.addFacesPage(
                    rows = rows.map { row -> row.map { "${it.fullName} - Front" to it.frontFace } },
                    invertX = false,
                    withMarks = true,
                    rotation = if (rotate) PngExporter.Rotation.Clockwise else null
                )
                if (withBacks) {
                    pageFaceMaker.addFacesPage(
                        rows = rows.map { row -> row.map { "${it.fullName} - Back" to it.backFace } },
                        invertX = true,
                        withMarks = false,
                        rotation = if (rotate) PngExporter.Rotation.CounterClockwise else null
                    )
                }
                count += rows.sumOf { it.size }
                emit(Exporter.Progress(count, cards.size))
            }
        }

    private class PageFaceMaker(
        val document: PDDocument,
        val pageSizeMm: Size,
        val topLeftPt: Offset,
        val cardSizePt: Size,
        val bleedSizePt : Float,
        val cardsPerRow: Int,
        val rowsPerPage: Int,
    ) {
        suspend fun addFacesPage(
            rows: List<List<Pair<String, CardFace>>>,
            invertX: Boolean,
            rotation: PngExporter.Rotation?,
            withMarks: Boolean,
        ) {
            coroutineScope {
                val page = PDPage(PDRectangle(pageSizeMm.width * PT_PER_MM, pageSizeMm.height * PT_PER_MM))

                class Render(
                    val name: String,
                    val png: Deferred<ByteArray>,
                    val bleed: PngExporter.Bleed,
                )

                val pngRows = rows.mapIndexed { rowIndex, row ->
                    row.mapIndexed { cardIndex, (name, face) ->
                        val bleed = PngExporter.Bleed(
                            top = rowIndex == 0,
                            bottom = (
                                    // last row
                                    rowIndex == rows.lastIndex
                                    // Second to last row with nothing under
                                ||  (rowIndex == rows.lastIndex - 1 && cardIndex !in rows.last().indices)
                            ),
                            left = if (invertX) cardIndex == row.lastIndex else cardIndex == 0,
                            right = if (invertX) cardIndex == 0 else cardIndex == row.lastIndex,
                        )
                        Render(
                            name = name,
                            png = async(Dispatchers.Default) {
                                PngExporter.renderCardFaceBitmap(
                                    face = face,
                                    bleed = bleed,
                                    rotation = rotation,
                                )
                            },
                            bleed = bleed,
                        )
                    }
                }

                PDPageContentStream(document, page).use { stream ->
                    pngRows.forEachIndexed { rowIndex, row ->
                        row.forEachIndexed { cardIndex, render ->
                            val image = PDImageXObject.createFromByteArray(document, render.png.await(), "${render.name}.png")

                            var x =
                                if (invertX) topLeftPt.x + (cardsPerRow - cardIndex - 1) * cardSizePt.width
                                else topLeftPt.x + cardIndex * cardSizePt.width
                            var y = topLeftPt.y + (rowsPerPage - rowIndex - 1) * cardSizePt.height // Y is inverted on PDFs

                            if (render.bleed.left) x -= bleedSizePt
                            if (render.bleed.bottom) y -= bleedSizePt // Y is inverted on PDFs

                            val width = cardSizePt.width + bleedSizePt * render.bleed.horizontalBleeds
                            val height = cardSizePt.height + bleedSizePt * render.bleed.verticalBleeds

                            stream.drawImage(image, x, y, width, height)
                        }
                    }

                    if (withMarks) {
                        stream.setLineWidth(0.5f * PT_PER_MM)
                        stream.setStrokingColor(0.0f, 0.0f, 0.0f)
                        val markLengthPt = 5 * PT_PER_MM

                        repeat(cardsPerRow + 1) {
                            stream.moveTo(topLeftPt.x + it * cardSizePt.width, page.cropBox.upperRightY - topLeftPt.y + bleedSizePt)
                            stream.lineTo(topLeftPt.x + it * cardSizePt.width, page.cropBox.upperRightY - topLeftPt.y + bleedSizePt + markLengthPt)
                            stream.stroke()

                            stream.moveTo(topLeftPt.x + it * cardSizePt.width, topLeftPt.y - bleedSizePt)
                            stream.lineTo(topLeftPt.x + it * cardSizePt.width, topLeftPt.y - bleedSizePt - markLengthPt)
                            stream.stroke()
                        }

                        repeat(rowsPerPage + 1) {
                            stream.moveTo(topLeftPt.x - bleedSizePt, topLeftPt.y + it * cardSizePt.height)
                            stream.lineTo(topLeftPt.x - bleedSizePt - markLengthPt, topLeftPt.y + it * cardSizePt.height)
                            stream.stroke()

                            stream.moveTo(page.cropBox.upperRightX - topLeftPt.x + bleedSizePt, topLeftPt.y + it * cardSizePt.height)
                            stream.lineTo(page.cropBox.upperRightX - topLeftPt.x + bleedSizePt + markLengthPt, topLeftPt.y + it * cardSizePt.height)
                            stream.stroke()
                        }
                    }
                }
                document.addPage(page)
            }
        }
    }

    private fun requestPDFBlocking(): Path? {
        val dialog = FileDialog(null as Frame?, "Output Directory", FileDialog.SAVE)
        dialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".pdf") }
        dialog.file = "cards.pdf"
        dialog.isVisible = true
        val file = dialog.files.singleOrNull() ?: return null
        if (file.extension != "pdf") return Path(file.absolutePath + ".pdf")
        return Path(file.absolutePath)
    }

}
