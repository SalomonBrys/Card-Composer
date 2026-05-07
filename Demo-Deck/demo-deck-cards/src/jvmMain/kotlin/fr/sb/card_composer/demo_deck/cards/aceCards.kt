package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.layout.Box
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
import fr.sb.card_composer.demo_deck.cardSize
import fr.sb.card_composer.demo_deck.theme
import fr.sb.card_composer.demo_deck.widget.HorizontallyHatched
import fr.sb.card_composer.demo_deck.widget.SuitedCorner

fun aceCards() = Suits.entries.map { suit ->
    Card(
        size = cardSize.portrait,
        theme = theme.copy(
            textStyle = theme.textStyle.copy(color = suit.color)
        ),
        group = "Aces",
        name = "${suit.symbol}A",
        back = cardBack,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            val density = LocalDensity.current
            HorizontallyHatched(
                verticalGap = with(density) { .6.mm.toPx() },
                strokeWidth = with(density) { .2.mm.toPx() },
                angle = -22.5f,
            ) {
                Text(
                    text = suit.symbol,
                    fontSize = 128.sp,
                    modifier = Modifier.hatched()
                )
            }
            Text(
                text = suit.symbol,
                fontSize = 128.sp,
                style = CardTheme.current.textStyle.copy(
                    drawStyle = Stroke(with(density) { .1.mm.toPx() })
                )
            )
        }
        listOf(
            Modifier.align(Alignment.TopStart),
            Modifier.align(Alignment.TopEnd),
            Modifier.align(Alignment.BottomStart).rotate(180f),
            Modifier.align(Alignment.BottomEnd).rotate(180f),
        ).forEach {
            SuitedCorner(
                value = "A",
                suit = suit.symbol,
                modifier = it
            )
        }
    }
}