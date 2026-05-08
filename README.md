# Card Composer

Card Composer is a library for creating and printing board game cards using Compose Desktop. It provides a specialized unit system for physical printing, a theme system tailored for cards, and powerful text formatting tools.

## Getting Started

Card Composer is a side project and is **not published to Maven Central**. You can use it in your project in one of two ways:

### Using GitHub Packages

To use Card Composer via GitHub Packages, add the repository and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/salomonbrys/card-composer")
        credentials {
            username = "YOUR_GITHUB_USERNAME"
            password = "YOUR_GITHUB_TOKEN" // A classic Personal Access Token with 'read:packages' scope
        }
    }
}

dependencies {
    implementation("fr.sb.card-composition:card-composer:{VERSION}")
}
```

### Building Locally

If you prefer not to use GitHub Packages, you can clone the repository and install it to your local Maven repository:

1. Clone the repository:
   ```bash
   git clone https://github.com/salomonbrys/Card-Composer.git
   ```
2. Navigate to the directory and run the publish task:
   ```bash
   cd Card-Composer
   ./gradlew card-composer:publishToMavenLocal
   ```
3. In your own project's `build.gradle.kts`, ensure you have `mavenLocal()` in your repositories and add the dependency:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("fr.sb.card-composition:card-composer:{VERSION}")
}
```

## Demo

A complete demo project is available in the [`Demo-Deck`](Demo-Deck) directory. It demonstrates how to create various types of cards and how to use the export system.

## Measure System

When designing cards for print, standard `dp` or `sp` units can be confusing. Card Composer uses a system where **1 dp = 1 sp = 1 pt** (PostScript point, 1/72 inch).

To make it easier to work with physical dimensions, several extension properties are provided in `fr.sb.card_composer.Units`:

- `.mm`: Converts millimeters to points.
- `.inch`: Converts inches to points.
- `.pt`: Explicitly uses points.

Example:
```kotlin
val cardWidth = 63.mm
val margin = 0.25.inch
```

## Creating and Organizing Cards

Cards are defined using the `Card` data class. Each card has a name, a size, a theme, a back, and an optional group.

```kotlin
val myCard = Card(
    size = CardSize.Poker.portrait,
    theme = myTheme,
    name = "Fireball",
    group = "Spells",
    back = Card.Back(
        id = "spell-back",
        content = { SpellBackContent() }
    ),
    front = { SpellFrontContent() }
)
```

### Organizing with Groups
The `group` property is used by exporters to organize files. For example, the PNG exporter will create a sub-directory for each group, keeping your exported card faces neatly organized.

For the PDF exporter, all cards of a given group must be of the same size and orientation.

## Theming: Use CardTheme instead of Material

While you *can* use Material Design components, they are often not suited for game card design. Card Composer provides a dedicated `CardTheme` that focuses on card backgrounds and base text styles.

Avoid using `MaterialTheme` as it brings in many defaults (like primary colors and typography) that might interfere with your card's look. Instead, use `CardTheme.current` to access your card's specific theme properties.

```kotlin
val customTheme = CardTheme(
    cardBackground = Color.White,
    textStyle = TextStyle(fontFamily = myFont, fontSize = 10.pt)
)

// In your card content:
Box(Modifier.background(CardTheme.current.cardBackground)) {
    Text("Hello") // This is fr.sb.card_composer.composable.Text, which uses CardTheme.current.textStyle as default.
}
```

Note that, even though Material components are not declared as a dependency in `build.gradle.kts`, it will be in your classpath because of `compose.desktop.currentOs`.
You should therefore declare the corresponding dependency as such:

```kotlin
jvmMain.dependencies {
   implementation(compose.desktop.currentOs) {
       exclude("org.jetbrains.compose.material")
   }
}
```

## Card Back ID and PNG Export

The `Card.Back` class has an `id` property. This ID is crucial when exporting to PNG:

1. **If `id` is null**: The exporter assumes this card has a unique back. It will export it as `CardName - Back.png`.
2. **If `id` is provided**: The exporter treats it as a shared back. It will export only one file named `Back - {id}.png` for all cards sharing that same ID. This is much more efficient for professional printing services where many cards share the same back design.

## FormattedText

The `FormattedText` utility allows you to use a simple tag-based syntax for rich text within your cards.

### Syntax
- **Styles**: `<b>Bold</b>`, `<i>Italics</i>`, `<s>Strikethrough</s>`.
- **Custom Styles**: `<myStyle>Custom</myStyle>` (requires configuration in `TextFormatter`).
- **Unicode**: `<#2665>` (Inserts ♥).
- **Inline Content**: `<!icon_id>` (Inserts a Composable placeholder).
- **Closing tags**: `<//>` closes the last opened tag, `</*>` closes all tags.

### Usage
```kotlin
val formatter = TextFormatter()
Text(formatter.formatted("This is <b>bold</b> and this is <#2665>."))
```

## Images

It is highly recommended to use `fr.sb.card_composer.composable.Image` rather than `androidx.compose.foundation.Image` for several reasons:

- **No `contentDescription`**: Since this library is focused on print, accessibility descriptions are not required.
- **High Quality**: `filterQuality` is set to `FilterQuality.High` by default, which is essential for high-quality print output.

### Image Sources

You can display images from two main sources:

- **Compose Resources**: Use standard Compose resources.
- **External Files**: Use `java.nio.file.Path` to load images from the file system.

### SVG Support

SVG files are supported when loaded as **external files** (via `Path`). However, they are **not supported** when used as Compose resources.

```kotlin
// Loading an image from a path
Image(Path("path/to/image.png"))

// Loading an SVG from a path
Image(Path("path/to/illustration.svg"))
```

## Safe Margins and Content Padding

When printing cards, you must account for "bleed" (extra area that will be cut) and "safe zones" (area where important content should stay).

- **Safe Margin**: `CardSize.standardSafeMargin` is set to **3.18mm** (approx. 1/8 inch). This is the standard bleed/margin used by many professional printers.
- **Full Size**: `CardSize.fullSize(size)` calculates the total dimensions including the safe margins on all sides.
- **Safe Padding**: `CardSize.safePadding` (which is `standardSafeMargin * 2`) is a recommended padding to keep text and icons away from the cut line.

It is highly recommended to wrap your card content in a `Box` with safe padding:

```kotlin
Card(
    front = {
        Box(Modifier.padding(CardSize.safePadding)) {
            // Your important content here
        }
    }
)
```

## Previewing and Exporting: `cardComposerApplication`

Card Composer provides a built-in Compose Desktop application to preview your cards and export them to PNG or PDF.

To use it, create a `main` function in your JVM target:

```kotlin
fun main() = cardComposerApplication(
    title = "My Awesome Game",
    cards = {
        listOf(
            myFirstCard,
            mySecondCard,
            // ...
        )
    }
)
```

This will open a window where you can:
- Browse through all your defined cards.
- Toggle between light and dark mode for the UI.
- Export all cards or specific groups to PNG or PDF using the export dialog.
