package io.prismio.symbols;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junit.framework.TestCase;

/**
 * The index against a real standard library: a checkout's {@code std/} with
 * {@code -Dprismio.checkout}, an installed toolchain's {@code stdlib/*.plib} with
 * {@code -Dprismio.compiler}. Skips without them, like the lexer corpus test.
 *
 * <p>The assertions are deliberately about the shape of the answer, not a list of
 * names: this is the test that would catch the parser losing {@code impl}
 * members or a {@code .plib} layout change, and it must not become the
 * hardcoded list the index exists to avoid.
 */
public class StdlibCorpusTest extends TestCase {

  public void testCheckoutStdIndexes() throws Exception {
    String checkout = System.getProperty("prismio.checkout");
    if (checkout == null) {
      return;
    }
    assertLibraryShape(StdlibIndex.read(Path.of(checkout, "std")), "checkout std/");
  }

  public void testInstalledPlibIndexes() throws Exception {
    String compiler = System.getProperty("prismio.compiler");
    if (compiler == null) {
      return;
    }
    Path root = Path.of(compiler).toRealPath().getParent().getParent();
    Path stdlib = root.resolve("stdlib");
    if (!Files.isDirectory(stdlib)) {
      return; // a bare compiler with no installed layout
    }
    assertLibraryShape(StdlibIndex.read(stdlib), stdlib.toString());
  }

  private static void assertLibraryShape(List<ModuleSummary> modules, String where) {
    assertTrue(where + ": expected modules, got " + modules.size(), modules.size() >= 10);
    Map<String, ModuleSummary> byName =
        modules.stream().collect(Collectors.toMap(ModuleSummary::name, m -> m));
    assertTrue(where + ": no std.io", byName.containsKey("std.io"));
    assertTrue(where + ": no std.string", byName.containsKey("std.string"));

    SymbolTable table = new SymbolTable();
    for (ModuleSummary module : modules) {
      table.add(module, SymbolTable.EXPORTED);
    }
    SymbolTable.Scope scope = new SymbolTable.Scope(List.of(table));
    List<Member> ofString = scope.instanceMembers(TypeRef.STRING);
    long properties = ofString.stream().filter(m -> m.kind() == Member.Kind.PROPERTY).count();
    long methods = ofString.stream().filter(m -> m.kind() == Member.Kind.METHOD).count();
    // Not `properties >= n`: a toolchain from before `prop` (2026-09-25) declares
    // `length` as a method, and the index must report that toolchain as it is.
    assertTrue(where + ": String has " + (properties + methods) + " members", properties + methods >= 10);
    assertTrue(where + ": no String.length in any form",
        ofString.stream().anyMatch(m -> m.name().equals("length")));
    assertFalse(where + ": no public print in std.io", scope.functions("print").isEmpty());
    for (Member member : ofString) {
      FnSig fn = member.function();
      assertTrue(where + ": private member offered: " + member.name(), fn == null || fn.visibleOutside());
    }
    if (Boolean.getBoolean("prismio.dump")) {
      for (Member member : ofString) {
        System.out.println("  " + member.origin() + " " + member.kind() + " " + member.name() + " " + member.module());
      }
    }
    System.out.println(where + ": " + modules.size() + " modules, String: " + properties
        + " properties, " + methods + " methods");
  }

  /** The parser over every module in a checkout: no exception, and every `fn` it reads has a name. */
  public void testParserReadsTheWholeCheckout() throws Exception {
    String checkout = System.getProperty("prismio.checkout");
    if (checkout == null) {
      return;
    }
    Path root = Path.of(checkout);
    int files = 0;
    int functions = 0;
    try (Stream<Path> walk = Files.walk(root)) {
      for (Path file : walk.filter(p -> p.toString().endsWith(".psm"))
          .filter(p -> !p.toString().contains("/.prismio/") && !p.toString().contains("/build/"))
          .toList()) {
        ModuleSummary summary = SignatureParser.parse(Files.readString(file), "corpus");
        files++;
        for (FnSig fn : summary.functions()) {
          assertFalse(file + ": empty function name", fn.name().isEmpty());
          functions++;
        }
      }
    }
    System.out.println("parsed " + files + " files, " + functions + " functions");
    assertTrue(files > 100);
  }
}
