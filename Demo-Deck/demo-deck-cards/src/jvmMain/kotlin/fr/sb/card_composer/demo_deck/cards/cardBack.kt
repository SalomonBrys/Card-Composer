package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import fr.sb.card_composer.Card
import fr.sb.card_composer.CardTheme
import fr.sb.card_composer.composable.Text
import fr.sb.card_composer.mm
import fr.sb.card_composer.demo_deck.Suits
import fr.sb.card_composer.demo_deck.theme
import fr.sb.card_composer.demo_deck.widget.HorizontallyHatched

val cardBack = Card.Back(
    id = "play",
    theme = theme,
) {
    val density = LocalDensity.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.mm),
        modifier = Modifier
            .align(Alignment.Center)
    ) {
        listOf(Modifier, Modifier.rotate(180f)).forEach {
            Column(it) {
                Suits.entries.forEach { suit ->
                    Box(
                        modifier = Modifier
                    ) {
                        HorizontallyHatched(
                            angle = 22.5f,
                            verticalGap = with(density) { .6.mm.toPx() },
                            strokeWidth = with(density) { .2.mm.toPx() }
                        ) {
                            Text(
                                text = suit.symbol,
                                color = suit.color,
                                fontSize = 48.sp,
                                modifier = Modifier.hatched()
                            )
                        }
                        Text(
                            text = suit.symbol,
                            color = suit.color,
                            fontSize = 48.sp,
                            drawStyle = Stroke(with(density) { .2.mm.toPx() }),
                        )
                    }
                }
            }
        }
    }
}