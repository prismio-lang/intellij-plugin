package io.prismio.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.dsl.builder.BuilderKt;
import io.prismio.toolchain.PrismioToolchainService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import kotlin.Unit;

public class PrismioProjectStepTest extends BasePlatformTestCase {

  /** Byte for byte what `prismio init demo` writes. */
  public void testManifestIsWhatInitWrites() {
    assertEquals("""
        project {
            name = "demo"
            version = "0.1.0"
            prismio = "0.1.0"
        }

        targets {
            executable("demo") {
                entry = "src/main.psm"
            }
        }
        """, PrismioProjectFiles.manifest("demo", "0.1.0"));
  }

  public void testNamesUmsAccepts() {
    assertEquals("my-app.v2", PrismioProjectFiles.projectName("my-app.v2"));
    assertEquals("_x", PrismioProjectFiles.projectName("_x"));
  }

  public void testNamesUmsRefusesAreRewritten() {
    assertEquals("my_project", PrismioProjectFiles.projectName("my project"));
    assertEquals("_hidden", PrismioProjectFiles.projectName("-hidden"));
    assertEquals("_x", PrismioProjectFiles.projectName(".x"));
    assertEquals("app", PrismioProjectFiles.projectName("   "));
  }

  /** A compiler is whatever answers `--version` with a `prismio <version>` first line. */
  public void testCompilerVersionIsReadFromTheFirstLine() throws Exception {
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      return;
    }
    Path good = script("#!/bin/sh\necho 'prismio 9.9.9'\necho 'llvm 23'\n");
    Path other = script("#!/bin/sh\necho 'gcc 14'\n");
    Path failing = script("#!/bin/sh\necho 'prismio 1.0.0'\nexit 3\n");
    assertEquals("9.9.9", PrismioToolchainService.compilerVersion(good));
    assertNull(PrismioToolchainService.compilerVersion(other));
    assertNull(PrismioToolchainService.compilerVersion(failing));
    assertNull(PrismioToolchainService.compilerVersion(good.resolveSibling("missing")));
  }

  /**
   * The step's UI builds, and Create is allowed exactly when the field starts out holding a
   * compiler this machine has: with none installed it is blocked, never silently let through.
   */
  public void testCreateIsBlockedWithoutACompiler() throws Exception {
    NewProjectWizardStep step = new PrismioNewProjectWizard().createStep(new WizardContext(null, getTestRootDisposable()));
    DialogPanel panel = BuilderKt.panel(p -> {
      step.setupUI(p);
      return Unit.INSTANCE;
    });
    Path installed = PrismioToolchainService.detectInstalledCompiler();
    boolean works = installed != null && PrismioToolchainService.compilerVersion(installed) != null;
    assertEquals(works ? 0 : 1, panel.validateAll().size());
  }

  /** The wizard offers "Create Git repository" like the other languages' do. */
  public void testTheWizardOffersAGitRepository() {
    NewProjectWizardStep step = new PrismioNewProjectWizard().createStep(new WizardContext(null, getTestRootDisposable()));
    assertNotNull(com.intellij.ide.wizard.GitNewProjectWizardData.Companion.getGitData(step));
  }

  /**
   * CLion's dialog asks the peer for a component with its Location field in hand; an empty default
   * there is what once left the dialog with a Location and nothing else.
   */
  public void testTheOtherIdesDialogHasTheCompilerRow() {
    var peer = new PrismioDirectoryProjectGenerator().createPeer();
    javax.swing.JComponent component = peer.getComponent(new com.intellij.openapi.ui.TextFieldWithBrowseButton(), () -> {});
    assertNotNull(component);
    boolean hasField = com.intellij.util.ui.UIUtil.findComponentsOfType(component,
        com.intellij.openapi.ui.TextFieldWithBrowseButton.class).size() == 1;
    assertTrue("the panel holds the compiler path field", hasField);
    Path installed = PrismioToolchainService.detectInstalledCompiler();
    boolean works = installed != null && PrismioToolchainService.compilerVersion(installed) != null;
    assertEquals(works, peer.validate() == null);
  }

  /**
   * The step CLion shows for the generator is the platform's own base step; its panel must hold the
   * compiler row beside the Location field. A plain DirectoryProjectGenerator gets a default step
   * that shows Location alone, which is what this guards against.
   */
  public void testTheStepShowsTheCompilerRowBesideLocation() {
    PrismioDirectoryProjectGenerator generator = new PrismioDirectoryProjectGenerator();
    var step = generator.createStep(generator, new com.intellij.ide.util.projectWizard.AbstractNewProjectStep.AbstractCallback<>());
    javax.swing.JPanel panel = ((com.intellij.ide.util.projectWizard.ProjectSettingsStepBase<?>) step).createPanel();
    int fields = com.intellij.util.ui.UIUtil.findComponentsOfType(panel,
        com.intellij.openapi.ui.TextFieldWithBrowseButton.class).size();
    assertTrue("Location and the compiler path, found " + fields, fields >= 2);
  }

  /** CLion's dialog has Location only; the panel adds Name, which follows the location both ways. */
  public void testTheOtherIdesDialogAddsANameThatFollowsTheLocation() {
    var peer = new PrismioDirectoryProjectGenerator().createPeer();
    var location = new com.intellij.openapi.ui.TextFieldWithBrowseButton();
    location.setText("/tmp/projects/untitled");
    javax.swing.JComponent component = peer.getComponent(location, () -> {});
    var name = com.intellij.util.ui.UIUtil.findComponentOfType(component, com.intellij.ui.components.JBTextField.class);
    assertNotNull("a Name field", name);
    assertEquals("untitled", name.getText());

    name.setText("demo");
    assertEquals("/tmp/projects/demo", location.getText());

    location.setText("/tmp/projects/other");
    assertEquals("other", name.getText());

    boolean gitOffered = com.intellij.openapi.GitRepositoryInitializer.getInstance() != null;
    boolean hasGitBox = !com.intellij.util.ui.UIUtil.findComponentsOfType(component,
        com.intellij.ui.components.JBCheckBox.class).isEmpty();
    assertEquals("Create Git repository is offered exactly when Git is there", gitOffered, hasGitBox);
  }

  private Path script(String body) throws Exception {
    Path file = Files.createTempFile("prismio-fake-", ".sh");
    Files.writeString(file, body);
    Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"));
    file.toFile().deleteOnExit();
    return file;
  }
}
