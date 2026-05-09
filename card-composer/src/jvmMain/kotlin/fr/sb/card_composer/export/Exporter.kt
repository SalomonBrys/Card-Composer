package fr.sb.card_composer.export

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import fr.sb.card_composer.Card
import kotlinx.coroutines.flow.Flow

internal sealed interface Exporter {
    val name: String

    @Composable
    fun ColumnScope.Config()
    fun isConfigValid(): Boolean

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
