package fr.sb.card_composer

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf


public interface CardSize {
    public val portrait: MmSize
    public val landscape: MmSize

    public companion object {
        public val standardSafeMargin: Mm = 3.18.mm

        public fun fullSize(size: MmSize): MmSize = MmSize(size.width + standardSafeMargin * 2, size.height + standardSafeMargin * 2)

        public val safePadding: Mm get() = standardSafeMargin * 2

        public fun isPortrait(size: MmSize): Boolean = size.width <= size.height
        public fun isLandscape(size: MmSize): Boolean = size.width > size.height
    }

    public object Bridge : CardSize {
        override val portrait: MmSize = MmSize(57.mm, 89.mm)
        override val landscape: MmSize = MmSize(89.mm, 57.mm)
    }

    public object Domino : CardSize {
        override val portrait: MmSize = MmSize(44.mm, 89.mm)
        override val landscape: MmSize = MmSize(89.mm, 44.mm)
    }

    public object MiniEuropean : CardSize {
        override val portrait: MmSize = MmSize(44.mm, 67.mm)
        override val landscape: MmSize = MmSize(67.mm, 44.mm)
    }

    public object MiniUS : CardSize {
        override val portrait: MmSize = MmSize(41.mm, 63.mm)
        override val landscape: MmSize = MmSize(63.mm, 41.mm)
    }

    public object Poker : CardSize {
        override val portrait: MmSize = MmSize(63.mm, 88.mm)
        override val landscape: MmSize = MmSize(88.mm, 63.mm)
    }

    public object Trump : CardSize {
        override val portrait: MmSize = MmSize(62.mm, 100.mm)
        override val landscape: MmSize = MmSize(100.mm, 62.mm)
    }

    public object Square : CardSize {
        override val portrait: MmSize = MmSize(70.mm, 70.mm)
        override val landscape: MmSize = MmSize(70.mm, 70.mm)
    }

    public object SquareSmall : CardSize {
        override val portrait: MmSize = MmSize(50.8.mm, 50.8.mm)
        override val landscape: MmSize = MmSize(50.8.mm, 50.8.mm)
    }

    public object Tarot : CardSize {
        override val portrait: MmSize = MmSize(70.mm, 121.mm)
        override val landscape: MmSize = MmSize(121.mm, 70.mm)
    }
}

public data class Card(
    val size: MmSize,
    val theme: CardTheme,
    val name: String,
    val back: Back,
    val group: String? = null,
    val count: Int = 1,
    val front: @Composable BoxScope.() -> Unit,
) {
    public data class Back(
        val id: String? = null,
        val theme: CardTheme? = null,
        val content: @Composable BoxScope.() -> Unit,
    )
}

internal val Card.frontFace get() = CardFace(size, theme, front)

internal val Card.backFace get() = CardFace(size, back.theme ?: theme, back.content)

internal val Card.fullName get() = group?.let { "$it - $name" } ?: name

internal class CardFace(
    val size: MmSize,
    val theme: CardTheme,
    val content: @Composable BoxScope.() -> Unit,
)

public val LocalCardSize: ProvidableCompositionLocal<MmSize> = compositionLocalOf<MmSize> { error("No card size provided") }
