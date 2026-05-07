package fr.sb.card_composer.export

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.sb.card_composer.Card
import fr.sb.card_composer.CardFace
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.Pt
import fr.sb.card_composer.PtOffset
import fr.sb.card_composer.PtSize
import fr.sb.card_composer.backFace
import fr.sb.card_composer.frontFace
import fr.sb.card_composer.fullName
import fr.sb.card_composer.inch
import fr.sb.card_composer.mm
import fr.sb.card_composer.pt
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

    enum class Format(val size: PtSize) {
        A5(PtSize(148.mm, 210.mm)),
        A4(PtSize(210.mm, 297.mm)),
        A3(PtSize(297.mm, 420.mm)),
        Letter(PtSize(8.5.inch, 11.inch)),
        Legal(PtSize(8.5.inch, 14.inch)),
        Tabloid(PtSize(11.inch, 17.inch)),
    }
    private var format: Format by mutableStateOf(Format.A4)

    enum class Orientation { Portrait, Landscape }
    private var orientation: Orientation by mutableStateOf(Orientation.Portrait)

    private var withBacks by mutableStateOf(true)
    private var forcePageOrientation by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ColumnScope.Config() {
        var formatExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = formatExpanded,
            onExpandedChange = { formatExpanded = it },
            modifier = Modifier.width(160.dp)
        ) {
            OutlinedTextField(
                value = format.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .pointerHoverIcon(PointerIcon.Default, true)
            )
            ExposedDropdownMenu(
                expanded = formatExpanded,
                onDismissRequest = { formatExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("A5") },
                    onClick = {
                        format = Format.A5
                        formatExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("A4") },
                    onClick = {
                        format = Format.A4
                        formatExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("A3") },
                    onClick = {
                        format = Format.A3
                        formatExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("US Letter") },
                    onClick = {
                        format = Format.Letter
                        formatExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("US Legal") },
                    onClick = {
                        format = Format.Legal
                        formatExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("US Tabloid") },
                    onClick = {
                        format = Format.Tabloid
                        formatExpanded = false
                    },
                )
            }
        }

        var orientationExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = orientationExpanded,
            onExpandedChange = { orientationExpanded = it },
            modifier = Modifier.width(160.dp)
        ) {
            OutlinedTextField(
                value = orientation.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = orientationExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .pointerHoverIcon(PointerIcon.Default, true)
            )
            ExposedDropdownMenu(
                expanded = orientationExpanded,
                onDismissRequest = { orientationExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Portrait") },
                    onClick = {
                        orientation = Orientation.Portrait
                        orientationExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Landscape") },
                    onClick = {
                        orientation = Orientation.Landscape
                        orientationExpanded = false
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

    private val margin = 5.mm

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
            val pageSize = when (orientation) {
                Orientation.Portrait -> format.size
                Orientation.Landscape -> PtSize(format.size.height, format.size.width)
            }
            val pageSafeSize = PtSize(pageSize.width - margin * 2, pageSize.height - margin * 2)
            val originalCardSize = cards.first().size
            val originalCardOrientation = if (CardSize.isPortrait(originalCardSize)) Orientation.Portrait else Orientation.Landscape
            val rotate = forcePageOrientation && (orientation != originalCardOrientation)
            val cardSize =
                if (rotate) PtSize(originalCardSize.height, originalCardSize.width)
                else originalCardSize
            val cardsPerRow = floor(pageSafeSize.width / cardSize.width).toInt()
            require(cardsPerRow > 0) { "$groupName's cards width is too large to fit on the requested page width (${pageSafeSize.width.value} pt)" }
            val rowsPerPage = floor(pageSafeSize.height / cardSize.height).toInt()
            require(rowsPerPage > 0) { "$groupName's cards height is too large to fit on the requested page width (${pageSafeSize.height.value} pt)" }

            val contentSize = PtSize(
                width = cardSize.width * cardsPerRow,
                height = cardSize.height * rowsPerPage,
            )
            val topLeft = PtOffset(
                x = (pageSize.width - contentSize.width) / 2,
                y = (pageSize.height - contentSize.height) / 2,
            )
            val pageFaceMaker = PageFaceMaker(
                document = this@addGroupPages,
                pageSize = pageSize,
                topLeft = topLeft,
                cardSize = cardSize,
                bleedSize = CardSize.standardSafeMargin,
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
        val pageSize: PtSize,
        val topLeft: PtOffset,
        val cardSize: PtSize,
        val bleedSize : Pt,
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
                val page = PDPage(PDRectangle(pageSize.width.value, pageSize.height.value))

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

                            var x: Pt =
                                if (invertX) topLeft.x + cardSize.width * (cardsPerRow - cardIndex - 1)
                                else topLeft.x + cardSize.width * cardIndex
                            var y: Pt = topLeft.y + cardSize.height * (rowsPerPage - rowIndex - 1) // Y is inverted on PDFs

                            if (render.bleed.left) x -= bleedSize
                            if (render.bleed.bottom) y -= bleedSize // Y is inverted on PDFs

                            val width: Pt = cardSize.width + bleedSize * render.bleed.horizontalBleeds
                            val height: Pt = cardSize.height + bleedSize * render.bleed.verticalBleeds

                            stream.drawImage(image, x.value, y.value, width.value, height.value)
                        }
                    }

                    if (withMarks) {
                        stream.setLineWidth(0.5.mm.value)
                        stream.setStrokingColor(0.0f, 0.0f, 0.0f)
                        val markLength = 5.mm

                        repeat(cardsPerRow + 1) {
                            stream.moveTo((topLeft.x + cardSize.width * it).value, (page.cropBox.upperRightY.pt - topLeft.y + bleedSize).value)
                            stream.lineTo((topLeft.x + cardSize.width * it).value, (page.cropBox.upperRightY.pt - topLeft.y + bleedSize + markLength).value)
                            stream.stroke()

                            stream.moveTo((topLeft.x + cardSize.width * it).value, (topLeft.y - bleedSize).value)
                            stream.lineTo((topLeft.x + cardSize.width * it).value, (topLeft.y - bleedSize - markLength).value)
                            stream.stroke()
                        }

                        repeat(rowsPerPage + 1) {
                            stream.moveTo((topLeft.x - bleedSize).value, (topLeft.y + cardSize.height * it).value)
                            stream.lineTo((topLeft.x - bleedSize - markLength).value, (topLeft.y + cardSize.height * it).value)
                            stream.stroke()

                            stream.moveTo((page.cropBox.upperRightX.pt - topLeft.x + bleedSize).value, (topLeft.y + cardSize.height * it).value)
                            stream.lineTo((page.cropBox.upperRightX.pt - topLeft.x + bleedSize + markLength).value, (topLeft.y + cardSize.height * it).value)
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
