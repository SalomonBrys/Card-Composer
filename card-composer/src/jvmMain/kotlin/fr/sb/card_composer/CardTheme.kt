package fr.sb.card_composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily


public data class CardTheme(
    val cardBackground: Color,
    val textStyle: TextStyle,
) {

    public companion object {

        public val default: CardTheme = CardTheme(
            cardBackground = Color.White,
            textStyle = TextStyle(
                fontFamily = FontFamily.Default,
                color = Color.Black,
            ),
        )

        private val local = staticCompositionLocalOf<CardTheme> { default }

        public infix fun provides(theme: CardTheme): ProvidedValue<CardTheme> = local provides theme

        public val current: CardTheme @Composable get() = local.current

        public val cardBackground: Color @Composable get() = current.cardBackground
        public val textStyle: TextStyle @Composable get() = current.textStyle
    }

    public fun merge(
        cardBackground: Color = Color.Unspecified,
        textStyle: TextStyle? = null,
    ): CardTheme {
        return copy(
            cardBackground = cardBackground.takeOrElse { this.cardBackground },
            textStyle = this.textStyle.merge(textStyle)
        )
    }
}
