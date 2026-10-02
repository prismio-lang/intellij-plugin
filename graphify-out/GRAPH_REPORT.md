# Graph Report - intellij-plugin  (2026-10-02)

## Corpus Check
- 155 files · ~73,888 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 14 file(s) not represented in the graph (top: .xml 5, .properties 3, (none) 2)

## Summary
- 1878 nodes · 5369 edges · 101 communities (36 shown, 65 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 213 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cebb1374`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- PrismioCodeStyleSettingsProvider.java
- DeclarationScanner.java
- CompilerSelector
- TypeInference
- com.intellij.openapi.util.TextRange
- com.intellij.testFramework.fixtures.BasePlatformTestCase
- com.intellij.lang.ASTNode
- UmsLexer
- PrismioSettingsConfigurable
- UmsStructureViewFactory.java
- com.intellij.psi.codeStyle.CodeStyleSettings
- UmsContext
- .checkAutoPopup
- com.intellij.psi.PsiElement
- .collectCached
- PrismioScopeResolver
- UmsParserDefinition.java
- PrismioExternalAnnotator
- PrismioToolchainService
- Kind
- PrismioColorSettingsPage
- PrismioSmartCompletionTest
- PrismioRunConfiguration
- UmsRunConfigurations
- SignatureParser
- list
- com.intellij.psi.PsiFile
- PrismioSpellcheckingStrategy.java
- PrismioLexer
- PrismioNewProjectWizard.java
- UmsBraceMatcher.java
- PrismioAdvancedFeaturesTest.java
- PrismioDocRenderDefault.java
- javax.swing.Icon
- PrismioExternalAnnotator.java
- org.jetbrains.annotations.NotNull
- PrismioProjectSettings
- UmsManifest
- PrismioParameterInfoHandler.java
- Lookups.java
- CreateUmsManifestAction.java
- UmsFileType
- ImportPaths
- PrismioDirectoryProjectGenerator.java
- arraylist
- UmsFile.java
- Prismio Language Support
- PrismioAdvancedFeaturesTest
- PrismioRunConfiguration.java
- CompilerIntrinsics
- .getGotoDeclarationTargets
- PrismioImports
- PrismioCodeInsightTest.java
- PrismioBlock.java
- UmsRunConfigurationsTest
- TypeRef
- impl String
- PrismioCompletionContributor.java
- .instanceMembers
- PrismioGotoDeclarationTest
- CreatePrismioFileAction.java
- UmsQuoteHandler
- PrismioRunProfileState.java
- UmsColorSettingsPage
- PrismioSyntaxHighlighter.java
- PrismioDocumentationProvider
- com.intellij.lang.Language
- PrismioFindUsagesProvider
- Kind
- Display
- Option
- Plugin Marketplace Icon
- com.intellij.openapi.project.Project
- .collect
- process.psm
- math.psm
- com.intellij.psi.tree.IElementType
- DeclaredHostTest
- Declaration
- .postProcessEnter
- org.jetbrains.annotations.Nullable
- PrismioSettingsConfigurable.java
- PrismioCommenter
- .receiverBefore
- PrismioProjectStepTest
- com.intellij.lang.PsiParser
- com.intellij.openapi.editor.highlighter.HighlighterIterator
- ModuleSummary

## God Nodes (most connected - your core abstractions)
1. `PrismioLexer` - 67 edges
2. `TypeRef` - 47 edges
3. `TypeInference` - 41 edges
4. `SignatureParser` - 37 edges
5. `PrismioRunConfiguration` - 36 edges
6. `PrismioSmartCompletionTest` - 36 edges
7. `FnSig` - 31 edges
8. `UmsContext` - 30 edges
9. `PrismioTypes` - 27 edges
10. `PrismioAnnotator` - 26 edges

## Surprising Connections (you probably didn't know these)
- `Miniature Standard Library Test Mock` --conceptually_related_to--> `Code Completion & Live Templates`  [INFERRED]
  src/test/testData/stdlib/README.md → README.md
- `Shown` --references--> `FnSig`  [EXTRACTED]
  src/main/java/io/prismio/completion/PrismioParameterInfoHandler.java → src/main/java/io/prismio/symbols/FnSig.java
- `PrismioRunConfiguration` --references--> `PrismioRunProfileState`  [EXTRACTED]
  src/main/java/io/prismio/execution/PrismioRunConfiguration.java → src/main/java/io/prismio/execution/PrismioRunProfileState.java
- `PrismioRunConfigurationEditor` --references--> `PrismioRunConfiguration`  [EXTRACTED]
  src/main/java/io/prismio/execution/PrismioRunConfigurationEditor.java → src/main/java/io/prismio/execution/PrismioRunConfiguration.java
- `Located` --references--> `UmsManifest`  [EXTRACTED]
  src/main/java/io/prismio/execution/UmsRunConfigurations.java → src/main/java/io/prismio/ums/UmsManifest.java

## Import Cycles
- None detected.

## Communities (101 total, 65 thin omitted)

### Community 0 - "PrismioCodeStyleSettingsProvider.java"
Cohesion: 0.17
Nodes (3): PrismioCodeStyleSettings, PrismioCodeStylePanel, PrismioCodeStyleSettingsProvider

### Community 2 - "CompilerSelector"
Cohesion: 0.14
Nodes (6): CompilerSelector, Status, ERROR, NEUTRAL, OK, PrismioProjectStep

### Community 4 - "TypeInference"
Cohesion: 0.17
Nodes (4): Import, Scope, TypeInference, Value

### Community 5 - "com.intellij.openapi.util.TextRange"
Cohesion: 0.06
Nodes (7): PrismioInlineDocumentation, PrismioInlineDocumentationProvider, PrismioBlockCommentPostFormatProcessor, PrismioAstFactory, PrismioIdentifier, PrismioIdentifierManipulator, PrismioReferenceTest

### Community 6 - "com.intellij.testFramework.fixtures.BasePlatformTestCase"
Cohesion: 0.07
Nodes (4): PrismioFormatterTest, PrismioParsingTest, PrismioRenameTest, UmsValueChecksTest

### Community 10 - "UmsStructureViewFactory.java"
Cohesion: 0.06
Nodes (7): PrismioStructureViewElement, PrismioStructureViewFactory, PrismioStructureViewModel, Entry, Model, Node, UmsStructureViewFactory

### Community 12 - "UmsContext"
Cohesion: 0.07
Nodes (13): UmsAnnotator, Role, ARGS, BLOCK, CALL, KEY, VALUE, Token (+5 more)

### Community 16 - "PrismioScopeResolver"
Cohesion: 0.25
Nodes (3): LocalDecl, PrismioScopeResolver, ScopeInfo

### Community 18 - "PrismioExternalAnnotator"
Cohesion: 0.07
Nodes (9): PrismioCheckTarget, PrismioDiagnostic, PrismioDiagnosticParser, AnnotationResult, Checked, InitialInfo, PrismioExternalAnnotator, PrismioCheckTargetTest (+1 more)

### Community 20 - "Kind"
Cohesion: 0.50
Nodes (4): Kind, ENUM, STRUCT, TRAIT

### Community 23 - "PrismioRunConfiguration"
Cohesion: 0.16
Nodes (3): PrismioRunConfiguration, PrismioRunConfigurationProducer, UmsRunConfigurationProducer

### Community 24 - "UmsRunConfigurations"
Cohesion: 0.23
Nodes (3): Located, Spec, UmsRunConfigurations

### Community 25 - "SignatureParser"
Cohesion: 0.10
Nodes (12): CompletionPosition, Kind, EXPRESSION, IMPORT, IMPORT_FROM, MEMBER, NONE, STATEMENT (+4 more)

### Community 27 - "com.intellij.psi.PsiFile"
Cohesion: 0.18
Nodes (3): Keywords, PrismioCompletionContributor, MemberTargets

### Community 28 - "PrismioSpellcheckingStrategy.java"
Cohesion: 0.21
Nodes (4): CommentTokenizer, PrismioSpellcheckingStrategy, StringTokenizer, UmsSpellcheckingStrategy

### Community 29 - "PrismioLexer"
Cohesion: 0.05
Nodes (4): PrismioLineBreakpointType, PrismioLexer, PrismioCodeInsightTest, PrismioLexerTest

### Community 32 - "PrismioAdvancedFeaturesTest.java"
Cohesion: 0.15
Nodes (3): PrismioConsoleFilter, PrismioRunLineMarkerContributor, UmsTypes

### Community 33 - "PrismioDocRenderDefault.java"
Cohesion: 0.17
Nodes (3): PrismioDocRenderDefault, Startup, PrismioDocRenderDefaultTest

### Community 34 - "javax.swing.Icon"
Cohesion: 0.22
Nodes (3): PrismioRunConfigurationType, PrismioIcons, Prismio File Icon

### Community 36 - "org.jetbrains.annotations.NotNull"
Cohesion: 0.13
Nodes (5): Lookups, FnSig, Param, Member, TypeDecl

### Community 38 - "UmsManifest"
Cohesion: 0.20
Nodes (5): UmsRunLineMarkerContributor, Command, Target, Tok, UmsManifest

### Community 39 - "PrismioParameterInfoHandler.java"
Cohesion: 0.25
Nodes (3): PrismioParameterInfoHandler, Shown, Call

### Community 40 - "Lookups.java"
Cohesion: 0.14
Nodes (5): CallInsertHandler, ArgumentsInsertHandler, AssignmentInsertHandler, BlockInsertHandler, UmsCompletionContributor

### Community 41 - "CreateUmsManifestAction.java"
Cohesion: 0.09
Nodes (3): PrismioFormattingModelBuilder, CreateUmsManifestAction, UmsFormattingModelBuilder

### Community 44 - "PrismioDirectoryProjectGenerator.java"
Cohesion: 0.11
Nodes (3): Peer, PrismioDirectoryProjectGenerator, Settings

### Community 47 - "Prismio Language Support"
Cohesion: 0.18
Nodes (12): Contributing Guide, Development Setup & Gradle Guide, JFlex Lexer Guidelines, Code Completion & Live Templates, Formatting & Indentation, Code Navigation & Structure View, Prismio Language Support, Syntax Highlighting & Color Settings (+4 more)

### Community 50 - "CompilerIntrinsics"
Cohesion: 0.20
Nodes (3): CompilerIntrinsics, Entry, CompilerIntrinsicsTest

### Community 54 - "PrismioBlock.java"
Cohesion: 0.12
Nodes (4): PrismioLanguage, PrismioTokenSets, PrismioTokenType, UmsTokenType

### Community 57 - "impl String"
Cohesion: 0.14
Nodes (3): impl String, impl StringBuilder, StringBuilder

### Community 59 - ".instanceMembers"
Cohesion: 0.19
Nodes (5): Origin, BUILT_IN, EXTENSION, OWN, TRAIT

### Community 75 - "Kind"
Cohesion: 0.29
Nodes (7): Kind, CONSTANT, FIELD, METHOD, PROPERTY, STATIC_METHOD, VARIANT

### Community 76 - "Display"
Cohesion: 0.38
Nodes (3): impl Display for Int, impl Display for String, Display

### Community 77 - "Option"
Cohesion: 0.33
Nodes (4): Option, None, Some, impl Option

### Community 82 - "process.psm"
Cohesion: 0.40
Nodes (3): impl ProcessQuery, ProcessQuery, process

### Community 87 - "Declaration"
Cohesion: 0.11
Nodes (12): Declaration, DeclarationKind, CONSTANT, ENUM, ENUM_VARIANT, FIELD, FUNCTION, IMPLEMENTATION (+4 more)

### Community 102 - "ModuleSummary"
Cohesion: 0.10
Nodes (8): Global, ModuleSummary, ProjectSymbols, Interface, Snapshot, StdlibIndex, SymbolTable, StdlibCorpusTest

## Knowledge Gaps
- **52 isolated node(s):** `NONE`, `TOP_LEVEL`, `STATEMENT`, `EXPRESSION`, `TYPE` (+47 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 294 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `PrismioLexer` connect `PrismioLexer` to `DeclarationScanner.java`, `PrismioSyntaxHighlighter.java`, `PrismioExternalAnnotator.java`, `com.intellij.openapi.util.TextRange`, `PrismioFindUsagesProvider`, `.collect`, `PrismioScopeResolver`, `UmsParserDefinition.java`, `PrismioCodeInsightTest.java`, `com.intellij.psi.tree.IElementType`, `PrismioBlock.java`, `SignatureParser`, `.postProcessEnter`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Why does `PrismioSmartCompletionTest` connect `PrismioSmartCompletionTest` to `list`, `org.jetbrains.annotations.NotNull`, `com.intellij.testFramework.fixtures.BasePlatformTestCase`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `PrismioAdvancedFeaturesTest` connect `PrismioAdvancedFeaturesTest` to `PrismioAdvancedFeaturesTest.java`, `org.jetbrains.annotations.Nullable`, `com.intellij.testFramework.fixtures.BasePlatformTestCase`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **What connects `NONE`, `TOP_LEVEL`, `STATEMENT` to the rest of the system?**
  _52 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CompilerSelector` be split into smaller, more focused modules?**
  _Cohesion score 0.13846153846153847 - nodes in this community are weakly interconnected._
- **Should `CompilerSelector.java` be split into smaller, more focused modules?**
  _Cohesion score 0.13333333333333333 - nodes in this community are weakly interconnected._
- **Should `com.intellij.openapi.util.TextRange` be split into smaller, more focused modules?**
  _Cohesion score 0.05989110707803993 - nodes in this community are weakly interconnected._