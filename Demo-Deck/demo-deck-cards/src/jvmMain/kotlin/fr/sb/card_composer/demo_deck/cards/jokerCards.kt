package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import demo_deck.demo_deck_cards.generated.resources.Res
import demo_deck.demo_deck_cards.generated.resources.joker
import fr.sb.card_composer.Card
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.composable.Icon
import fr.sb.card_composer.mm
import fr.sb.card_composer.demo_deck.Suits
import fr.sb.card_composer.demo_deck.cardSize
import fr.sb.card_composer.demo_deck.theme
import org.jetbrains.compose.resources.imageResource

fun jokerCards() =
    listOf(
        "Black" to Modifier
            .graphicsLayer { colorFilter = ColorFilter.tint(color = Color.DarkGray) },
        "Colored" to Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.linearGradient(colors = (Suits.entries + Suits.entries).map { it.color }),
                    blendMode = BlendMode.SrcIn,
                )
            }
    ).map { (name, color) ->
        Card(
            size = cardSize.portrait,
            theme = theme,
            group = "Jokers",
            name = "$name joker",
            back = cardBack,
        ) {
            Image(
                bitmap = imageResource(Res.drawable.joker),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(.8f)
                    .then(color)
            )
            listOf(
                Modifier.align(Alignment.TopStart),
                Modifier.align(Alignment.TopEnd),
                Modifier.align(Alignment.BottomStart).rotate(180f),
                Modifier.align(Alignment.BottomEnd).rotate(180f),
            ).forEach {
                Icon(
                    icon = Icons.Rounded.Stars,
                    modifier = it
                        .padding(CardSize.safePadding)
                        .padding(top = 1.mm)
                        .size(8.mm)
                        .then(color)
                )
            }
        }
    }