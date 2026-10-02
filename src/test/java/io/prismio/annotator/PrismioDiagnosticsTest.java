package io.prismio.annotator;

import java.nio.file.Path;
import java.util.List;
import junit.framework.TestCase;

/** Reading the compiler's JSON, and choosing what of it belongs on the file being edited. */
public class PrismioDiagnosticsTest extends TestCase {

  private static final Path FILE = Path.of("/work/src/parse/stmt.psm").toAbsolutePath().normalize();
  private static final Path ENTRY = Path.of("/work/src/main.psm").toAbsolutePath().normalize();

  private static String record(String severity, String code, String file, int line, String message) {
    return "{\"kind\":\"diagnostic\",\"schemaVersion\":1,\"severity\":\"" + severity + "\",\"code\":"
        + (code == null ? "null" : "\"" + code + "\"") + ",\"file\":"
        + (file == null ? "null" : "\"" + file + "\"") + ",\"line\":" + line
        + ",\"column\":3,\"length\":2,\"message\":\"" + message + "\"}";
  }

  public void testANoteJoinsTheDiagnosticItExplains() {
    String output = String.join("\n",
        record("warning", "P4003", FILE.toString(), 5, "this range counts down"),
        record("note", null, null, 0, "write `start..<n`"),
        "{\"kind\":\"summary\",\"schemaVersion\":1,\"errors\":0,\"warnings\":1}");
    List<PrismioDiagnostic> parsed = PrismioDiagnosticParser.parseOutput(output);
    assertEquals(1, parsed.size());
    assertEquals("this range counts down\nnote: write `start..<n`", parsed.get(0).message());
    assertEquals(5, parsed.get(0).line());
  }

  public void testANoteWithNothingBeforeItIsDropped() {
    assertTrue(PrismioDiagnosticParser.parseOutput(record("note", null, null, 0, "orphan")).isEmpty());
  }

  public void testOnlyThisFilesDiagnosticsAreShownWithASummaryOfTheRest() {
    List<PrismioDiagnostic> all = PrismioDiagnosticParser.parseOutput(String.join("\n",
        record("error", "P4001", FILE.toString(), 12, "unknown type `Parsr`"),
        record("error", "P4001", "/work/src/lexer/token.psm", 40, "expected Int"),
        record("error", "P4001", "/work/src/ir/expr.psm", 7, "unknown name"),
        record("error", "P0001", null, 0, "too many errors")));
    List<PrismioDiagnostic> shown = PrismioExternalAnnotator.forFile(all, FILE, ENTRY);

    assertEquals(3, shown.size());
    assertEquals(12, shown.get(0).line());
    assertEquals("a diagnostic with no file is the check's, so it is shown", "too many errors",
        shown.get(1).message());
    assertNull(shown.get(2).file());
    assertTrue(shown.get(2).message(), shown.get(2).message().startsWith("2 errors in other files of the program `main.psm`"));
    assertTrue(shown.get(2).message().contains("token.psm:40"));
  }

  public void testNoSummaryWhenTheRestOfTheProgramIsClean() {
    List<PrismioDiagnostic> all = PrismioDiagnosticParser.parseOutput(
        record("warning", "P4003", "/work/src/ir/expr.psm", 7, "a warning elsewhere"));
    assertTrue(PrismioExternalAnnotator.forFile(all, FILE, ENTRY).isEmpty());
  }

  public void testAnUntrustedProjectHostIsNotAFaultOnTheFile() {
    // The launcher leaves a host this machine did not build alone (P1077) and the global
    // compiler checks the file. In JSON mode the launcher says nothing; a compiler that did would
    // still not put an error, or a banner, on every open file.
    List<PrismioDiagnostic> all = PrismioDiagnosticParser.parseOutput(String.join("\n",
        record("warning", "P1077", null, 0, "the project compiler was not built on this machine, so it will not be run"),
        record("note", null, null, 0, "`prismio build` builds it here"),
        record("error", "P4001", FILE.toString(), 12, "unknown type `Parsr`")));
    List<PrismioDiagnostic> shown = PrismioExternalAnnotator.forFile(all, FILE, ENTRY);
    assertEquals(1, shown.size());
    assertEquals("P4001", shown.get(0).code());
  }

  public void testTheLaunchersOtherStatusWarningsAreNotAFaultEither() {
    List<PrismioDiagnostic> all = PrismioDiagnosticParser.parseOutput(String.join("\n",
        record("warning", "P1052", null, 0, "the configured project host is not runnable"),
        record("warning", "P1064", null, 0, "the project compiler is from an older toolchain generation"),
        record("note", null, null, 0, "rebuilding .prismio/build/debug/prismio with the global compiler first"),
        record("warning", "P1065", null, 0, "no target in this project builds the configured compiler host"),
        record("warning", "P4003", FILE.toString(), 3, "a real warning")));
    List<PrismioDiagnostic> shown = PrismioExternalAnnotator.forFile(all, FILE, ENTRY);
    assertEquals(1, shown.size());
    assertEquals("P4003", shown.get(0).code());
  }

  public void testTheLaunchersTextWarningIsNotParsedAsADiagnostic() {
    String output = String.join("\n",
        "warning[P1077]: the project compiler was not built on this machine, so it will not be run",
        "  note: .prismio/build/debug/prismio",
        record("error", "P4001", FILE.toString(), 12, "unknown type `Parsr`"));
    List<PrismioDiagnostic> parsed = PrismioDiagnosticParser.parseOutput(output);
    assertEquals(1, parsed.size());
    assertEquals("P4001", parsed.get(0).code());
  }

  public void testWithOnlyTheUntrustedWarningTheFileIsClean() {
    List<PrismioDiagnostic> all = PrismioDiagnosticParser.parseOutput(
        record("warning", "P1077", null, 0, "the project compiler was not built on this machine"));
    assertTrue(PrismioExternalAnnotator.forFile(all, FILE, ENTRY).isEmpty());
  }
}
