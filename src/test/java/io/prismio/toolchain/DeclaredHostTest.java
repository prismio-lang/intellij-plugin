package io.prismio.toolchain;

import com.intellij.openapi.util.SystemInfo;
import java.nio.file.Files;
import java.nio.file.Path;
import junit.framework.TestCase;

/**
 * {@code toolchain.host} resolves as the compiler's launcher resolves it: relative to
 * {@code build.ums}, reaching outside the project with {@code ..}, or absolute.
 */
public class DeclaredHostTest extends TestCase {

  private static Path project(String manifest) throws Exception {
    Path root = Files.createTempDirectory("prismio-host");
    Files.writeString(root.resolve("build.ums"), manifest);
    return root;
  }

  private static String exe(String name) {
    return SystemInfo.isWindows ? name + ".exe" : name;
  }

  public void testARelativeHostIsUnderTheProject() throws Exception {
    Path root = project("toolchain {\n    host = \".prismio/build/debug/prismio\"\n}\n");
    assertEquals(root.resolve(exe(".prismio/build/debug/prismio")).normalize(),
        PrismioToolchainService.declaredHost(root));
  }

  public void testASubProjectMayNameItsParentsHost() throws Exception {
    Path root = project("toolchain {\n    host = \"../.prismio/build/debug/prismio\"\n}\n");
    assertEquals(root.getParent().resolve(exe(".prismio/build/debug/prismio")),
        PrismioToolchainService.declaredHost(root));
  }

  public void testAnAbsoluteHostIsUsedAsWritten() throws Exception {
    Path host = Files.createTempDirectory("prismio-abs").resolve("prismio");
    Path root = project("toolchain {\n    host = \"" + host.toString().replace("\\", "\\\\") + "\"\n}\n");
    assertEquals(Path.of(exe(host.toString())), PrismioToolchainService.declaredHost(root));
  }

  public void testNoToolchainBlockIsNoHost() throws Exception {
    Path root = project("project {\n    name = \"p\"\n}\n");
    assertNull(PrismioToolchainService.declaredHost(root));
  }
}
