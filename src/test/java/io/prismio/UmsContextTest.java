package io.prismio;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import io.prismio.ums.UmsContext;
import io.prismio.ums.UmsFileType;
import io.prismio.ums.UmsWords;
import java.util.List;

/**
 * Position decides meaning in a manifest, so this is the part worth testing.
 *
 * <p>{@code library} is a target kind inside {@code targets} and a linker input
 * inside {@code link} — the same word, one nesting level apart, and the only
 * thing that tells them apart is the enclosing block path.
 */
public class UmsContextTest extends BasePlatformTestCase {

  private static final String MANIFEST = """
      toolchain {
          host = ".prismio/build/debug/prismio"
      }

      project {
          name = "app"
      }

      targets {
          executable("app") {
              entry = "src/main.psm"
              link {
                  library("sqlite3")
              }
          }
      }

      commands {
          command("dist") {
              description = "Package"
              run("tools/package.py", args)
          }
      }
      """;

  private PsiFile manifest() {
    return myFixture.configureByText(UmsFileType.INSTANCE, MANIFEST);
  }

  /** The block path at the offset just after the given snippet's opening brace. */
  private List<String> pathInsideBlockOpenedBy(String snippet) {
    PsiFile file = manifest();
    int brace = MANIFEST.indexOf(snippet) + snippet.length();
    return UmsContext.pathAt(file, brace + 1);
  }

  public void testTopLevelOffersTheBlockNames() {
    PsiFile file = manifest();
    assertEquals(List.of(), UmsContext.pathAt(file, 0));
    assertEquals(UmsWords.TOP_LEVEL_BLOCKS, UmsContext.completionsFor(List.of()));
  }

  public void testPathTracksNesting() {
    assertEquals(List.of("project"), pathInsideBlockOpenedBy("project {"));
    assertEquals(List.of("targets"), pathInsideBlockOpenedBy("targets {"));
    // A block opened by a call carries the call's name, not its argument.
    assertEquals(List.of("targets", "executable"),
        pathInsideBlockOpenedBy("executable(\"app\") {"));
    assertEquals(List.of("targets", "executable", "link"),
        pathInsideBlockOpenedBy("link {"));
    assertEquals(List.of("commands", "command"),
        pathInsideBlockOpenedBy("command(\"dist\") {"));
  }

  public void testTheSameWordMeansDifferentThingsAtDifferentDepths() {
    // `library` is a target kind at one level and a linker input at the next.
    assertTrue(UmsContext.completionsFor(List.of("targets")).containsKey("library"));
    assertTrue(UmsContext.completionsFor(List.of("targets", "executable", "link"))
        .containsKey("library"));
    // ...and `entry` is neither of those places.
    assertFalse(UmsContext.completionsFor(List.of("targets")).containsKey("entry"));
    assertTrue(UmsContext.completionsFor(List.of("targets", "executable")).containsKey("entry"));
  }

  public void testUnmodelledBlocksOfferNothingRatherThanTheWrongThing() {
    // A block this plugin does not know: UMS parses new blocks before the model
    // learns them, so the honest answer is "no suggestions", not the top-level
    // list.
    assertNull(UmsContext.completionsFor(List.of("somethingNew")));
    assertNull(UmsContext.completionsFor(List.of("project", "nested")));
  }

  public void testCommandStepsAreRecognised() {
    var body = UmsContext.completionsFor(List.of("commands", "command"));
    assertNotNull(body);
    assertTrue(body.containsKey("build"));
    assertTrue(body.containsKey("run"));
    assertTrue(body.containsKey("shell"));
    assertTrue(body.containsKey("description"));
  }
}
