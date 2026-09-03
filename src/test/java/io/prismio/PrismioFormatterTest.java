package io.prismio;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Reformatting must not change what a file means.
 *
 * <p>That is the whole bar for a formatter, and it is easy to miss with a
 * spacing table: a rule written for one token pair silently applies to every
 * pair that matches it first. `and` and `or` are keywords used as infix
 * operators, so a blanket "no space after an identifier" rule turned
 * `a and b` into `aand b`.
 */
public class PrismioFormatterTest extends BasePlatformTestCase {

  private String reformat(String source) {
    PsiFile file = myFixture.configureByText(PrismioFileType.INSTANCE, source);
    WriteCommandAction.runWriteCommandAction(getProject(), (Runnable) () ->
        CodeStyleManager.getInstance(getProject()).reformat(file));
    return file.getText();
  }

  /** Reformatting is idempotent and preserves every token, in order. */
  private void assertTokensPreserved(String source) {
    String formatted = reformat(source);
    assertEquals("reformatting must not change the token stream",
        tokenText(source), tokenText(formatted));
  }

  private static String tokenText(String source) {
    io.prismio.lexer.PrismioLexer lexer = new io.prismio.lexer.PrismioLexer();
    lexer.start(source, 0, source.length(), 0);
    StringBuilder out = new StringBuilder();
    while (lexer.getTokenType() != null) {
      if (lexer.getTokenType() != com.intellij.psi.TokenType.WHITE_SPACE) {
        out.append(source, lexer.getTokenStart(), lexer.getTokenEnd()).append('');
      }
      lexer.advance();
    }
    return out.toString();
  }

  public void testWordOperatorsKeepTheirSpaces() {
    assertEquals("let ok = a and b", reformat("let ok = a and b"));
    assertEquals("let ok = a or b", reformat("let ok = a or b"));
    assertEquals("let ok = a and b or c", reformat("let ok = a and b or c"));
  }

  /** Every keyword that can follow an expression, not just the two operators. */
  public void testKeywordsAfterAnIdentifierKeepTheirSpaces() {
    assertEquals("let n = value as Int", reformat("let n = value as Int"));
    assertEquals("for item in items { }", reformat("for item in items { }"));
  }

  public void testGenericArgumentsAreNotSpacedApart() {
    // `<` and `>` lex as relational operators, so a blanket "space around
    // relational" rule turns a type argument list into a comparison.
    assertEquals("let items: List<Int> = list_new()",
        reformat("let items: List<Int> = list_new()"));
    assertEquals("fn first<T>(values: List<T>) -> T { }",
        reformat("fn first<T>(values: List<T>) -> T { }"));
  }

  public void testComparisonsStillGetSpaces() {
    assertEquals("if (a < b) { }", reformat("if (a<b) { }"));
    assertEquals("if (a >= b) { }", reformat("if (a>=b) { }"));
  }

  public void testIndexingAndOptionalStayTight() {
    assertEquals("let x = values[0]", reformat("let x = values[0]"));
    assertEquals("let maybe: Int? = none", reformat("let maybe: Int? = none"));
  }

  public void testCallsAndFieldAccessStayTight() {
    assertEquals("let n = point.x", reformat("let n = point.x"));
    assertEquals("println(value)", reformat("println(value)"));
    assertEquals("let n = strFromInt(point.x)", reformat("let n = strFromInt(point.x)"));
  }

  public void testRangeStaysTight() {
    assertEquals("for i in 0..10 { }", reformat("for i in 0..10 { }"));
  }

  /**
   * Reformatting every real source file in a checkout must preserve its tokens.
   *
   * <p>This is the property Cmd+Option+L is judged on, and the one that was
   * broken: a formatter that changes the token stream changes the program.
   * Skipped without `-Dprismio.checkout`.
   */
  public void testReformattingACheckoutPreservesEveryToken() throws Exception {
    String checkout = System.getProperty("prismio.checkout");
    if (checkout == null || !java.nio.file.Files.isDirectory(java.nio.file.Path.of(checkout))) {
      return;
    }

    java.util.List<java.nio.file.Path> sources;
    try (var walk = java.nio.file.Files.walk(java.nio.file.Path.of(checkout))) {
      sources = walk.filter(p -> p.toString().endsWith(".psm"))
          .filter(p -> !p.getFileName().toString().startsWith("neg_"))
          .sorted()
          .toList();
    }

    java.util.List<String> changed = new java.util.ArrayList<>();
    for (java.nio.file.Path source : sources) {
      String text = java.nio.file.Files.readString(source);
      String formatted = reformat(text);
      if (!tokenText(text).equals(tokenText(formatted))) {
        changed.add(source.getFileName().toString());
      }
    }

    assertTrue("reformatting changed the token stream of " + changed.size() + " file(s): "
        + changed.subList(0, Math.min(changed.size(), 10)), changed.isEmpty());
    assertTrue("the checkout should contain sources", !sources.isEmpty());
  }

  public void testReformattingARealisticFilePreservesEveryToken() {
    assertTokensPreserved("""
        import std.io

        struct Point { x: Int, y: Int }

        fn classify<T: Ord>(values: List<T>, limit: Int) -> Bool {
            let mut total = 0
            for i in 0..10 {
                if (i > 3 and i != 7 or total < limit) {
                    total += i
                }
            }
            let maybe: Int? = none
            return total >= limit
        }
        """);
  }
}
