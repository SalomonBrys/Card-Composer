package fr.sb.card_composer.export

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import fr.sb.card_composer.Card
import fr.sb.card_composer.MM_PER_INCH
import kotlinx.coroutines.flow.Flow

internal sealed interface Exporter {
    val name: String
    @Composable
    fun ColumnScope.Config()
    fun export(cards: List<Card>): Flow<Progress>

    data class Progress(
        val current: Int,
        val count: Int,
    ) {
        fun progress() = current.toFloat() / count.toFloat()

        companion object {
            val Initial = Progress(0, Int.MAX_VALUE)
        }
    }
}

internal const val PIXEL_PER_INCH = 300
internal const val PIXEL_PER_MM = PIXEL_PER_INCH / MM_PER_INCH
