<div align="center">

<img src="https://www.prismio.org/icons/prismio-banner.png" alt="Prismio Logo" width="140" />

# Prismio Language Support

**First-class JetBrains IDE support for the [Prismio](https://prismio.org) programming language.**

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange?logo=jetbrains&logoColor=white)](https://plugins.jetbrains.com/)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/Vibrant275/PrismioPlugin/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)
[![IntelliJ](https://img.shields.io/badge/IntelliJ%20IDEA-2024.2%2B-blueviolet?logo=intellij-idea)](https://www.jetbrains.com/idea/)
[![Build](https://img.shields.io/badge/build-Gradle%208-red?logo=gradle)](build.gradle.kts)

</div>

---

## Overview

This repository provides **full language support** for the [Prismio programming language](https://github.com/prismio-lang/prismio) inside all JetBrains IDEs — including IntelliJ IDEA, CLion, GoLand, PyCharm, WebStorm, and more.

Prismio (`.psm` files) is a compiled, statically-typed systems language. This plugin bridges the Prismio compiler ecosystem with the JetBrains Platform, delivering a developer experience on par with first-class supported languages.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🎨 **Syntax Highlighting** | Lexer-based token coloring + semantic annotations for functions, structs, enums, and parameters |
| 🧠 **Code Completion** | Keyword, built-in type, and snippet suggestions with ranked ordering |
| 🗂️ **Structure View** | Instant navigation panel showing all top-level declarations |
| 🔍 **Go to Symbol** | Jump to any symbol across the project with `Ctrl+Alt+Shift+N` |
| 📖 **Quick Documentation** | Hover docs for Prismio elements with `Ctrl+Q` |
| ✏️ **Code Formatting** | Auto-format on save/reformat with fully configurable code style settings |
| 🔧 **Live Templates** | 15+ ready-to-use snippets: `fn`, `struct`, `enum`, `impl`, `if`, `while`, `for`, `match`, `let`, and more |
| 🔗 **Brace & Quote Matching** | Auto-close `()`, `[]`, `{}`, `""`, `''` |
| ↩️ **Smart Indentation** | Brace-aware `Enter` to auto-indent inside blocks |
| 💬 **Comment Toggling** | Line (`//`) and block (`/* */`) comment support via `Ctrl+/` and `Ctrl+Shift+/` |
| ✅ **Find Usages & Rename** | Basic find-usages and inline rename refactoring |
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
| JDK | 21+ |
| Gradle | 8.x (wrapper included) |
| IntelliJ IDEA | 2024.2+ (Community or Ultimate) |

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
prismio-intellij/
├── src/
│   └── main/
│       ├── java/io/prismio/
│       │   ├── annotator/          # Semantic highlighting (functions, structs, enums)
│       │   ├── completion/         # Code completion contributor
│       │   ├── formatter/          # Code formatting rules
│       │   ├── handler/            # Typed & enter key handlers
│       │   ├── highlighter/        # Lexer-based syntax highlighting
│       │   ├── lexer/              # JFlex lexer generated output
│       │   ├── psi/                # PSI tree elements
│       │   ├── settings/           # Color settings page
│       │   ├── template/           # File templates & live templates
│       │   ├── utils/              # Shared utilities
│       │   ├── Prismio.bnf         # Grammar definition (GrammarKit BNF)
│       │   ├── Prismio.flex        # JFlex lexer specification
│       │   └── ...                 # Core language registrations
│       └── resources/
│           ├── META-INF/
│           │   ├── plugin.xml      # Plugin descriptor & extension points
│           │   └── pluginIcon.svg  # Plugin marketplace icon
│           ├── colorSchemes/       # Default & Darcula color attributes
│           ├── fileTemplates/      # .psm file template
│           ├── icons/              # File type icons
│           └── liveTemplates/      # Live template definitions
├── build.gradle.kts                # Gradle build configuration
└── gradle.properties               # Build cache & Kotlin stdlib settings
```

---

## 🧩 Compatibility

| IDE | Since Build | Until Build |
|---|---|---|
| IntelliJ IDEA Community | 2024.2 (`242`) | 2025.3.* (`253.*`) |
| IntelliJ IDEA Ultimate | 2024.2 | 2025.3.* |
| CLion | 2024.2 | 2025.3.* |
| All other JetBrains IDEs | 2024.2 | 2025.3.* |

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
