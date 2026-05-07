@file:Suppress("NOTHING_TO_INLINE")

package fr.sb.card_composer

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


public typealias Mm = Dp

public typealias MmSize = DpSize

@Stable
public fun MmSize(width: Dp, height: Dp): MmSize = DpSize(width, height)

@Stable
public inline val Int.mm: Mm get() = this.dp

@Stable
public inline val Double.mm: Mm get() = this.dp

@Stable
public inline val Float.mm: Mm get() = this.dp


public typealias Pt = TextUnit

@Stable
public inline val Int.pt: Pt get() = this.sp

@Stable
public inline val Double.pt: Pt get() = this.sp

@Stable
public inline val Float.pt: Pt get() = this.sp

// 1 inch = 25,4 mm
public const val MM_PER_INCH: Float = 25.4f

public const val PT_PER_INCH: Float = 72f

// 1 mm = 2.83465 pt
public const val PT_PER_MM: Float = PT_PER_INCH / MM_PER_INCH

// 1 pt = 0,352778 mm
public const val MM_PER_PT: Float = MM_PER_INCH / PT_PER_INCH
