# Contributing to Prismio Language Support

Thank you for your interest in contributing! This plugin is the official JetBrains IDE integration for the [Prismio programming language](https://github.com/prismio-lang/prismio), and every contribution — from a bug report to a new feature — makes a meaningful difference.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Submitting Pull Requests](#submitting-pull-requests)
- [Development Guide](#development-guide)
  - [Project Layout](#project-layout)
  - [Key Technologies](#key-technologies)
  - [Working with the Grammar](#working-with-the-grammar)
  - [Working with the Lexer](#working-with-the-lexer)
  - [Running & Testing](#running--testing)
- [Coding Standards](#coding-standards)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Release Process](#release-process)
- [Security Vulnerabilities](#security-vulnerabilities)

---

## Code of Conduct

By participating in this project you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). We are committed to providing a welcoming and respectful environment for everyone.

---

## Getting Started

### Prerequisites

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| JDK | 26 | `JAVA_HOME` must be set |
| IntelliJ IDEA | 2026.2.1 | For the Grammar-Kit & JFlex plugins |
| Gradle | 9.6.1 | Wrapper (`./gradlew`) is included — no separate install needed |
| Git | Any recent version | |

**Recommended IntelliJ Plugins (for development):**

- [Grammar-Kit](https://plugins.jetbrains.com/plugin/6606-grammar-kit) — BNF grammar editing and PSI generation
- [JFlex Support](https://plugins.jetbrains.com/plugin/263-jflex-support) — JFlex lexer editing

### Development Setup

```bash
# 1. Fork this repository on GitHub, then clone your fork
git clone https://github.com/<your-username>/prismio-intellij.git
cd prismio-intellij

# 2. Add the upstream remote
git remote add upstream https://github.com/Vibrant275/PrismioPlugin.git

# 3. Build once to verify everything compiles
./gradlew buildPlugin

# 4. Launch a sandboxed IDE instance with the plugin pre-installed
./gradlew runIde
```

The `runIde` task downloads a matching version of IntelliJ IDEA Community and boots it with the plugin loaded — no manual IDE configuration needed.

---

## How to Contribute

### Reporting Bugs

Before filing a new bug report:

1. Check the [existing issues](https://github.com/Vibrant275/PrismioPlugin/issues) to avoid duplicates.
2. Try to reproduce the bug on the **latest plugin version**.

When opening an issue, please include:

- Plugin version (from **Settings → Plugins**)
- IDE name and version (e.g., IntelliJ IDEA 2026.2.1)
- Operating system and architecture
- A **minimal** `.psm` file or code snippet that triggers the bug
- The exact error message or unexpected behavior you observe
- Steps to reproduce the issue

> [!TIP]
> Attach the IDE log from **Help → Show Log in Explorer / Finder** if you see an exception.

### Suggesting Features

We welcome feature suggestions! Open an issue with the label **`enhancement`** and describe:

- What problem the feature solves
- Your proposed solution or design
- Any alternatives you have considered

For large changes (new subsystems, significant refactors), please **open a discussion issue first** before writing code to align on the approach.

### Submitting Pull Requests

1. **Fork** the repository and create a descriptive branch:
   ```bash
   git checkout -b feat/structure-view-icons
   # or
   git checkout -b fix/completion-crash-on-empty-file
   ```

2. **Make your changes.** Keep each PR focused on a single concern.

3. **Add or update tests** in `src/test/` where applicable.

4. **Run the full verification suite** and ensure it passes:
   ```bash
   ./gradlew check verifyPlugin
   ```

5. **Commit** following our [commit message guidelines](#commit-message-guidelines).

6. **Push** your branch and open a Pull Request against `main`.

7. Fill in the PR template, linking to the relevant issue (`Closes #123`).

8. A maintainer will review your PR. Please be responsive to feedback — PRs with no activity for 14 days may be closed.

> [!IMPORTANT]
> PRs that change the BNF grammar (`Prismio.bnf`) **must** also regenerate the PSI classes and parser using Grammar-Kit, and commit the generated output under `src/main/gen/`.

---

## Development Guide

### Project Layout

```
src/
└── main/
    ├── gen/io/prismio/       # Generated lexer, parser, and token types
    ├── java/io/prismio/
    │   ├── annotator/          # Semantic annotations (PrismioAnnotator.java)
    │   ├── completion/         # CompletionContributor
    │   ├── debugger/           # Gutter line-breakpoint support
    │   ├── documentation/      # Quick documentation provider
    │   ├── editor/             # Commenter, brace matcher, quote handler
    │   ├── folding/            # Function-body folding
    │   ├── formatter/          # FormattingModelBuilder helpers
    │   ├── handler/            # TypedHandler, EnterHandler
    │   ├── highlighter/        # SyntaxHighlighter, SyntaxHighlighterFactory
    │   ├── icons/              # Shared plugin icon registry
    │   ├── lexer/              # IntelliJ lexer adapter
    │   ├── navigation/         # Structure view, symbols, and usages
    │   ├── parser/             # ParserDefinition
    │   ├── psi/                # File and token PSI support
    │   ├── settings/           # Color and code-style settings
    │   ├── spellcheck/         # Comment and string spellchecking
    │   ├── template/           # New Prismio File action
    │   ├── Prismio.bnf         # ← Grammar-Kit BNF grammar
    │   └── Prismio.flex        # ← JFlex lexer spec
    └── resources/
        ├── META-INF/plugin.xml # ← Extension point registrations
        ├── colorSchemes/       # Default token colors
        ├── fileTemplates/      # New file template
        ├── icons/              # .psm file icon
        └── liveTemplates/      # Live template XML
```

### Key Technologies

| Technology | Role |
|---|---|
| [JetBrains IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/) | Plugin APIs |
| [Grammar-Kit (BNF)](https://github.com/JetBrains/Grammar-Kit) | PSI parser & tree generation from `Prismio.bnf` |
| [JFlex](https://jflex.de/) | Lexer generation from `Prismio.flex` |
| [IntelliJ Platform Gradle Plugin v2](https://github.com/JetBrains/intellij-platform-gradle-plugin) | Build, packaging, verification |

### Working with the Grammar

The grammar is defined in [`Prismio.bnf`](src/main/java/io/prismio/Prismio.bnf) using the Grammar-Kit BNF format.

To regenerate the PSI parser and element classes after editing the grammar:

1. Open `Prismio.bnf` in IntelliJ IDEA.
2. Right-click inside the file → **Generate Parser Code**.
3. The generated output lands in `src/main/gen/`. **Commit these files.**

### Working with the Lexer

The lexer is defined in [`Prismio.flex`](src/main/java/io/prismio/Prismio.flex) (JFlex format).

To regenerate the lexer after editing:

1. Open `Prismio.flex` in IntelliJ IDEA.
2. Right-click → **Run JFlex Generator**.
3. The generated `PrismioLexer.java` lands in `src/main/gen/`. **Commit it.**

> [!WARNING]
> Never hand-edit generated files under `src/main/gen/`. They will be overwritten the next time the generator runs.

### Running & Testing

```bash
# Run all unit tests
./gradlew test

# Run plugin verifier (checks binary compatibility with the target IDE)
./gradlew verifyPlugin

# Build the distributable zip
./gradlew buildPlugin
# Output: build/distributions/prismio-intellij-<version>.zip

# Boot a sandboxed IDE with the plugin loaded
./gradlew runIde
```

---

## Coding Standards

- **Java 25** source compatibility, compiled with JDK 26 (no preview features).
- Follow the [IntelliJ Platform Coding Guidelines](https://plugins.jetbrains.com/docs/intellij/intellij-coding-guidelines.html).
- Keep classes focused and single-responsibility.
- All public methods and classes must have Javadoc comments.
- Do not suppress warnings with `@SuppressWarnings` without a comment explaining why.
- Avoid platform-deprecated APIs; check for yellow warnings in the IDE.
- Generated code (under `src/main/gen/`) is exempt from style rules.

---

## Commit Message Guidelines

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary>

[optional body]

[optional footer: Closes #issue]
```

**Types:**

| Type | When to use |
|------|------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code refactor, no behavior change |
| `test` | Adding or fixing tests |
| `chore` | Build scripts, dependencies, tooling |
| `perf` | Performance improvement |

**Examples:**

```
feat(completion): add keyword completion for 'match' expressions

fix(highlighter): prevent NPE when lexer encounters empty token stream
Closes #42

docs: update CONTRIBUTING with BNF regeneration steps
```

---

## Release Process

> This section is for maintainers.

1. Bump `version` in `build.gradle.kts` → `pluginConfiguration { version = "x.y.z" }`.
2. Update `<change-notes>` in `plugin.xml`.
3. Run `./gradlew buildPlugin verifyPlugin`.
4. Tag the release: `git tag v<x.y.z> && git push origin v<x.y.z>`.
5. Upload `build/distributions/*.zip` to the GitHub Release and JetBrains Marketplace.

---

## Security Vulnerabilities

Please **do not** open public issues for security problems. Follow the instructions in [SECURITY.md](SECURITY.md) to report vulnerabilities privately.

---

Thank you for helping make Prismio development a better experience for everyone! 🎉
