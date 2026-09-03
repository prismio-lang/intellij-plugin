package io.prismio;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.prismio.lexer.PrismioLexer;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * The lexer against the language it claims to follow.
 *
 * <p>Every case here is one the previous JFlex lexer got wrong: it did not know
 * `and`, `or`, `where` or the contextual keywords, it had no range token, and it
 * accepted `_` digit separators the compiler's scanner has never had.
 */
public class PrismioLexerTest extends TestCase {

  private static List<IElementType> types(String source) {
    PrismioLexer lexer = new PrismioLexer();
    lexer.start(source, 0, source.length(), 0);
    List<IElementType> out = new ArrayList<>();
    while (lexer.getTokenType() != null) {
      if (lexer.getTokenType() != TokenType.WHITE_SPACE) {
        out.add(lexer.getTokenType());
      }
      lexer.advance();
    }
    return out;
  }

  private static List<String> texts(String source) {
    PrismioLexer lexer = new PrismioLexer();
    lexer.start(source, 0, source.length(), 0);
    List<String> out = new ArrayList<>();
    while (lexer.getTokenType() != null) {
      if (lexer.getTokenType() != TokenType.WHITE_SPACE) {
        out.add(source.substring(lexer.getTokenStart(), lexer.getTokenEnd()));
      }
      lexer.advance();
    }
    return out;
  }

  /** The whole file is covered: offsets must be contiguous from 0 to the end. */
  private static void assertCoversInput(String source) {
    PrismioLexer lexer = new PrismioLexer();
    lexer.start(source, 0, source.length(), 0);
    int expected = 0;
    while (lexer.getTokenType() != null) {
      assertEquals("token starts where the previous ended", expected, lexer.getTokenStart());
      assertTrue("token must consume input", lexer.getTokenEnd() > lexer.getTokenStart());
      expected = lexer.getTokenEnd();
      lexer.advance();
    }
    assertEquals("lexer reached the end of the input", source.length(), expected);
  }

  public void testLogicalOperatorsAreWords() {
    // The language spells these `and`/`or`; the old lexer only knew `&&`/`||`
    // and coloured both of these as ordinary identifiers.
    assertEquals(List.of(PrismioTypes.IDENTIFIER, PrismioTypes.KEYWORD, PrismioTypes.IDENTIFIER),
        types("a and b"));
    assertEquals(List.of(PrismioTypes.IDENTIFIER, PrismioTypes.KEYWORD, PrismioTypes.IDENTIFIER),
        types("a or b"));
  }

  public void testKeywordsTheOldLexerNeverHad() {
    for (String keyword : List.of("where", "as", "inout", "sink", "region", "none", "throw")) {
      assertEquals(keyword + " is a keyword", List.of(PrismioTypes.KEYWORD), types(keyword));
    }
  }

  public void testContextualKeywordsAreTheirOwnToken() {
    for (String word : List.of("public", "private", "internal", "dyn", "spawn", "pin", "unique",
        "produce", "borrow", "alias", "Self", "type")) {
      assertEquals(word + " is contextual", List.of(PrismioTypes.CONTEXTUAL_KEYWORD), types(word));
    }
  }

  public void testTypesAreSplitByOrigin() {
    assertEquals(List.of(PrismioTypes.BUILTIN_TYPE), types("Usize"));
    assertEquals(List.of(PrismioTypes.BUILTIN_TYPE), types("String"));
    assertEquals(List.of(PrismioTypes.STDLIB_TYPE), types("List"));
    assertEquals(List.of(PrismioTypes.STDLIB_TYPE), types("Option"));
    // The compiler's table defines I8, I16 and I64 but never a 32-bit signed
    // type, so this must not be highlighted as one.
    assertEquals(List.of(PrismioTypes.IDENTIFIER), types("I32"));
  }

  public void testRangeIsNotTwoDotsAndNotAFloat() {
    assertEquals(List.of(PrismioTypes.INTEGER, PrismioTypes.RANGE, PrismioTypes.IDENTIFIER),
        types("0..n"));
    assertEquals(List.of("0", "..", "n"), texts("0..n"));
    // A '.' continues a number only when a digit follows it.
    assertEquals(List.of(PrismioTypes.FLOAT), types("3.14"));
    assertEquals(List.of(PrismioTypes.IDENTIFIER, PrismioTypes.DOT, PrismioTypes.IDENTIFIER),
        types("point.x"));
  }

  public void testDigitSeparatorsAreNotPartOfTheLanguage() {
    // `1_000` is one identifier-shaped run in the compiler's scanner: an integer
    // followed by the identifier `_000`. The old lexer accepted it as a number.
    assertEquals(List.of(PrismioTypes.INTEGER, PrismioTypes.IDENTIFIER), types("1_000"));
  }

  public void testBlockCommentsNest() {
    assertEquals(List.of(PrismioTypes.BLOCK_COMMENT), types("/* outer /* inner */ still */"));
    assertEquals(List.of(PrismioTypes.BLOCK_COMMENT, PrismioTypes.KEYWORD),
        types("/* a /* b */ c */ return"));
    // One close is not enough to end a nested comment: the rest of the file is
    // still comment, exactly as the compiler reads it.
    assertEquals(List.of(PrismioTypes.BLOCK_COMMENT), types("/* a /* b */"));
  }

  public void testAnyNumberOfStarsIsStillOneComment() {
    // The depth counter reacts to `/*` and `*/` and to nothing else, so extra
    // stars are ordinary content. Each of these is one token and nothing leaks
    // out of it.
    assertEquals(List.of(PrismioTypes.DOC_COMMENT), types("/**\n\n*/"));
    assertEquals(List.of(PrismioTypes.DOC_COMMENT), types("/** on one line */"));
    assertEquals(List.of(PrismioTypes.DOC_COMMENT), types("/**\n * shaped like javadoc\n */"));

    // Empty ones are not documentation: there is nothing in them to render.
    assertEquals(List.of(PrismioTypes.BLOCK_COMMENT), types("/**/"));
    assertEquals(List.of(PrismioTypes.BLOCK_COMMENT), types("/***/"));
    assertEquals(List.of(PrismioTypes.BLOCK_COMMENT), types("/**** banner ****/"));

    // And they really close -- the trailing `/` must not leak into the code.
    assertEquals(List.of(PrismioTypes.INTEGER, PrismioTypes.BLOCK_COMMENT,
        PrismioTypes.ARITHMETIC_OP, PrismioTypes.INTEGER), types("1 /**/ + 1"));
    assertEquals(List.of(PrismioTypes.INTEGER, PrismioTypes.BLOCK_COMMENT,
        PrismioTypes.ARITHMETIC_OP, PrismioTypes.INTEGER), types("1 /***/ + 1"));
  }

  public void testLineAndDocComments() {
    assertEquals(List.of(PrismioTypes.LINE_COMMENT), types("// ordinary"));
    assertEquals(List.of(PrismioTypes.DOC_COMMENT), types("/// documentation"));
    assertEquals(List.of(PrismioTypes.LINE_COMMENT), types("//// a rule, not documentation"));
  }

  public void testStringsAndCharactersStopAtTheLineEnd() {
    assertEquals(List.of(PrismioTypes.STRING_LITERAL), types("\"escaped \\\" quote\""));
    assertEquals(List.of(PrismioTypes.CHARACTER_LITERAL), types("'p'"));
    assertEquals(List.of(PrismioTypes.CHARACTER_LITERAL), types("'\\n'"));
    // An unterminated literal must not swallow the rest of the file.
    assertEquals(List.of(PrismioTypes.STRING_LITERAL, PrismioTypes.KEYWORD),
        types("\"unterminated\nreturn"));
  }

  public void testOperatorsAreClassified() {
    assertEquals(List.of(PrismioTypes.ARROW), types("->"));
    assertEquals(List.of(PrismioTypes.FAT_ARROW), types("=>"));
    assertEquals(List.of(PrismioTypes.RELATIONAL_OP), types("!="));
    assertEquals(List.of(PrismioTypes.SHIFT_OP), types(">>"));
    assertEquals(List.of(PrismioTypes.ASSIGNMENT_OP), types("+="));
    assertEquals(List.of(PrismioTypes.ASSIGNMENT_OP), types("="));
    assertEquals(List.of(PrismioTypes.NEGATION), types("!"));
  }

  public void testTildeAndQuestionMark() {
    // Both were bad characters until the corpus test lexed a real checkout:
    // `~` is bitwise NOT and `?` is the nullable-type suffix.
    assertEquals(List.of(PrismioTypes.BITWISE_OP, PrismioTypes.IDENTIFIER), types("~mask"));
    assertEquals(List.of(PrismioTypes.BUILTIN_TYPE, PrismioTypes.OPTIONAL), types("Int?"));
    // `~=` is not a compound assignment; there is no compound bitwise NOT.
    assertEquals(List.of(PrismioTypes.BITWISE_OP, PrismioTypes.ASSIGNMENT_OP), types("~="));
  }

  public void testSemicolonIsRejected() {
    // `;` is in neither isSeparator nor isOperator, so `let x = 1;` does not
    // compile. The editor has to agree, or it hides a build failure.
    assertEquals(List.of(PrismioTypes.INTEGER, TokenType.BAD_CHARACTER), types("1;"));
  }

  public void testLexerCoversEveryByteOfARealisticFile() {
    assertCoversInput("""
        // A file exercising most of the grammar.
        /* including /* nested */ comments */
        import std.io

        extern fn read_file(path: String borrow) -> String produce(free)

        public fn main() -> Int {
            let mut total = 0
            for i in 0..10 {
                if (i > 3 and i != 7) { total += i }
            }
            let text = "a \\"quoted\\" word"
            let c = 'x'
            let flags = ~0
            let maybe: Int? = none
            match total { 0 => return 1, _ => return 0 }
        }
        """);
  }
}
