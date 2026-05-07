package fr.sb.card_composer.demo_deck

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.CardTheme
import fr.sb.card_composer.cardComposerApplication
import fr.sb.card_composer.demo_deck.cards.aceCards
import fr.sb.card_composer.demo_deck.cards.headCards
import fr.sb.card_composer.demo_deck.cards.jokerCards
import fr.sb.card_composer.demo_deck.cards.numberCards
import fr.sb.card_composer.demo_deck.cards.tichuCards


val cardSize = CardSize.Poker

val theme = CardTheme(
    cardBackground = Color(0xFFF7E1DE),
    textStyle = TextStyle(
        lineHeight = 0.8.em,
    ),
)

enum class Suits(
    val symbol: String,
    val color: Color,
) {
    Spade(
        symbol = "♠",
        color = Color(0xFF4B66B9),
    ),
    Heart(
        symbol = "♥",
        color = Color(0xFFD83838),
    ),
    Club(
        symbol = "♣",
        color = Color(0xFF308918),
    ),
    Diamond(
        symbol = "♦",
        color = Color(0xFFCB6D1A),
    ),
}

fun main() = cardComposerApplication(
    title = "Kod-7",
    cards = {
        buildList {
            addAll(numberCards())
            addAll(headCards())
            addAll(aceCards())
            addAll(jokerCards())
            addAll(tichuCards())
        }
    },
)
