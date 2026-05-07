package fr.sb.card_composer.composable

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.nio.ByteBuffer
import java.util.LinkedList
import kotlin.text.get


private val ftToken = Regex("<(?<type>[/!#@])?(?<commands>[a-zA-Z0-9|,/!#@:*+-]+)>")

/**
 * A utility class for formatting annotated text. The `TextFormatter` uses token-based parsing
 * to apply styles, custom builds, and inline content to a text. Styles can be applied using
 * predefined span styles or custom definitions. Additionally, Unicode symbols and inline content
 * can also be rendered within the formatted text.
 *
 * This class supports a builder pattern to define styles and custom rendering logic
 * by associating unique identifiers with specific span styles or rendering functions.
 *
 * ## Token Types
 *
 * The formatter supports the following token types within angle brackets `<>`:
 *
 * - **Style Start**: `<id>` or `<id:argument>` - Starts applying a style with the given id and optional argument
 * - **Style End**: `</id>` - Ends the style with the given id
 * - **End Any**: `</>` - Ends the most recently started style
 * - **End All**: `</*>` - Ends all currently active styles */
 * - **Inline Content**: `<!id>` - Inserts inline content with the given id
 * - **Unicode**: `<#hexcode>` - Inserts a Unicode character using UTF-32 hexadecimal code
 * - **Custom Build**: `<@id>` or `<@id:argument>` - Executes a custom build function with the given id and optional argument
 * - **Multiple Commands**: `<cmd1,cmd2,cmd3>` - Multiple commands can be combined in a single token separated by commas
 *
 * ## Predefined Styles
 * 
 * - `b` - Bold text
 * - `i` - Italic text
 * - `s` - Strikethrough text
 *
 * @constructor Initializes the `TextFormatter` with an optional builder block to configure
 * custom styles and builds.
 *
 * @param build A configuration block for initializing styles and custom builds.
 */
public class TextFormatter(build: Builder.() -> Unit = {}) {

    public typealias SpanStyleProvider = (String?) -> SpanStyle

    public typealias CustomBuild = AnnotatedString.Builder.(String?) -> Unit

    public class Builder internal constructor(
        private val styles: MutableMap<String, SpanStyleProvider>,
        private val customBuilds: MutableMap<String, CustomBuild>,
    ) {
        public fun style(id: String, styleProvider: SpanStyleProvider) {
            styles[id] = styleProvider
        }
        public fun customBuild(id: String, build: CustomBuild) {
            customBuilds[id] = build
        }
    }

    private val styles: Map<String, SpanStyleProvider>
    private val customBuilds: Map<String, CustomBuild>

    init {
        val styles = HashMap<String, SpanStyleProvider>()
        val customBuilds = HashMap<String, CustomBuild>()
        val builder = Builder(styles, customBuilds)
        builder.style("b") { SpanStyle(fontWeight = FontWeight.Bold) }
        builder.style("i") { SpanStyle(fontStyle = FontStyle.Italic) }
        builder.style("s") { SpanStyle(textDecoration = TextDecoration.LineThrough) }
        builder.build()
        this.styles = styles
        this.customBuilds = customBuilds
    }

    private fun warning(message: String) {
        IllegalStateException(message).printStackTrace()
    }

    private sealed interface Token {
        data class Start(val id: String, val argument: String?) : Token
        data class End(val id: String) : Token
        data class InlineContent(val id: String) : Token
        data class Text(val text: String) : Token
        data class Custom(val id: String, val argument: String?) : Token
        data class Unicode(val code: Int) : Token
    }

    @Suppress("ReplaceRangeStartEndInclusiveWithFirstLast")
    private fun tokenize(text: String) : List<Token> = buildList {
        var position = 0
        ftToken.findAll(text).forEach { match ->
            if (match.range.start != position) {
                add(Token.Text(text.substring(position, match.range.start)))
            }
            val commands = match.groups["commands"]!!.value.split(",")
            commands.filter { it.isNotEmpty() }.forEach { command ->
                val split = command.split(':', limit = 2)
                val id = split[0]
                val argument = split.getOrNull(1)
                when (match.groups["type"]?.value) {
                    null -> add(Token.Start(id, argument))
                    "/" -> {
                        if (argument != null) warning("Argument '$argument' is ignored on end tag '$id'")
                        add(Token.End(id))
                    }
                    "!" -> {
                        if (argument != null) warning("Argument '$argument' is ignored on inline content tag '$id'")
                        add(Token.InlineContent(id))
                    }
                    "#" -> {
                        if (argument != null) warning("Argument '$argument' is ignored on unicode tag '$id'")
                        val code = id.toIntOrNull(16)
                        if (code == null) warning("Invalid unicode code '$id'")
                        else add(Token.Unicode(code))
                    }
                    "@" -> add(Token.Custom(id, argument))
                    else -> warning("Unknown token type '${match.groups["type"]!!.value}'")
                }
            }
            position = match.range.endInclusive + 1
        }
        if (position < text.length) {
            add(Token.Text(text.substring(position)))
        }
    }

    public fun append(builder: AnnotatedString.Builder, text: String) {
        val tokens = tokenize(text)
        val started = LinkedList<Pair<Int, Token.Start>>()

        var position = 0

        fun appendText(text: String) {
            builder.append(text)
            position += text.length
        }

        fun addStyle(start: Int, id: String, argument: String?) {
            val styleProvider = styles[id]
            if (styleProvider == null) warning("Unknown style id '${id}'")
            else {
                builder.addStyle(styleProvider(argument), start, position)
            }
        }

        tokens.forEach { token ->
            when (token) {
                is Token.Text -> {
                    appendText(token.text)
                }
                is Token.Unicode -> {
                    val buffer = ByteBuffer.allocate(4)
                    buffer.putInt(0, token.code)
                    val text = Charsets.UTF_32.decode(buffer).toString()
                    appendText(text)
                }
                is Token.InlineContent -> {
                    builder.appendInlineContent(token.id)
                    position += 1
                }
                is Token.Custom -> {
                    val build = customBuilds[token.id]
                    if (build == null) warning("Unknown custom build '${token.id}'")
                    else {
                        val previousLength = builder.length
                        build(builder, token.argument)
                        val added = builder.length - previousLength
                        position += added
                    }
                }
                is Token.Start -> {
                    if (token.id == "*" || token.id == "/") warning("Style id cannot be '*' or '/'")
                    else {
                        started.addFirst(position to token)
                    }
                }
                is Token.End -> {
                    when (token.id) {
                        "/" -> {
                            if (started.isEmpty()) warning("Unexpected end of any style")
                            else {
                                val (startPosition, startToken) = started.removeFirst()
                                addStyle(startPosition, startToken.id, startToken.argument)
                            }
                        }
                        "*" -> {
                            started.forEach { (startPosition, startToken) ->
                                addStyle(startPosition, startToken.id, startToken.argument)
                            }
                            started.clear()
                        }
                        else -> {
                            val styleIndex = started.indexOfFirst { (_, startToken) -> startToken.id == token.id }
                            if (styleIndex == -1) warning("Unexpected end of style '${token.id}'")
                            else {
                                val (startPosition, startToken) = started.removeAt(styleIndex)
                                addStyle(startPosition, startToken.id, startToken.argument)
                            }
                        }
                    }
                }
            }
        }

        started.forEach { (startPosition, startToken) ->
            addStyle(startPosition, startToken.id, startToken.argument)
        }
    }
}

public fun AnnotatedString.Builder.append(formatter: TextFormatter, text: String): Unit = formatter.append(this, text)

public fun TextFormatter.formatted(text: String): AnnotatedString = buildAnnotatedString { append(this, text) }
