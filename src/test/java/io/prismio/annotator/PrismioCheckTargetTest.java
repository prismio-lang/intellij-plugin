package io.prismio.annotator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import junit.framework.TestCase;

/**
 * Which program a file is checked through. Each case is a layout the Prismio repository has:
 * the compiler's modules under {@code src/} reached from a manifest's entry, the standard
 * library, a test program one directory above the modules it imports, and a program of its own.
 */
public class PrismioCheckTargetTest extends TestCase {

  private Path root;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    root = Files.createTempDirectory("prismio-check-target-").toRealPath();
  }

  @Override
  protected void tearDown() throws Exception {
    try (Stream<Path> walk = Files.walk(root)) {
      walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
    }
    super.tearDown();
  }

  private Path write(String relative, String text) throws IOException {
    Path path = root.resolve(relative);
    Files.createDirectories(path.getParent());
    Files.writeString(path, text);
    return path;
  }

  private List<PrismioCheckTarget> plan(Path file) throws IOException {
    return PrismioCheckTarget.plan(file, Files.readString(file), root);
  }

  public void testAModuleIsCheckedThroughItsManifestsEntry() throws IOException {
    write("build.ums", "targets {\n    executable(\"app\") {\n        entry = \"src/main.psm\"\n    }\n}\n");
    Path main = write("src/main.psm", "import parse.*\nfn main() -> Int { return 0 }\n");
    Path stmt = write("src/parse/stmt.psm", "import lexer.token\nfn parseStatement(p: Parser) { }\n");

    List<PrismioCheckTarget> targets = plan(stmt);
    assertEquals(new PrismioCheckTarget(main, null), targets.get(0));
    assertEquals("the file alone is the last resort", new PrismioCheckTarget(stmt, null),
        targets.get(targets.size() - 1));
  }

  public void testAProgramIsItsOwnEntry() throws IOException {
    write("build.ums", "targets {\n    executable(\"app\") {\n        entry = \"src/main.psm\"\n    }\n}\n");
    Path tool = write("src/tool.psm", "import std.io\n\nfn main() -> Int { return 0 }\n");
    assertEquals(List.of(new PrismioCheckTarget(tool, null)), plan(tool));
  }

  public void testAStandardLibraryModuleIsCheckedAsItself() throws IOException {
    write("std/io.psm", "");
    write("std/string.psm", "");
    Path map = write("std/map.psm", "public struct Map<K, V> { }\n");
    assertEquals(List.of(new PrismioCheckTarget(map, "std.map")), plan(map));
  }

  public void testAFolderMerelyNamedStdIsNotTheLibrary() throws IOException {
    Path helper = write("app/std/helpers.psm", "fn help() { }\n");
    assertNull(PrismioCheckTarget.plan(helper, "fn help() { }\n", root).get(0).module());
  }

  public void testAModuleIsFoundByTheProgramThatImportsIt() throws IOException {
    Path beta = write("tests/visibility/beta.psm", "import visibility.alpha\n");
    write("tests/test_1_unrelated.psm", "import std.io\nfn main() -> Int { return 0 }\n");
    Path importer = write("tests/test_2_groups.psm",
        "import {alpha, beta} from visibility\nfn main() -> Int { return 0 }\n");

    List<PrismioCheckTarget> targets = plan(beta);
    assertEquals(List.of(new PrismioCheckTarget(importer, null), new PrismioCheckTarget(beta, null)), targets);
  }

  public void testImportSpellings() {
    assertTrue(PrismioCheckTarget.importsModule("import parse.stmt\n", "parse.stmt"));
    assertTrue(PrismioCheckTarget.importsModule("import parse.stmt as s\n", "parse.stmt"));
    assertTrue(PrismioCheckTarget.importsModule("import parse.stmt.parseFor\n", "parse.stmt"));
    assertTrue(PrismioCheckTarget.importsModule("import parse.*\n", "parse.stmt"));
    assertTrue(PrismioCheckTarget.importsModule("import * from parse\n", "parse.stmt"));
    assertTrue(PrismioCheckTarget.importsModule("import {expr, stmt as s} from parse\n", "parse.stmt"));
    assertFalse(PrismioCheckTarget.importsModule("import parse.stmts\n", "parse.stmt"));
    assertFalse(PrismioCheckTarget.importsModule("import {statement} from parse\n", "parse.stmt"));
    assertFalse(PrismioCheckTarget.importsModule("// import parse.stmt\n", "parse.stmt"));
  }

  public void testModuleNames() {
    assertEquals("parse.stmt", PrismioCheckTarget.moduleName(root.resolve("src"), root.resolve("src/parse/stmt.psm")));
    assertEquals("common", PrismioCheckTarget.moduleName(root, root.resolve("common.psm")));
  }
}
