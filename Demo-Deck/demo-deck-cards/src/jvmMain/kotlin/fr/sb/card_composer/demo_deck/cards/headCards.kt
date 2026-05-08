package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import fr.sb.card_composer.Card
import fr.sb.card_composer.composable.Image
import fr.sb.card_composer.demo_deck.Suits
import fr.sb.card_composer.demo_deck.cardSize
import fr.sb.card_composer.demo_deck.theme
import fr.sb.card_composer.demo_deck.widget.SuitedCorner
import kotlin.io.path.Path


fun headCards(): List<Card> = Suits.entries.flatMap { suit ->
    listOf("J", "Q", "K").map { value ->
        Card(
            size = cardSize.portrait,
            theme = theme.merge(
                textStyle = TextStyle(color = suit.color)
            ),
            group = "Heads",
            name = "${suit.symbol}${value}",
            back = cardBack,
        ) {
            Image(
                path = Path("assets/images/Heads/${value}.png"),
                colorFilter = ColorFilter.tint(color = suit.color),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(.8f)
                    .fillMaxHeight(.55f)
            )
            listOf(
                Modifier.align(Alignment.TopStart),
                Modifier.align(Alignment.TopEnd),
                Modifier.align(Alignment.BottomStart).rotate(180f),
                Modifier.align(Alignment.BottomEnd).rotate(180f),
            ).forEach {
                SuitedCorner(
                    value = value,
                    suit = suit.symbol,
                    modifier = it
                )
            }
        }
    }
}