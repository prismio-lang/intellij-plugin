package io.prismio.annotator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import junit.framework.TestCase;

/**
 * Every {@code .psm} in a real Prismio checkout, checked the way the editor checks it.
 *
 * <p>A checkout compiles, so any error the editor would show on one of its files is the
 * editor's mistake -- which is how "cannot read imported module `lexer.token`" on
 * {@code src/parse/stmt.psm} went unnoticed: the file is fine, and it was checked as a program
 * of its own. Negative fixtures are wrong on purpose, and {@code aif/evidence} and
 * {@code scratch} hold experiments against older versions of the language, so those are left out.
 *
 * <p>Skipped unless both {@code -Dprismio.checkout=<dir>} and {@code -Dprismio.compiler=<prismio>}
 * are given.
 */
public class PrismioCheckCorpusTest extends TestCase {

  private static final List<String> SKIPPED_DIRS = List.of(
      "third_party", "build", ".prismio", "graphify-out", "node_modules", ".git", "evidence", "scratch");

  public void testNoFileInACheckoutShowsAnError() throws IOException {
    String checkout = System.getProperty("prismio.checkout");
    String compiler = System.getProperty("prismio.compiler");
    if (checkout == null || compiler == null || !Files.isDirectory(Path.of(checkout))) {
      return;
    }
    Path root = Path.of(checkout).toRealPath();

    List<Path> sources;
    try (Stream<Path> walk = Files.walk(root)) {
      sources = walk.filter(p -> p.toString().endsWith(".psm"))
          .filter(p -> !p.getFileName().toString().startsWith("neg_"))
          .filter(p -> root.relativize(p).getNameCount() > 0)
          .filter(p -> {
            for (Path part : root.relativize(p)) {
              if (SKIPPED_DIRS.contains(part.toString())) {
                return false;
              }
            }
            return true;
          })
          .sorted()
          .toList();
    }

    List<String> problems = new ArrayList<>();
    for (Path source : sources) {
      Path file = source.toRealPath();
      String text = Files.readString(file);
      PrismioExternalAnnotator.Checked checked = PrismioExternalAnnotator.checkThroughProgram(
          Path.of(compiler), file, text, file, root);
      if (checked == null) {
        problems.add(root.relativize(file) + ": no program could check it");
        continue;
      }
      for (PrismioDiagnostic d : PrismioExternalAnnotator.forFile(checked, file)) {
        if ("error".equalsIgnoreCase(d.severity()) || d.file() == null) {
          problems.add(root.relativize(file) + " via " + root.relativize(checked.target().entry()) + ":"
              + d.line() + ": " + d.message());
        }
      }
    }
    assertTrue(sources.size() + " files; editor errors on:\n" + String.join("\n", problems), problems.isEmpty());
  }
}
