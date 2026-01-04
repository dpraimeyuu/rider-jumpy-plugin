# Jumpy Plugin for Rider

[![Build](https://github.com/dpraimeyuu/rider-jumpy-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/dpraimeyuu/rider-jumpy-plugin/actions/workflows/build.yml)
[![Code Quality](https://github.com/dpraimeyuu/rider-jumpy-plugin/actions/workflows/code-quality.yml/badge.svg)](https://github.com/dpraimeyuu/rider-jumpy-plugin/actions/workflows/code-quality.yml)
[![Release](https://github.com/dpraimeyuu/rider-jumpy-plugin/actions/workflows/release.yml/badge.svg)](https://github.com/dpraimeyuu/rider-jumpy-plugin/actions/workflows/release.yml)

A quick cursor navigation plugin for JetBrains Rider, inspired by VSCode's Jumpy extension. Navigate to any visible word using two-letter combinations.

## Demo

https://github.com/user-attachments/assets/jumpy-demo.webm

**How it works:**
1. Press `Shift+Enter` to activate jumpy mode
2. Two-letter labels appear at the start of each word (highlighted in yellow)
3. Type a combination like `aq`, `bb`, or `cs` to instantly jump to that location
4. Labels turn orange as you type to show matches
5. Your cursor jumps instantly - no characters are typed into your file

## Features

- **Fast Navigation**: Jump to any visible word with just two keystrokes
- **Visual Labels**: Clear, color-coded labels at word boundaries
- **Minimal Interference**: Labels only appear when activated
- **Works Everywhere**: Compatible with all file types in Rider

## Usage

1. **Activate Jumpy Mode**: Press `Shift+Enter` (configurable)
2. **See Labels**: Two-letter combinations (aa, ab, ac, etc.) appear at the start of each word
3. **Jump**: Type the two-letter combination to jump to that position
4. **Cancel**: Press `ESC` to exit jumpy mode without jumping

### Example

```
Your code:
function calculateTotal(items) {
    return items.reduce((sum, item) => sum + item.price, 0);
}

With jumpy mode active:
aafunction abcalculateTotal(aditems) {
    aereturn afitems.agreduce((ahsum, aiitem) => ajsum + akitem.alprice, am0);
}

Type "ab" to jump to "calculateTotal"
Type "ai" to jump to the second "item"
```

## Building the Plugin

### Prerequisites

- JDK 17 or higher
- Internet connection (for Gradle to download dependencies)

### Build Steps

1. **Build the plugin**:
   ```bash
   ./gradlew buildPlugin
   ```

2. **The plugin ZIP will be created at**:
   ```
   build/distributions/rider-jumpy-plugin-1.0.0.zip
   ```

### Other Useful Gradle Tasks

- `./gradlew runIde` - Run Rider with the plugin installed (for development)
- `./gradlew test` - Run tests
- `./gradlew clean` - Clean build artifacts

## Installation

### From ZIP File

1. Build the plugin (see above) or download a release ZIP
2. Open Rider
3. Go to **Settings** → **Plugins**
4. Click the gear icon ⚙️ → **Install Plugin from Disk...**
5. Select the `rider-jumpy-plugin-1.0.0.zip` file
6. Restart Rider

### For Development

```bash
./gradlew runIde
```

This will launch a Rider instance with the plugin pre-installed.

## Configuration

### Changing the Keyboard Shortcut

1. Go to **Settings** → **Keymap**
2. Search for "Jumpy" or "Activate Jumpy Mode"
3. Right-click → **Add Keyboard Shortcut**
4. Set your preferred shortcut

## How It Works

The plugin consists of several components:

- **JumpyAction**: Entry point triggered by keyboard shortcut
- **JumpyModeHandler**: Manages state and coordinates components
- **WordPositionFinder**: Locates word boundaries in visible text
- **LabelGenerator**: Creates two-letter label combinations
- **JumpyOverlayPanel**: Renders labels over the editor

When activated:
1. Plugin scans visible viewport for word start positions
2. Generates labels (aa, ab, ac... zz) for up to 676 positions
3. Displays labels as overlay on the editor
4. Captures keyboard input
5. Jumps to the selected position or cancels on ESC/invalid input

## Compatibility

- **Platform**: JetBrains Rider 2023.3+
- **Language**: Kotlin
- **SDK**: IntelliJ Platform SDK

## Development

### Project Structure

```
rider-jumpy-plugin/
├── src/main/kotlin/com/github/dprai/jumpyplugin/
│   ├── JumpyAction.kt              # Action triggered by shortcut
│   ├── JumpyModeHandler.kt         # State management
│   ├── LabelGenerator.kt           # Label generation logic
│   ├── WordPositionFinder.kt       # Word detection logic
│   └── ui/
│       └── JumpyOverlayPanel.kt    # UI rendering
├── src/main/resources/META-INF/
│   └── plugin.xml                  # Plugin descriptor
└── build.gradle.kts                # Build configuration
```

### Future Enhancements

- [ ] CamelCase support (already implemented, needs toggle)
- [ ] Configurable label colors
- [ ] Configurable label scheme (single letter first, then double)
- [ ] Jump to line starts, line ends, or specific characters
- [ ] Custom character sequences

## License

This project is open source. Feel free to modify and distribute.

## Credits

Inspired by [Jumpy](https://marketplace.visualstudio.com/items?itemName=wmaurer.vscode-jumpy) for Visual Studio Code.
