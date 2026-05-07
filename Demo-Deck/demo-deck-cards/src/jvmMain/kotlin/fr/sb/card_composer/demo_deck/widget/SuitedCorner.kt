package fr.sb.card_composer.demo_deck.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import fr.sb.card_composer.CardSize
import fr.sb.card_composer.composable.Text
import fr.sb.card_composer.mm


@Composable
fun SuitedCorner(
    value: String,
    suit: String,
    modifier: Modifier = Modifier,
    compress: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(CardSize.safePadding)
            .width(7.mm)
            .wrapContentWidth(unbounded = true)
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = if (compress) -4.sp else TextUnit.Unspecified,
            modifier = Modifier
                .then(
                    if (compress) Modifier.graphicsLayer {
                        scaleX = .75f
                        translationX = -.6.mm.toPx()
                    }
                    else Modifier
                )
                .graphicsLayer { translationY = -1.8.mm.toPx() }
        )
        Text(
            text = suit,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .graphicsLayer { translationY = -4.3.mm.toPx() }
        )
    }
}
