package io.prismio.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.prismio.lang.PrismioWords;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Prismio lexer, written by hand to follow {@code src/lexer/scanner.psm}.
 *
 * <p>The decision order below is that file's {@code lexerNextToken}, including
 * the one part of it that is easy to get wrong: {@code '.'} is a separator but
 * {@code ".."} is a range, so the range check sits between the operator check
 * and the separator check. Scanning {@code 0..n} the other way produces the
 * float {@code 0.} followed by {@code .n}.
 *
 * <p>An unterminated string or character literal ends at the newline rather than
 * running to the end of the file. The compiler reports it as a fatal error and
 * stops; an editor has to keep colouring the rest of the file, and a literal
 * that swallows everything after it makes a missing quote look like a fault
 * hundreds of lines further down.
 */
public final class PrismioLexer extends LexerBase {

  private CharSequence buffer = "";
  private int bufferEnd;
  private int tokenStart;
  private int tokenEnd;
  private @Nullable IElementType tokenType;

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    this.buffer = buffer;
    this.bufferEnd = endOffset;
    this.tokenStart = startOffset;
    this.tokenEnd = startOffset;
    advance();
  }

  @Override
  public int getState() {
    // Every token is decided from its own first characters, so there is no state
    // to carry between them. Comments are single-line and strings cannot span a
    // newline, which is what makes that true.
    return 0;
  }

  @Override
  public @Nullable IElementType getTokenType() {
    return tokenType;
  }

  @Override
  public int getTokenStart() {
    return tokenStart;
  }

  @Override
  public int getTokenEnd() {
    return tokenEnd;
  }

  @Override
  public @NotNull CharSequence getBufferSequence() {
    return buffer;
  }

  @Override
  public int getBufferEnd() {
    return bufferEnd;
  }

  @Override
  public void advance() {
    tokenStart = tokenEnd;
    if (tokenStart >= bufferEnd) {
      tokenType = null;
      return;
    }
    tokenType = scan();
  }

  private IElementType scan() {
    char c = buffer.charAt(tokenStart);

    if (isWhitespace(c)) {
      tokenEnd = tokenStart;
      while (tokenEnd < bufferEnd && isWhitespace(buffer.charAt(tokenEnd))) {
        tokenEnd++;
      }
      return TokenType.WHITE_SPACE;
    }

    if (c == '/' && peekIs(1, '/')) {
      return lineComment();
    }
    if (c == '/' && peekIs(1, '*')) {
      return blockComment();
    }
    if (isIdentifierStart(c)) {
      return word();
    }
    if (isDigit(c)) {
      return number();
    }
    if (c == '"') {
      return quoted('"', PrismioTypes.STRING_LITERAL);
    }
    if (c == '\'') {
      return quoted('\'', PrismioTypes.CHARACTER_LITERAL);
    }
    if (isOperatorChar(c)) {
      return operator();
    }
    // Between the operator and separator checks: see the class comment.
    if (c == '.' && peekIs(1, '.')) {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.RANGE;
    }
    IElementType separator = separator(c);
    if (separator != null) {
      tokenEnd = tokenStart + 1;
      return separator;
    }

    tokenEnd = tokenStart + 1;
    return TokenType.BAD_CHARACTER;
  }

  private IElementType lineComment() {
    // `///` is a documentation convention, not compiler syntax: the scanner sees
    // an ordinary `//` line. Told apart here so rendered docs and spellchecking
    // can treat it as prose. `////` and longer are separators in comment art,
    // and are deliberately not documentation.
    boolean documentation = peekIs(2, '/') && !peekIs(3, '/');
    tokenEnd = tokenStart + 2;
    while (tokenEnd < bufferEnd && buffer.charAt(tokenEnd) != '\n' && buffer.charAt(tokenEnd) != '\r') {
      tokenEnd++;
    }
    return documentation ? PrismioTypes.DOC_COMMENT : PrismioTypes.LINE_COMMENT;
  }

  /**
   * A block comment, counting depth exactly as {@code lexerSkipBlockComment} in
   * the compiler does.
   *
   * <p>Depth counts delimiters and nothing else. A {@code //} inside the comment
   * is not a line comment, so it neither hides a close nor protects a stray
   * open — matching the compiler is the point, because an editor that disagreed
   * would colour a region the compiler is reading as code.
   *
   * <p>An unterminated comment runs to the end of the file. The compiler makes
   * that a fatal error; here it has to produce a token, and colouring the
   * remainder as comment is what shows the reader where the unclosed delimiter
   * swallowed their program.
   */
  private IElementType blockComment() {
    // `/**` opens a documentation comment, the same convention `///` follows and
    // the one PrismioInlineDocumentationProvider renders. `/**/` and `/***/` are
    // excluded: both are empty comments that merely happen to start with those
    // three characters, and there is nothing in either to render.
    // `/**` is positions 0..2, so what decides it is position 3: the first
    // character of the comment's body.
    boolean documentation = peekIs(2, '*') && !peekIs(3, '/') && !peekIs(3, '*');

    tokenEnd = tokenStart + 2;
    int depth = 1;
    while (tokenEnd < bufferEnd && depth > 0) {
      char c = buffer.charAt(tokenEnd);
      char next = tokenEnd + 1 < bufferEnd ? buffer.charAt(tokenEnd + 1) : '\0';
      if (c == '/' && next == '*') {
        depth++;
        tokenEnd += 2;
      } else if (c == '*' && next == '/') {
        depth--;
        tokenEnd += 2;
      } else {
        tokenEnd++;
      }
    }
    return documentation ? PrismioTypes.DOC_COMMENT : PrismioTypes.BLOCK_COMMENT;
  }

  private IElementType word() {
    tokenEnd = tokenStart;
    while (tokenEnd < bufferEnd && isIdentifierPart(buffer.charAt(tokenEnd))) {
      tokenEnd++;
    }
    String text = buffer.subSequence(tokenStart, tokenEnd).toString();

    if (text.equals("true") || text.equals("false")) {
      return PrismioTypes.BOOLEAN;
    }
    if (PrismioWords.KEYWORDS.contains(text)) {
      return PrismioTypes.KEYWORD;
    }
    if (PrismioWords.BUILTIN_TYPES.contains(text)) {
      return PrismioTypes.BUILTIN_TYPE;
    }
    if (PrismioWords.STDLIB_TYPES.contains(text)) {
      return PrismioTypes.STDLIB_TYPE;
    }
    if (PrismioWords.CONTEXTUAL_KEYWORDS.contains(text)) {
      return PrismioTypes.CONTEXTUAL_KEYWORD;
    }
    return PrismioTypes.IDENTIFIER;
  }

  private IElementType number() {
    tokenEnd = tokenStart;
    while (tokenEnd < bufferEnd && isDigit(buffer.charAt(tokenEnd))) {
      tokenEnd++;
    }
    // A '.' continues the number only when a digit follows, which is what keeps
    // `0..n` a range rather than a float.
    if (tokenEnd + 1 < bufferEnd && buffer.charAt(tokenEnd) == '.' && isDigit(buffer.charAt(tokenEnd + 1))) {
      tokenEnd++;
      while (tokenEnd < bufferEnd && isDigit(buffer.charAt(tokenEnd))) {
        tokenEnd++;
      }
      return PrismioTypes.FLOAT;
    }
    return PrismioTypes.INTEGER;
  }

  private IElementType quoted(char delimiter, IElementType type) {
    tokenEnd = tokenStart + 1;
    while (tokenEnd < bufferEnd) {
      char c = buffer.charAt(tokenEnd);
      if (c == '\n' || c == '\r') {
        // Unterminated. Stop at the line end so the damage stays on one line.
        return type;
      }
      if (c == '\\' && tokenEnd + 1 < bufferEnd) {
        tokenEnd += 2;
        continue;
      }
      tokenEnd++;
      if (c == delimiter) {
        return type;
      }
    }
    return type;
  }

  private IElementType operator() {
    char first = buffer.charAt(tokenStart);
    char second = tokenStart + 1 < bufferEnd ? buffer.charAt(tokenStart + 1) : '\0';

    if (first == '-' && second == '>') {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.ARROW;
    }
    if (first == '=' && second == '>') {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.FAT_ARROW;
    }
    if ((first == '=' || first == '!' || first == '<' || first == '>') && second == '=') {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.RELATIONAL_OP;
    }
    if ((first == '&' && second == '&') || (first == '|' && second == '|')) {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.LOGICAL_OP;
    }
    // `>>` also closes two nested generic arguments; the compiler's parser splits
    // it back apart when it expects a single '>'. One token here either way.
    if ((first == '<' && second == '<') || (first == '>' && second == '>')) {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.SHIFT_OP;
    }
    if (second == '=' && isCompoundAssignable(first)) {
      tokenEnd = tokenStart + 2;
      return PrismioTypes.ASSIGNMENT_OP;
    }

    tokenEnd = tokenStart + 1;
    if (first == '<' || first == '>') {
      return PrismioTypes.RELATIONAL_OP;
    }
    if (first == '=') {
      return PrismioTypes.ASSIGNMENT_OP;
    }
    if (first == '!') {
      return PrismioTypes.NEGATION;
    }
    if (first == '&' || first == '|' || first == '^' || first == '~') {
      return PrismioTypes.BITWISE_OP;
    }
    return PrismioTypes.ARITHMETIC_OP;
  }

  private static @Nullable IElementType separator(char c) {
    return switch (c) {
      case '(' -> PrismioTypes.LPAREN;
      case ')' -> PrismioTypes.RPAREN;
      case '{' -> PrismioTypes.LBRACE;
      case '}' -> PrismioTypes.RBRACE;
      case '[' -> PrismioTypes.LBRACKET;
      case ']' -> PrismioTypes.RBRACKET;
      case ',' -> PrismioTypes.COMMA;
      case ':' -> PrismioTypes.COLON;
      case '.' -> PrismioTypes.DOT;
      case '?' -> PrismioTypes.OPTIONAL;
      default -> null;
    };
  }

  private boolean peekIs(int offset, char expected) {
    int at = tokenStart + offset;
    return at < bufferEnd && buffer.charAt(at) == expected;
  }

  private static boolean isCompoundAssignable(char c) {
    // The set `isTwoCharOperator` accepts before an `=`. `~=` is deliberately
    // absent: there is no compound form of bitwise NOT.
    return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
        || c == '&' || c == '|' || c == '^';
  }

  /** `isOperator` in `src/common/text.psm`, character for character. */
  private static boolean isOperatorChar(char c) {
    return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
        || c == '=' || c == '!' || c == '<' || c == '>'
        || c == '&' || c == '|' || c == '^' || c == '~';
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isIdentifierStart(char c) {
    return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }
}
