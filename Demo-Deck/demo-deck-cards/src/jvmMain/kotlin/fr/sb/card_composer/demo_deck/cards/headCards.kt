package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import demo_deck.demo_deck_cards.generated.resources.Res
import demo_deck.demo_deck_cards.generated.resources.head_j
import demo_deck.demo_deck_cards.generated.resources.head_k
import demo_deck.demo_deck_cards.generated.resources.head_q
import fr.sb.card_composer.Card
import fr.sb.card_composer.demo_deck.Suits
import fr.sb.card_composer.demo_deck.cardSize
import fr.sb.card_composer.demo_deck.theme
import fr.sb.card_composer.demo_deck.widget.SuitedCorner
import org.jetbrains.compose.resources.imageResource

fun headCards(): List<Card> = Suits.entries.flatMap { suit ->
    listOf(
        "J" to Res.drawable.head_j,
        "Q" to Res.drawable.head_q,
        "K" to Res.drawable.head_k,
    ).map { (value, drawable) ->
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
                bitmap = imageResource(drawable),
                contentDescription = null,
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