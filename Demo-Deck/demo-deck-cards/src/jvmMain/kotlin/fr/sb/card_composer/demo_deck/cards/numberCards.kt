package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
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

fun numberCards(): List<Card> = Suits.entries.flatMap { suit ->
    listOf(
        2 to listOf(1, 1),
        3 to listOf(2, 1),
        4 to listOf(2, 2),
        5 to listOf(2, 1, 2),
        6 to listOf(2, 2, 2),
        7 to listOf(2, 3, 2),
        8 to listOf(3, 2, 3),
        9 to listOf(3, 3, 3),
        10 to listOf(3, 4, 3),
    ).map { (value, lines) ->
        Card(
            size = cardSize.portrait,
            theme = theme.copy(
                textStyle = theme.textStyle.copy(color = suit.color)
            ),
            group = "Numbers",
            name = "${suit.symbol}${value}",
            back = cardBack,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.mm),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                CompositionLocalProvider(
                    CardTheme.Companion provides CardTheme.current.copy(
                        textStyle = CardTheme.current.textStyle.copy(fontSize = 32.sp)
                    )
                ) {
                    lines.forEachIndexed { index, line ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.mm),
                            modifier = if (index == lines.lastIndex) Modifier.rotate(180f) else Modifier.Companion
                        ) {
                            repeat(line) {
                                val density = LocalDensity.current
                                Box {
                                    HorizontallyHatched(
                                        angle = -22.5f,
                                        verticalGap = with(density) { .6.mm.toPx() },
                                        strokeWidth = with(density) { .2.mm.toPx() }
                                    ) {
                                        Text(
                                            text = suit.symbol,
                                            color = suit.color,
                                            modifier = Modifier.hatched()
                                        )
                                    }
                                    Text(
                                        text = suit.symbol,
                                        style = CardTheme.current.textStyle.copy(
                                            drawStyle = Stroke(with(density) { .1.mm.toPx() })
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            listOf(
                Modifier.align(Alignment.TopStart),
                Modifier.align(Alignment.TopEnd),
                Modifier.align(Alignment.BottomStart).rotate(180f),
                Modifier.align(Alignment.BottomEnd).rotate(180f),
            ).forEach {
                SuitedCorner(
                    value = value.toString(),
                    suit = suit.symbol,
                    compress = value == 10,
                    modifier = it
                )
            }
        }
    }
}