package io.prismio.execution;

import java.util.List;
import junit.framework.TestCase;

/**
 * What the run configuration hands the compiler. {@code prismio run} reads every argument it
 * does not know as its own mistake (P1050), so a program's arguments must sit behind a {@code --}.
 */
public class PrismioRunCommandLineTest extends TestCase {

  public void testAFileAloneIsRunWithNoSeparator() {
    assertEquals(List.of("run", "/w/src/main.psm"),
        PrismioRunProfileState.arguments("/w/src/main.psm", "", ""));
  }

  public void testProgramArgumentsFollowADoubleDash() {
    assertEquals(List.of("run", "/w/src/main.psm", "--", "input.txt", "-v"),
        PrismioRunProfileState.arguments("/w/src/main.psm", "", "input.txt -v"));
  }

  public void testAQuotedArgumentIsOneArgument() {
    assertEquals(List.of("run", "/w/main.psm", "--", "two words", "x"),
        PrismioRunProfileState.arguments("/w/main.psm", "", "\"two words\" x"));
  }

  public void testAnArgumentThatLooksLikeACompilerFlagStaysTheProgramsOwn() {
    // Without the `--` the compiler would read `--release` as its own.
    List<String> arguments = PrismioRunProfileState.arguments("/w/main.psm", "", "--release");
    assertEquals(List.of("run", "/w/main.psm", "--", "--release"), arguments);
  }

  public void testProjectModeNamesTheTargetThenTheProgramsArguments() {
    assertEquals(List.of("run", "app", "--", "a", "b"),
        PrismioRunProfileState.arguments("", "app", "a b"));
  }

  public void testProjectModeWithNoTargetRunsTheOnlyExecutable() {
    assertEquals(List.of("run"), PrismioRunProfileState.arguments("", "", ""));
    assertEquals(List.of("run", "--", "a"), PrismioRunProfileState.arguments(null, null, "a"));
  }

  public void testAFileWinsOverATarget() {
    assertEquals(List.of("run", "/w/main.psm"),
        PrismioRunProfileState.arguments("/w/main.psm", "app", "  "));
  }

  // ------------------------------------------------------------ project commands

  public void testProjectCommandsAsTheCliSpellsThem() {
    assertEquals(List.of("build"), PrismioRunProfileState.arguments("build", "", "", "", false));
    assertEquals(List.of("build", "--release", "app"),
        PrismioRunProfileState.arguments("build", "", "app", "", true));
    assertEquals(List.of("test", "--release"), PrismioRunProfileState.arguments("test", "", "", "", true));
    assertEquals(List.of("clean", "--release"), PrismioRunProfileState.arguments("clean", "", "app", "", true));
    assertEquals(List.of("run", "--release", "app", "--", "x"),
        PrismioRunProfileState.arguments("run", "", "app", "x", true));
  }

  public void testManifestCommandTakesItsArgumentsDirectly() {
    // `prismio ship --dry-run`: the command's steps splice them in where the manifest writes `args`.
    assertEquals(List.of("ship", "--dry-run", "two words"),
        PrismioRunProfileState.arguments("ship", "", "", "--dry-run \"two words\"", false));
    assertEquals(List.of("dist"), PrismioRunProfileState.arguments("dist", "", "", "", true));
  }
}
