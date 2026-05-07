package fr.sb.card_composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
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
    }
}
