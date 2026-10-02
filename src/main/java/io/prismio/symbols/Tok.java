package io.prismio.symbols;

import com.intellij.lexer.Lexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.prismio.lexer.PrismioLexer;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * One significant token, with the one fact about the whitespace before it that
 * Prismio's grammar depends on: whether a line ended there.
 *
 * <p>Prismio has no {@code ;}. A newline ends a statement, so a reader working on
 * tokens without it cannot tell {@code let a = b} followed by {@code c()} from
 * {@code let a = b c()}. Comments are kept, because a declaration's documentation
 * is the comment block directly above it.
 */
public record Tok(@NotNull IElementType type, @NotNull String text, int start, int end,
    boolean newlineBefore) {

  public boolean is(@NotNull String value) {
    return text.equals(value);
  }

  public boolean isComment() {
    return type == PrismioTypes.LINE_COMMENT || type == PrismioTypes.DOC_COMMENT
        || type == PrismioTypes.BLOCK_COMMENT;
  }

  /**
   * A token that can name something: an identifier, a type name, or a word the
   * lexer colours as a contextual keyword but which is a name in most positions
   * ({@code self}, {@code from}, {@code free}).
   */
  public boolean isName() {
    return type == PrismioTypes.IDENTIFIER || type == PrismioTypes.BUILTIN_TYPE
        || type == PrismioTypes.STDLIB_TYPE || type == PrismioTypes.CONTEXTUAL_KEYWORD;
  }

  /** Lexes {@code text} with the plugin's own lexer: the one that tracks the compiler's scanner. */
  public static @NotNull List<Tok> lex(@NotNull CharSequence text) {
    List<Tok> tokens = new ArrayList<>();
    Lexer lexer = new PrismioLexer();
    lexer.start(text);
    boolean newline = true;
    while (lexer.getTokenType() != null) {
      IElementType type = lexer.getTokenType();
      int start = lexer.getTokenStart();
      int end = lexer.getTokenEnd();
      if (type == TokenType.WHITE_SPACE) {
        for (int i = start; i < end; i++) {
          if (text.charAt(i) == '\n') {
            newline = true;
            break;
          }
        }
      } else {
        tokens.add(new Tok(type, text.subSequence(start, end).toString(), start, end, newline));
        // A line comment runs to the end of its line, so whatever follows it is on the next.
        newline = type == PrismioTypes.LINE_COMMENT || type == PrismioTypes.DOC_COMMENT;
      }
      lexer.advance();
    }
    return tokens;
  }

  /** The same list without comments: what the grammar sees. */
  public static @NotNull List<Tok> code(@NotNull List<Tok> tokens) {
    List<Tok> code = new ArrayList<>(tokens.size());
    boolean pendingNewline = false;
    for (Tok token : tokens) {
      if (token.isComment()) {
        pendingNewline |= token.newlineBefore();
        continue;
      }
      if (pendingNewline && !token.newlineBefore()) {
        token = new Tok(token.type(), token.text(), token.start(), token.end(), true);
      }
      pendingNewline = false;
      code.add(token);
    }
    return code;
  }
}
