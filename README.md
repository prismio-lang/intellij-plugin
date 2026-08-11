<div align="center">

<img src="https://www.prismio.org/icons/prismio-banner.png" alt="Prismio Logo" width="140" />

# Prismio Language Support

**First-class JetBrains IDE support for the [Prismio](https://prismio.org) programming language.**

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange?logo=jetbrains&logoColor=white)](https://plugins.jetbrains.com/)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/Vibrant275/PrismioPlugin/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)
[![IntelliJ](https://img.shields.io/badge/IntelliJ%20IDEA-2026.2%2B-blueviolet?logo=intellij-idea)](https://www.jetbrains.com/idea/)
[![Build](https://img.shields.io/badge/build-Gradle%209.6.1-red?logo=gradle)](build.gradle.kts)

</div>

---

## Overview

This repository provides JetBrains IDE support for the [Prismio programming language](https://github.com/prismio-lang/prismio), including IntelliJ IDEA, CLion, GoLand, PyCharm, and WebStorm.

Prismio (`.psm` files) is a compiled, statically typed systems language. The plugin currently provides the editor, navigation, and presentation layer for `.psm` files. Compiler-backed parsing, diagnostics, resolution, and execution integration remain separate post-v1 milestones.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🎨 **Syntax Highlighting** | Theme-aware token and semantic colors for declarations, calls, types, imports, fields, variables, constants, and enum variants |
| 🧠 **Code Completion** | Keyword, built-in type, and snippet suggestions with ranked ordering |
| 🗂️ **Structure View** | Instant navigation panel showing declarations and methods |
| 📦 **Code Folding** | Collapse multiline function bodies from the editor gutter |
| 🔴 **Line Breakpoints** | Add persistent breakpoint markers on meaningful `.psm` lines |
| 🔍 **Go to Symbol** | Jump to any symbol across the project with `Ctrl+Alt+Shift+N` |
| 📖 **Documentation** | Declaration summaries with `Ctrl+Q` and rendered `/** */` comments with `Ctrl+Alt+Q` |
| ✏️ **Code Formatting** | Auto-format on save/reformat with fully configurable code style settings |
| 🔧 **Live Templates** | 15+ ready-to-use snippets: `fn`, `struct`, `enum`, `impl`, `if`, `while`, `for`, `match`, `let`, and more |
| 🔗 **Delimiter Matching** | Auto-close `()`, `[]`, `{}`, `""`, `''`, and block comments (`/* */`) |
| ↩️ **Smart Indentation** | Brace-aware blocks and consistent three-space block-comment continuation |
| 💬 **Comment Toggling** | Line (`//`) and block (`/* */`) comment support via `Ctrl+/` and `Ctrl+Shift+/` |
| ✅ **Find Usages** | Identifier-aware usage search and element descriptions |
| 🎨 **Color Settings Page** | Fully configurable token colors in **Settings → Editor → Color Scheme → Prismio** |
| 🔤 **Spellchecking** | Integrated spell-checker for strings and comments |
| 📄 **New File Action** | Create `.psm` files from the **New** menu with a Prismio icon |

---

## 🚀 Installation

### Via JetBrains Marketplace

1. Open your JetBrains IDE.
2. Go to **Settings / Preferences → Plugins → Marketplace**.
3. Search for **"Prismio Language Support"**.
4. Click **Install** and restart the IDE.
---

## 🛠️ Building from Source

### Prerequisites

| Requirement | Version |
|---|---|
| JDK | 26 |
| Gradle | 9.6.1 (wrapper included) |
| IntelliJ IDEA | 2026.2+ |

### Steps

```bash
# Clone this repository
git clone https://github.com/Vibrant275/PrismioPlugin.git
cd PrismioPlugin

# Build the plugin zip
./gradlew buildPlugin

# Run an IDE sandbox for testing
./gradlew runIde
```

The distributable plugin zip will be placed in `build/distributions/`.

---

## 🗂️ Project Structure

```
intellij-plugin/
├── src/
│   ├── main/
│   │   ├── gen/io/prismio/       # Generated lexer, parser, and token types
│   │   ├── java/io/prismio/
│   │   │   ├── annotator/          # Semantic highlighting (functions, structs, enums)
│   │   │   ├── completion/         # Code completion contributor
│   │   │   ├── debugger/           # Gutter line breakpoint support
│   │   │   ├── documentation/      # Quick documentation provider
│   │   │   ├── editor/             # Comments, braces, and quote handling
│   │   │   ├── folding/            # Function body folding regions
│   │   │   ├── formatter/          # Code formatting rules
│   │   │   ├── handler/            # Typed & enter key handlers
│   │   │   ├── highlighter/        # Lexer-based syntax highlighting
│   │   │   ├── icons/              # Shared plugin icon registry
│   │   │   ├── lexer/              # IntelliJ lexer adapter
│   │   │   ├── navigation/         # Structure view, symbols, and usages
│   │   │   ├── parser/             # Parser definition
│   │   │   ├── psi/                # File and token PSI support
│   │   │   ├── settings/           # Color and code-style settings
│   │   │   ├── spellcheck/         # Comment and string spellchecking
│   │   │   ├── template/           # New Prismio File action
│   │   │   ├── Prismio.bnf         # Grammar definition (GrammarKit BNF)
│   │   │   ├── Prismio.flex        # JFlex lexer specification
│   │   │   └── ...                 # Core language registrations
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   ├── plugin.xml      # Plugin descriptor & extension points
│   │       │   └── pluginIcon.svg  # Plugin marketplace icon
│   │       ├── colorSchemes/       # Light and dark semantic call colors
│   │       ├── fileTemplates/      # .psm file template
│   │       ├── icons/              # File type icons
│   │       └── liveTemplates/      # Live template definitions
│   └── test/                       # Platform fixture tests
├── build.gradle.kts                # Gradle build configuration
└── gradle.properties               # Build cache & Kotlin stdlib settings
```

---

## 🧩 Compatibility

| IDE | Since Build | Until Build |
|---|---|---|
| IntelliJ IDEA | 2026.2 (`262`) | Open-ended |
| CLion | 2026.2 | Open-ended |
| All other JetBrains IDEs with the spellchecker module | 2026.2 | Open-ended |

> The plugin depends only on `com.intellij.modules.platform`, making it universal across all JetBrains products.

---

## 🌐 Related Projects

| Project                                            | Description |
|----------------------------------------------------|---|
| [Prismio](https://github.com/prismio-lang/prismio) | The Prismio compiler |
| This repo                                          | JetBrains IDE plugin |

---

## 🤝 Contributing

Contributions are very welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

**Quick start:**

```bash
git checkout -b feature/my-awesome-feature
# make your changes
./gradlew check        # run tests & verifications
git commit -m "feat: add awesome feature"
git push origin feature/my-awesome-feature
# open a Pull Request
```

---

## 🔒 Security

Please do **not** file public GitHub issues for security vulnerabilities. Instead, refer to our [SECURITY.md](SECURITY.md) for responsible disclosure instructions.

---

## 📄 License

See [LICENSE](LICENSE) for full details.

---

<div align="center">
Made with ❤️ for the Prismio ecosystem
</div>
