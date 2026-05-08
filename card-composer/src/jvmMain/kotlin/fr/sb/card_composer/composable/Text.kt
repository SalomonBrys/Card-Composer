@file:Suppress("DuplicatedCode")

package fr.sb.card_composer.composable

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import fr.sb.card_composer.CardTheme


@Composable
public fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontSynthesis: FontSynthesis? = null,
    fontFamily: FontFamily? = null,
    fontFeatureSettings: String? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    baselineShift: BaselineShift? = null,
    textGeometricTransform: TextGeometricTransform? = null,
    localeList: LocaleList? = null,
    background: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    shadow: Shadow? = null,
    drawStyle: DrawStyle? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    textDirection: TextDirection = TextDirection.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textIndent: TextIndent? = null,
    lineHeightStyle: LineHeightStyle? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    hyphens: Hyphens = Hyphens.Unspecified,
    platformStyle: PlatformTextStyle? = null,
    textMotion: TextMotion? = null,
    style: TextStyle = CardTheme.textStyle,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    autoSize: TextAutoSize? = null,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
            baselineShift = baselineShift,
            textGeometricTransform = textGeometricTransform,
            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
            drawStyle = drawStyle,
            textAlign = textAlign,
            textDirection = textDirection,
            lineHeight = lineHeight,
            textIndent = textIndent,
            lineHeightStyle = lineHeightStyle,
            lineBreak = lineBreak,
            hyphens = hyphens,
            platformStyle = platformStyle,
            textMotion = textMotion,
        ),
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        autoSize = autoSize,
    )
}

@Composable
public fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontSynthesis: FontSynthesis? = null,
    fontFamily: FontFamily? = null,
    fontFeatureSettings: String? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    baselineShift: BaselineShift? = null,
    textGeometricTransform: TextGeometricTransform? = null,
    localeList: LocaleList? = null,
    background: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    shadow: Shadow? = null,
    drawStyle: DrawStyle? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    textDirection: TextDirection = TextDirection.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textIndent: TextIndent? = null,
    lineHeightStyle: LineHeightStyle? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    hyphens: Hyphens = Hyphens.Unspecified,
    platformStyle: PlatformTextStyle? = null,
    textMotion: TextMotion? = null,
    style: TextStyle = CardTheme.textStyle,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    autoSize: TextAutoSize? = null,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
            baselineShift = baselineShift,
            textGeometricTransform = textGeometricTransform,
            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
            drawStyle = drawStyle,
            textAlign = textAlign,
            textDirection = textDirection,
            lineHeight = lineHeight,
            textIndent = textIndent,
            lineHeightStyle = lineHeightStyle,
            lineBreak = lineBreak,
            hyphens = hyphens,
            platformStyle = platformStyle,
            textMotion = textMotion,
        ),
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        autoSize = autoSize,
    )
}

@Composable
public fun ProvideTextStyle(
    style: TextStyle,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        CardTheme provides CardTheme.current.merge(
            textStyle = style
        ),
        content = content
    )
}

@Composable
public fun ProvideTextStyle(
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontSynthesis: FontSynthesis? = null,
    fontFamily: FontFamily? = null,
    fontFeatureSettings: String? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    baselineShift: BaselineShift? = null,
    textGeometricTransform: TextGeometricTransform? = null,
    localeList: LocaleList? = null,
    background: Color = Color.Unspecified,
    textDecoration: TextDecoration? = null,
    shadow: Shadow? = null,
    drawStyle: DrawStyle? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    textDirection: TextDirection = TextDirection.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textIndent: TextIndent? = null,
    lineHeightStyle: LineHeightStyle? = null,
    lineBreak: LineBreak = LineBreak.Unspecified,
    hyphens: Hyphens = Hyphens.Unspecified,
    platformStyle: PlatformTextStyle? = null,
    textMotion: TextMotion? = null,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        CardTheme provides CardTheme.current.copy(
            textStyle = CardTheme.current.textStyle.merge(
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                fontSynthesis = fontSynthesis,
                fontFamily = fontFamily,
                fontFeatureSettings = fontFeatureSettings,
                letterSpacing = letterSpacing,
                baselineShift = baselineShift,
                textGeometricTransform = textGeometricTransform,
                localeList = localeList,
                background = background,
                textDecoration = textDecoration,
                shadow = shadow,
                drawStyle = drawStyle,
                textAlign = textAlign,
                textDirection = textDirection,
                lineHeight = lineHeight,
                textIndent = textIndent,
                lineHeightStyle = lineHeightStyle,
                lineBreak = lineBreak,
                hyphens = hyphens,
                platformStyle = platformStyle,
                textMotion = textMotion,
            )
        )
    ) {
        content()
    }
}
