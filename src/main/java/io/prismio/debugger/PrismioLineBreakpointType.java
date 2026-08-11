package io.prismio.debugger;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import io.prismio.PrismioFileType;
import io.prismio.lexer.PrismioLexerAdapter;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Adds persistent Prismio line breakpoints to the editor gutter. */
public final class PrismioLineBreakpointType extends XLineBreakpointType<XBreakpointProperties<?>> {
  public PrismioLineBreakpointType() {
    super("prismio-line", "Prismio Line Breakpoints");
  }

  @Override
  public @Nullable XBreakpointProperties<?> createBreakpointProperties(
      @NotNull VirtualFile file, int line) {
    return null;
  }

  @Override
  public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
    if (file.getFileType() != PrismioFileType.INSTANCE || line < 0) {
      return false;
    }

    Document document = FileDocumentManager.getInstance().getDocument(file);
    if (document == null || line >= document.getLineCount()) {
      return false;
    }

    int startOffset = document.getLineStartOffset(line);
    int endOffset = document.getLineEndOffset(line);
    if (startOffset >= endOffset) {
      return false;
    }

    Lexer lexer = new PrismioLexerAdapter();
    lexer.start(document.getCharsSequence(), startOffset, endOffset, 0);
    while (lexer.getTokenType() != null) {
      IElementType tokenType = lexer.getTokenType();
      if (tokenType != TokenType.WHITE_SPACE && tokenType != PrismioTypes.SINGLE_LINE_COMMENT
          && tokenType != PrismioTypes.MULTILINE_COMMENT && tokenType != PrismioTypes.LBRACE
          && tokenType != PrismioTypes.RBRACE) {
        return true;
      }
      lexer.advance();
    }
    return false;
  }
}
