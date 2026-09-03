package io.prismio.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import io.prismio.icons.PrismioIcons;
import io.prismio.psi.PrismioFile;
import io.prismio.lang.PrismioWords;
import io.prismio.psi.PrismioTypes;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context-aware code completion for Prismio language.
 * Provides intelligent suggestions based on the current cursor position.
 */
public final class PrismioCompletionContributor extends CompletionContributor {
  // Keywords that start declarations (after these, we expect an identifier - no
  // suggestions)
  private static final Set<String> DECLARATION_KEYWORDS =
      Set.of("let", "mut", "fn", "struct", "enum", "trait", "impl", "extern", "import");

  // Control flow keywords (valid at statement positions)
  private static final Set<String> CONTROL_KEYWORDS =
      Set.of("if", "else", "while", "for", "loop", "match");

  // Statement keywords
  private static final Set<String> STATEMENT_KEYWORDS =
      Set.of("return", "break", "continue", "throw");

  // Visibility precedes a declaration, so after one the user is choosing a
  // declaration keyword rather than typing a name.
  private static final Set<String> VISIBILITY_KEYWORDS =
      Set.of("public", "private", "internal");

  public PrismioCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(@NotNull CompletionParameters parameters,
              @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiFile file = parameters.getOriginalFile();
            if (!(file instanceof PrismioFile)) {
              return;
            }

            PsiElement position = parameters.getPosition();
            CompletionContext ctx = analyzeContext(position);

            switch (ctx) {
              case AFTER_DECLARATION_KEYWORD:
                // After let, fn, struct, etc. - user is typing an
                // identifier
                // Don't suggest anything - let them type freely
                return;

              case TYPE_ANNOTATION:
                // After : or -> - suggest type keywords only
                addTypeCompletions(result);
                return;

              case STATEMENT_START:
              case BLOCK_CONTENT:
                // At the start of a statement - suggest all appropriate
                // keywords
                addDeclarationCompletions(result);
                addControlFlowCompletions(result);
                addStatementCompletions(result);
                addBuiltinFunctionCompletions(result);
                addTemplateCompletions(result);
                return;

              case EXPRESSION:
                // Inside an expression - suggest literals and built-ins
                addBooleanLiterals(result);
                addBuiltinFunctionCompletions(result);
                return;

              case TOP_LEVEL:
              default:
                // Top level or unknown - suggest declarations and
                // templates
                addDeclarationCompletions(result);
                addTemplateCompletions(result);
                return;
            }
          }
        });
  }

  /**
   * Completion context types
   */
  private enum CompletionContext {
    TOP_LEVEL, // At file top level
    AFTER_DECLARATION_KEYWORD, // After let, fn, struct, etc. - expecting identifier
    TYPE_ANNOTATION, // After : or -> - expecting type
    STATEMENT_START, // At the start of a new statement
    BLOCK_CONTENT, // Inside a block { }
    EXPRESSION // Inside an expression
  }

  /**
   * Analyzes the current position to determine the completion context
   */
  private CompletionContext analyzeContext(PsiElement position) {
    if (position == null) {
      return CompletionContext.TOP_LEVEL;
    }

    // Look at the previous non-whitespace sibling or token
    PsiElement prev = findPreviousSignificantElement(position);
    if (prev == null) {
      return CompletionContext.TOP_LEVEL;
    }

    String prevText = prev.getText();
    IElementType prevType = prev.getNode() != null ? prev.getNode().getElementType() : null;

    // Check if we're after a declaration keyword
    if (prevType == PrismioTypes.KEYWORD && DECLARATION_KEYWORDS.contains(prevText)) {
      return CompletionContext.AFTER_DECLARATION_KEYWORD;
    }

    // Check if we're after 'mut' (which follows 'let')
    if ("mut".equals(prevText)) {
      return CompletionContext.AFTER_DECLARATION_KEYWORD;
    }

    // Check if we're in a type annotation context (after : or ->)
    if (prevType == PrismioTypes.COLON || prevType == PrismioTypes.ARROW) {
      return CompletionContext.TYPE_ANNOTATION;
    }

    // Check if we're after an identifier that follows a declaration keyword
    // e.g., "let name" - we're after "name", check what's before it
    if (prevType == PrismioTypes.IDENTIFIER) {
      PsiElement beforeIdentifier = findPreviousSignificantElement(prev);
      if (beforeIdentifier != null) {
        String beforeText = beforeIdentifier.getText();
        IElementType beforeType =
            beforeIdentifier.getNode() != null ? beforeIdentifier.getNode().getElementType() : null;

        // If the identifier is right after a declaration keyword,
        // we might be continuing to type or at a type annotation position
        if (beforeType == PrismioTypes.KEYWORD && DECLARATION_KEYWORDS.contains(beforeText)) {
          // We're after "let name" - could be typing more or waiting for :
          // Check the current character being typed
          return CompletionContext.EXPRESSION;
        }
        if ("mut".equals(beforeText)) {
          return CompletionContext.EXPRESSION;
        }
      }
    }

    // Check if we're after an opening brace
    if (prevType == PrismioTypes.LBRACE) {
      return CompletionContext.BLOCK_CONTENT;
    }

    // Check if we're after a closing brace (possible else, or new statement)
    if (prevType == PrismioTypes.RBRACE) {
      return CompletionContext.STATEMENT_START;
    }

    // Check if we're after a comma (in parameter list or similar)
    if (prevType == PrismioTypes.COMMA) {
      // Could be in a parameter list - check parent context
      return CompletionContext.EXPRESSION;
    }

    // Check if we're after parentheses
    if (prevType == PrismioTypes.LPAREN) {
      return CompletionContext.EXPRESSION;
    }

    // Default to expression context
    return CompletionContext.EXPRESSION;
  }

  /**
   * Finds the previous significant (non-whitespace) element
   */
  @Nullable
  private PsiElement findPreviousSignificantElement(PsiElement element) {
    if (element == null) {
      return null;
    }

    // First try previous sibling
    PsiElement prev = element.getPrevSibling();
    while (prev != null) {
      // Skip whitespace
      if (prev.getNode() != null
          && prev.getNode().getElementType() != com.intellij.psi.TokenType.WHITE_SPACE
          && prev.getTextLength() > 0) {
        return prev;
      }
      prev = prev.getPrevSibling();
    }

    // If no previous sibling, try parent's previous sibling
    PsiElement parent = element.getParent();
    if (parent != null && !(parent instanceof PsiFile)) {
      return findPreviousSignificantElement(parent);
    }

    return null;
  }

  // ==================== Completion Adders ====================

  private void addDeclarationCompletions(CompletionResultSet result) {
    for (String visibility : new String[] {"public", "private", "internal"}) {
      result.addElement(LookupElementBuilder.create(visibility)
              .withTypeText("visibility")
              .withInsertHandler(new SpaceInsertHandler())
              .bold());
    }

    result.addElement(LookupElementBuilder.create("region")
            .withTypeText("arena scope")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("let")
            .withTypeText("variable")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("let mut")
            .withTypeText("mutable variable")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("fn")
            .withTypeText("function")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("struct")
            .withTypeText("structure")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("enum")
            .withTypeText("enumeration")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("trait")
            .withTypeText("trait")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("impl")
            .withTypeText("implementation")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("extern")
            .withTypeText("external declaration")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("import")
            .withTypeText("import")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());
  }

  private void addControlFlowCompletions(CompletionResultSet result) {
    // Prismio uses: if () { } syntax (with parentheses)
    result.addElement(LookupElementBuilder.create("if")
            .withTypeText("control flow")
            .withInsertHandler(new ParenthesisWithBraceInsertHandler())
            .bold());

    // else doesn't need parentheses
    result.addElement(LookupElementBuilder.create("else")
            .withTypeText("control flow")
            .withInsertHandler(new BraceInsertHandler())
            .bold());

    // Prismio uses: while () { } syntax (with parentheses)
    result.addElement(LookupElementBuilder.create("while")
            .withTypeText("loop")
            .withInsertHandler(new ParenthesisWithBraceInsertHandler())
            .bold());

    // for loop needs parentheses: for (x in items) { }
    result.addElement(LookupElementBuilder.create("for")
            .withTypeText("loop")
            .withInsertHandler(new ParenthesisWithBraceInsertHandler())
            .bold());

    // loop doesn't need parentheses - infinite loop
    result.addElement(LookupElementBuilder.create("loop")
            .withTypeText("infinite loop")
            .withInsertHandler(new BraceInsertHandler())
            .bold());

    // Prismio uses: match () { } syntax (with parentheses)
    result.addElement(LookupElementBuilder.create("match")
            .withTypeText("pattern matching")
            .withInsertHandler(new ParenthesisWithBraceInsertHandler())
            .bold());
  }

  private void addStatementCompletions(CompletionResultSet result) {
    result.addElement(LookupElementBuilder.create("return")
            .withTypeText("statement")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());

    result.addElement(LookupElementBuilder.create("break").withTypeText("statement").bold());

    result.addElement(LookupElementBuilder.create("continue").withTypeText("statement").bold());

    result.addElement(LookupElementBuilder.create("throw")
            .withTypeText("statement")
            .withInsertHandler(new SpaceInsertHandler())
            .bold());
  }

  private void addTypeCompletions(CompletionResultSet result) {
    // Both lists come from PrismioWords, so a type added to the language reaches
    // completion and highlighting together. Sorted so the popup is stable.
    PrismioWords.BUILTIN_TYPES.stream().sorted().forEach(name ->
        result.addElement(LookupElementBuilder.create(name)
            .withTypeText("built-in type")
            .withIcon(PrismioIcons.FILE)));

    // Offered with the bracket, because every one of these is generic and a bare
    // `List` does not type-check.
    PrismioWords.STDLIB_TYPES.stream().sorted().forEach(name ->
        result.addElement(LookupElementBuilder.create(name)
            .withTypeText("std type")
            .withTailText("<>", true)
            .withInsertHandler(new AngleBracketInsertHandler())
            .withIcon(PrismioIcons.FILE)));
  }

  private void addBooleanLiterals(CompletionResultSet result) {
    result.addElement(LookupElementBuilder.create("true").withTypeText("boolean").bold());

    result.addElement(LookupElementBuilder.create("false").withTypeText("boolean").bold());
  }

  private void addBuiltinFunctionCompletions(CompletionResultSet result) {
    result.addElement(LookupElementBuilder.create("println")
            .withTypeText("built-in function")
            .withInsertHandler(new ParenthesisInsertHandler())
            .withIcon(PrismioIcons.FILE));

    result.addElement(LookupElementBuilder.create("print")
            .withTypeText("built-in function")
            .withInsertHandler(new ParenthesisInsertHandler())
            .withIcon(PrismioIcons.FILE));
  }

  private void addTemplateCompletions(CompletionResultSet result) {
    // Main function template
    result.addElement(LookupElementBuilder.create("fn main()")
            .withPresentableText("main")
            .withTypeText("main function template")
            .withInsertHandler((insertContext, item) -> {
              String indent = getIndentAtOffset(insertContext);
              String innerIndent = indent + "    ";
              String template = " {\n" + innerIndent + "\n" + indent + "}";
              insertContext.getDocument().insertString(insertContext.getTailOffset(), template);
              insertContext.getEditor().getCaretModel().moveToOffset(
                  insertContext.getTailOffset() - indent.length() - 2);
            }));

    // Function with return type template
    result.addElement(LookupElementBuilder.create("fn name() -> Type")
            .withPresentableText("fn -> Type")
            .withTypeText("function with return type")
            .withInsertHandler((insertContext, item) -> {
              String indent = getIndentAtOffset(insertContext);
              String innerIndent = indent + "    ";
              String template = " {\n" + innerIndent + "\n" + indent + "}";
              insertContext.getDocument().insertString(insertContext.getTailOffset(), template);
              insertContext.getEditor().getCaretModel().moveToOffset(
                  insertContext.getTailOffset() - indent.length() - 2);
            }));
  }

  /**
   * Gets the indentation of the current line at the insertion offset.
   */
  private static String getIndentAtOffset(InsertionContext context) {
    int offset = context.getStartOffset();
    CharSequence text = context.getDocument().getCharsSequence();

    // Find the start of the current line
    int lineStart = offset;
    while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
      lineStart--;
    }

    // Extract leading whitespace
    StringBuilder indent = new StringBuilder();
    for (int i = lineStart; i < offset && i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == ' ' || c == '\t') {
        indent.append(c);
      } else {
        break;
      }
    }
    return indent.toString();
  }

  // ==================== Insert Handlers ====================

  /** Every std container is generic, so the caret lands between the brackets. */
  private static class AngleBracketInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      context.getDocument().insertString(context.getTailOffset(), "<>");
      context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() - 1);
    }
  }

  private static class SpaceInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      context.getDocument().insertString(context.getTailOffset(), " ");
      context.getEditor().getCaretModel().moveToOffset(context.getTailOffset());
    }
  }

  private static class ParenthesisInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      context.getDocument().insertString(context.getTailOffset(), " ()");
      context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() - 1);
    }
  }

  /**
   * Insert handler for control flow keywords that need both parentheses and
   * braces.
   * Example: for/while/if/match completion results in:
   * for () {
   * |
   * }
   */
  private static class ParenthesisWithBraceInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      String indent = getIndentAtOffset(context);
      String innerIndent = indent + "    ";

      // Insert: " () {\n \n}"
      String template = " () {\n" + innerIndent + "\n" + indent + "}";
      context.getDocument().insertString(context.getTailOffset(), template);

      // Position cursor inside the parentheses (after the space, before the closing
      // paren)
      // Template: " () {\n..." - cursor should be at position 2 (between the parens)
      int tailOffset = context.getTailOffset();
      int parenOffset =
          tailOffset - innerIndent.length() - indent.length() - 5; // Go back to find ()
      context.getEditor().getCaretModel().moveToOffset(parenOffset);
    }
  }

  private static class BraceInsertHandler implements InsertHandler<LookupElement> {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      // Calculate the current line's indentation using shared helper
      String indent = getIndentAtOffset(context);
      String innerIndent = indent + "    ";

      // Insert brace with proper indentation: { on same line, content indented, }
      // aligned with opening
      String template = " {\n" + innerIndent + "\n" + indent + "}";
      context.getDocument().insertString(context.getTailOffset(), template);
      // Position cursor inside the block (at the indented empty line)
      context.getEditor().getCaretModel().moveToOffset(
          context.getTailOffset() - indent.length() - 2);
    }
  }
}
