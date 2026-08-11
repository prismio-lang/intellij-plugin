package io.prismio.handler;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Typed handler for Prismio.
 * Handles paired delimiters and auto-spacing before braces.
 */
public class PrismioTypedHandler extends TypedHandlerDelegate {
  @Override
  public @NotNull Result charTyped(
      char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (!(file instanceof PrismioFile)) {
      return Result.CONTINUE;
    }

    int offset = editor.getCaretModel().getOffset();
    Document document = editor.getDocument();
    CharSequence text = document.getCharsSequence();

    // Complete a block-comment pair and leave the caret inside it. Do not do this
    // inside strings or existing comments, where /* is ordinary text.
    if (c == '*' && offset >= 2 && text.charAt(offset - 2) == '/' && !startsWith(text, offset, "*/")
        && !isInsideLiteralOrComment(editor, offset - 2)) {
      EditorModificationUtil.insertStringAtCaret(editor, "*/", false, 0);
      return Result.STOP;
    }

    // Auto-insert space before opening brace if not already present
    // The brace was just typed, so it's at offset - 1
    if (c == '{' && offset >= 2) {
      char charBeforeBrace = text.charAt(offset - 2);
      // If previous char is not a space, (, or newline, insert a space before the
      // brace
      if (charBeforeBrace != ' ' && charBeforeBrace != '(' && charBeforeBrace != '\n'
          && charBeforeBrace != '\t') {
        WriteCommandAction.runWriteCommandAction(
            project, () -> { document.insertString(offset - 1, " "); });
        // Move caret forward by 1 to account for inserted space
        editor.getCaretModel().moveToOffset(offset + 1);
        return Result.STOP;
      }
    }

    // Auto-close quotes if not escaped and not already inside a string
    if (c == '"' || c == '\'') {
      // Check if this quote was just typed (it's at offset - 1)
      if (offset > 0) {
        // Check if we should auto-close (not escaped)
        boolean isEscaped = offset >= 2 && text.charAt(offset - 2) == '\\';

        if (!isEscaped) {
          // Check if next char is already the same quote (user is at end of string)
          if (offset < text.length() && text.charAt(offset) == c) {
            // Skip - don't insert another quote
            return Result.CONTINUE;
          }

          // Count quotes to determine if we're opening or closing
          int quoteCount = 0;
          for (int i = 0; i < offset - 1; i++) {
            if (text.charAt(i) == c && (i == 0 || text.charAt(i - 1) != '\\')) {
              quoteCount++;
            }
          }

          // If even number of quotes before, we're starting a new string - insert closing
          // quote
          if (quoteCount % 2 == 0) {
            EditorModificationUtil.insertStringAtCaret(editor, String.valueOf(c), false, 0);
            return Result.STOP;
          }
        }
      }
    }

    return Result.CONTINUE;
  }

  private static boolean startsWith(@NotNull CharSequence text, int offset, @NotNull String value) {
    if (offset < 0 || offset + value.length() > text.length()) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      if (text.charAt(offset + i) != value.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isInsideLiteralOrComment(@NotNull Editor editor, int offset) {
    if (editor.getDocument().getTextLength() == 0) {
      return false;
    }

    HighlighterIterator iterator = editor.getHighlighter().createIterator(offset);
    IElementType tokenType = iterator.getTokenType();
    return tokenType == PrismioTypes.STRING_LITERAL || tokenType == PrismioTypes.CHARACTER_LITERAL
        || tokenType == PrismioTypes.SINGLE_LINE_COMMENT
        || tokenType == PrismioTypes.MULTILINE_COMMENT;
  }
}
