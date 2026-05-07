@file:Suppress("NOTHING_TO_INLINE")

package fr.sb.card_composer

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

// 1 inch = 25,4 mm
public const val MM_PER_INCH: Float = 25.4f

// 1 inch = 72 pt
public const val PT_PER_INCH: Float = 72f

public typealias Pt = Dp

public typealias PtSize = DpSize

public inline fun PtSize(width: Pt, height: Pt): PtSize = DpSize(width, height)

public typealias PtOffset = DpOffset

public inline fun PtOffset(x: Pt, y: Pt): PtOffset = DpOffset(x, y)

@Stable
public inline val Int.pt: Pt get() = toFloat().pt

@Stable
public inline val Double.pt: Pt get() = toFloat().pt

@Stable
public inline val Float.pt: Pt get() = Pt(this)

@Stable
public inline val Int.inch: Pt get() = toFloat().inch

@Stable
public inline val Double.inch: Pt get() = toFloat().inch

@Stable
public inline val Float.inch: Pt get() = Pt(this * PT_PER_INCH)

@Stable
public inline val Int.mm: Pt get() = toFloat().mm

@Stable
public inline val Double.mm: Pt get() = toFloat().mm

@Stable
public val Float.mm: Pt get() = Pt(this / MM_PER_INCH * PT_PER_INCH)
