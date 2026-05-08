package fr.sb.card_composer.demo_deck.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import demo_deck.demo_deck_cards.generated.resources.Res
import demo_deck.demo_deck_cards.generated.resources.tichu_dog
import demo_deck.demo_deck_cards.generated.resources.tichu_dragon
import demo_deck.demo_deck_cards.generated.resources.tichu_phoenix
import fr.sb.card_composer.Card
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.CardTheme
import fr.sb.card_composer.composable.Icon
import fr.sb.card_composer.composable.Text
import fr.sb.card_composer.mm
import fr.sb.card_composer.demo_deck.cardSize
import fr.sb.card_composer.demo_deck.theme
import fr.sb.card_composer.demo_deck.widget.HorizontallyHatched
import org.jetbrains.compose.resources.imageResource

fun tichuCards(): List<Card> {
    val tichuTheme = theme.merge(
        textStyle = TextStyle(color = Color.Black)
    )
    return listOf(
        Card(
            size = cardSize.portrait,
            theme = tichuTheme,
            group = "Tichu",
            name = "One",
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
                        text = "1",
                        fontSize = 128.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.hatched()
                    )
                }
                Text(
                    text = "1",
                    fontSize = 128.sp,
                    fontWeight = FontWeight.Black,
                    drawStyle = Stroke(with(density) { .1.mm.toPx() }),
                )
            }
            listOf(
                Modifier.align(Alignment.TopStart),
                Modifier.align(Alignment.TopEnd),
                Modifier.align(Alignment.BottomStart).rotate(180f),
                Modifier.align(Alignment.BottomEnd).rotate(180f),
            ).forEach {
                Text(
                    text = "1",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = it
                        .padding(CardSize.safePadding)
                        .width(7.mm)
                        .graphicsLayer { translationY = -1.8.mm.toPx() }
                )
            }
        }
    ) + listOf(
        Triple("Dog", Icons.Rounded.Pets, Res.drawable.tichu_dog),
        Triple("Phoenix", Icons.Rounded.AutoAwesome, Res.drawable.tichu_phoenix),
        Triple("Dragon", Icons.Rounded.Whatshot, Res.drawable.tichu_dragon),
    ).map { (name, icon, drawable) ->
        Card(
            size = cardSize.portrait,
            theme = tichuTheme,
            group = "Tichu",
            name = name,
            back = cardBack,
        ) {
            Image(
                bitmap = imageResource(drawable),
                contentDescription = null,
                colorFilter = ColorFilter.tint(CardTheme.textStyle.color),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(.9f)
                    .fillMaxWidth(.75f)
            )
            listOf(
                Modifier.align(Alignment.TopStart),
                Modifier.align(Alignment.TopEnd),
                Modifier.align(Alignment.BottomStart).rotate(180f),
                Modifier.align(Alignment.BottomEnd).rotate(180f),
            ).forEach {
                Icon(
                    icon = icon,
                    modifier = it
                        .padding(CardSize.safePadding)
                        .padding(top = 1.mm)
                        .size(8.mm)
                )
            }
        }
    }
}