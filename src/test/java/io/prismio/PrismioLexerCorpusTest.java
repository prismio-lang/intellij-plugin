package io.prismio;

import com.intellij.psi.TokenType;
import io.prismio.lexer.PrismioLexer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import junit.framework.TestCase;

/**
 * The lexer against every {@code .psm} file in a real Prismio checkout.
 *
 * <p>Hand-written cases prove the rules the author thought of. This proves the
 * ones they did not: the compiler, its standard library and its test suite are
 * tens of thousands of lines of source that is known to compile, so any byte
 * this lexer calls a bad character is a byte it is wrong about.
 *
 * <p>Skipped unless {@code -Dprismio.checkout=<dir>} points at one, so the suite
 * still passes for a contributor who only has the plugin.
 */
public class PrismioLexerCorpusTest extends TestCase {

  public void testEveryFileInACheckoutLexesCleanly() throws IOException {
    String checkout = System.getProperty("prismio.checkout");
    if (checkout == null || !Files.isDirectory(Path.of(checkout))) {
      return;
    }

    List<String> problems = new ArrayList<>();
    int files = 0;
    long tokens = 0;

    try (Stream<Path> walk = Files.walk(Path.of(checkout))) {
      List<Path> sources = walk.filter(p -> p.toString().endsWith(".psm"))
          // Negative fixtures are deliberately malformed; several are malformed
          // *lexically*, which is the one thing this test would report as a bug.
          .filter(p -> !p.getFileName().toString().startsWith("neg_"))
          .sorted()
          .toList();

      for (Path source : sources) {
        String text = Files.readString(source);
        PrismioLexer lexer = new PrismioLexer();
        lexer.start(text, 0, text.length(), 0);

        int expected = 0;
        while (lexer.getTokenType() != null) {
          if (lexer.getTokenStart() != expected) {
            problems.add(source + ": gap at offset " + expected);
            break;
          }
          if (lexer.getTokenEnd() <= lexer.getTokenStart()) {
            problems.add(source + ": empty token at offset " + lexer.getTokenStart());
            break;
          }
          if (lexer.getTokenType() == TokenType.BAD_CHARACTER) {
            problems.add(source + ": bad character `"
                + text.substring(lexer.getTokenStart(), lexer.getTokenEnd()) + "` at offset "
                + lexer.getTokenStart());
          }
          expected = lexer.getTokenEnd();
          tokens++;
          lexer.advance();
        }
        if (expected != text.length()) {
          problems.add(source + ": stopped at " + expected + " of " + text.length());
        }
        files++;
      }
    }

    assertTrue("lexed " + files + " files / " + tokens + " tokens, with problems:\n"
        + String.join("\n", problems.subList(0, Math.min(problems.size(), 20))),
        problems.isEmpty());
    assertTrue("the checkout should contain sources", files > 0);
  }

  /** The same sweep for manifests: every `.ums` in the checkout. */
  public void testEveryManifestInACheckoutLexesCleanly() throws IOException {
    String checkout = System.getProperty("prismio.checkout");
    if (checkout == null || !Files.isDirectory(Path.of(checkout))) {
      return;
    }

    List<String> problems = new ArrayList<>();
    int files = 0;

    try (Stream<Path> walk = Files.walk(Path.of(checkout))) {
      for (Path source : walk.filter(p -> p.toString().endsWith(".ums")).sorted().toList()) {
        String text = Files.readString(source);
        io.prismio.ums.UmsLexer lexer = new io.prismio.ums.UmsLexer();
        lexer.start(text, 0, text.length(), 0);

        int expected = 0;
        while (lexer.getTokenType() != null) {
          if (lexer.getTokenStart() != expected || lexer.getTokenEnd() <= lexer.getTokenStart()) {
            problems.add(source + ": discontinuity at offset " + expected);
            break;
          }
          if (lexer.getTokenType() == TokenType.BAD_CHARACTER) {
            problems.add(source + ": bad character `"
                + text.substring(lexer.getTokenStart(), lexer.getTokenEnd()) + "`");
          }
          expected = lexer.getTokenEnd();
          lexer.advance();
        }
        if (expected != text.length()) {
          problems.add(source + ": stopped at " + expected + " of " + text.length());
        }
        files++;
      }
    }

    assertTrue("lexed " + files + " manifests, with problems:\n" + String.join("\n", problems),
        problems.isEmpty());
  }
}
