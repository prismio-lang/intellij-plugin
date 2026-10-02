package io.prismio.handler;

import com.intellij.codeInsight.AutoPopupController;
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
  /**
   * Opens completion without Ctrl+Space where the next thing is a choice from a
   * list: a member after {@code .}, a module after {@code import }. The popup only
   * appears if completion has something to offer, so a {@code .} in a float or a
   * comment opens nothing.
   */
  @Override
  public @NotNull Result checkAutoPopup(
      char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (!(file instanceof PrismioFile)) {
      return Result.CONTINUE;
    }
    int offset = editor.getCaretModel().getOffset();
    if (c == '.' && !isEnclosedByLiteralOrComment(editor, Math.max(0, offset - 1))) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
      return Result.STOP;
    }
    if (c == ' ' && endsWithImportKeyword(editor.getDocument().getCharsSequence(), offset)) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
      return Result.STOP;
    }
    if ((c == '{' || c == ',') && inImportLine(editor.getDocument().getCharsSequence(), offset)) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
      return Result.STOP;
    }
    return Result.CONTINUE;
  }

  /** Whether the caret is on a line that starts with {@code import}: a group's names are a choice too. */
  private static boolean inImportLine(@NotNull CharSequence text, int offset) {
    int start = offset;
    while (start > 0 && text.charAt(start - 1) != '\n') {
      start--;
    }
    return text.subSequence(start, offset).toString().stripLeading().startsWith("import");
  }

  /** Whether the line so far is exactly {@code import}, the space not yet typed. */
  private static boolean endsWithImportKeyword(@NotNull CharSequence text, int offset) {
    int start = offset;
    while (start > 0 && text.charAt(start - 1) != '\n') {
      start--;
    }
    return text.subSequence(start, offset).toString().strip().equals("import");
  }

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
        && !isEnclosedByLiteralOrComment(editor, offset - 2)) {
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

  /**
   * Whether {@code offset} sits inside a literal or comment that began *before*
   * it.
   *
   * <p>The distinction matters because a `/*` is a block comment the moment it is
   * typed: the lexer runs an unterminated one to the end of the file, exactly as
   * the compiler does. Asking only "is this a comment token" would therefore say
   * yes to the comment the user is in the middle of opening, and auto-closing
   * would never fire. Requiring the token to have started earlier distinguishes
   * "I am opening a comment here" from "I am already inside one".
   */
  private static boolean isEnclosedByLiteralOrComment(@NotNull Editor editor, int offset) {
    if (editor.getDocument().getTextLength() == 0) {
      return false;
    }

    HighlighterIterator iterator = editor.getHighlighter().createIterator(offset);
    if (iterator.atEnd() || iterator.getStart() >= offset) {
      return false;
    }
    IElementType tokenType = iterator.getTokenType();
    return tokenType == PrismioTypes.STRING_LITERAL || tokenType == PrismioTypes.CHARACTER_LITERAL
        || tokenType == PrismioTypes.LINE_COMMENT
        || tokenType == PrismioTypes.DOC_COMMENT
        || tokenType == PrismioTypes.BLOCK_COMMENT;
  }
}
