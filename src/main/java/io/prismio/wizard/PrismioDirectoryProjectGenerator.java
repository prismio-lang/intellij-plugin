package io.prismio.wizard;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.GitRepositoryInitializer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.dsl.builder.AlignX;
import com.intellij.ui.dsl.builder.BuilderKt;
import kotlin.Unit;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import com.intellij.platform.ProjectGeneratorPeer;
import io.prismio.icons.PrismioIcons;
import io.prismio.toolchain.PrismioProjectSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * File | New | Project | Prismio in the IDEs that are not IntelliJ IDEA -- CLion, PyCharm,
 * WebStorm, Rider, RustRover and the rest. They list {@code DirectoryProjectGenerator}s; the
 * {@code GeneratorNewProjectWizard} IDEA lists works only where the Java plugin is. Both write
 * the same project, through {@link PrismioProjectFiles}, and ask for the compiler the same way,
 * through {@link CompilerSelector}.
 */
public final class PrismioDirectoryProjectGenerator
    extends DirectoryProjectGeneratorBase<PrismioDirectoryProjectGenerator.Settings>
    implements CustomStepProjectGenerator<PrismioDirectoryProjectGenerator.Settings> {

  /** What the dialog ended with: the compiler and the version it answered, and whether to start a Git repository. */
  public record Settings(@NotNull String compiler, @Nullable String version, @Nullable String detected, boolean git) {}

  @Override
  public @NotNull String getName() {
    return "Prismio";
  }

  @Override
  public @NotNull Icon getLogo() {
    return PrismioIcons.FILE;
  }

  @Override
  public @NotNull String getDescription() {
    return "A Prismio project with a build.ums manifest and a main program";
  }

  /**
   * The step that shows the peer's panel. A plain {@code DirectoryProjectGenerator} is given a
   * default step in CLion that shows only the Location field, so the compiler row never appears;
   * the base step is what shows the panel {@link Peer#getComponent} returns, as CLion's own Rust and
   * Meson generators do.
   */
  @Override
  public @NotNull AbstractActionWithPanel createStep(@NotNull DirectoryProjectGenerator<Settings> generator,
                                                     @NotNull AbstractNewProjectStep.AbstractCallback<Settings> callback) {
    return new ProjectSettingsStepBase<>(generator, new AbstractNewProjectStep.AbstractCallback<>());
  }

  @Override
  public @NotNull ProjectGeneratorPeer<Settings> createPeer() {
    return new Peer();
  }

  @Override
  public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Settings settings,
                              @NotNull Module module) {
    String name = PrismioProjectFiles.projectName(baseDir.getName());
    String version = settings.version() != null ? settings.version() : PrismioProjectFiles.FALLBACK_VERSION;
    if (!settings.compiler().isEmpty() && !settings.compiler().equals(settings.detected())) {
      PrismioProjectSettings.getInstance(project).getState().compilerPath = settings.compiler();
    }
    try {
      VirtualFile main = WriteAction.compute(() -> PrismioProjectFiles.create(baseDir, name, version));
      PrismioProjectStep.open(project, main);
      if (settings.git()) {
        // After the files exist, so the first commit has something in it; off the UI thread, since
        // it runs `git init`.
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          GitRepositoryInitializer git = GitRepositoryInitializer.getInstance();
          if (git != null) {
            git.initRepository(project, baseDir, true);
          }
        });
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not create the Prismio project files in " + Path.of(baseDir.getPath()), e);
    }
  }

  @Override
  public @NotNull ValidationResult validate(@NotNull String baseDirPath) {
    return ValidationResult.OK;
  }

  private static final class Peer implements ProjectGeneratorPeer<Settings> {
    private final CompilerSelector compiler = new CompilerSelector();
    private final JBTextField nameField = new JBTextField();
    private final JBCheckBox gitBox = new JBCheckBox("Create Git repository");
    private final boolean gitAvailable = GitRepositoryInitializer.getInstance() != null;

    /**
     * CLion's dialog asks for the panel and hands over its Location field and a way to validate
     * again. That dialog has a Location and no Name or Git option, which IDEA's has, so they are
     * added here: the name is the last folder of the location and the two follow each other.
     */
    @Override
    public @NotNull JComponent getComponent(@NotNull TextFieldWithBrowseButton location, @NotNull Runnable checkValid) {
      compiler.onChange(checkValid);
      followLocation(location);
      return BuilderKt.panel(panel -> {
        panel.row("Name:", row -> {
          row.cell(nameField).align(AlignX.FILL);
          return Unit.INSTANCE;
        });
        if (gitAvailable) {
          panel.row("", row -> {
            row.cell(gitBox);
            return Unit.INSTANCE;
          });
        }
        compiler.addRows(panel);
        return Unit.INSTANCE;
      });
    }

    /** Name and Location are one fact told twice: typing in either rewrites the other. */
    private void followLocation(@NotNull TextFieldWithBrowseButton location) {
      boolean[] syncing = {false};
      nameField.setText(lastSegment(location.getText()));
      location.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent event) {
          if (!syncing[0] && !nameField.getText().equals(lastSegment(location.getText()))) {
            syncing[0] = true;
            nameField.setText(lastSegment(location.getText()));
            syncing[0] = false;
          }
        }
      });
      nameField.getDocument().addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent event) {
          if (syncing[0] || nameField.getText().isBlank()) {
            return;
          }
          syncing[0] = true;
          File parent = new File(location.getText()).getParentFile();
          location.setText(new File(parent, nameField.getText().trim()).getPath());
          syncing[0] = false;
        }
      });
    }

    private static @NotNull String lastSegment(@NotNull String path) {
      return new File(path.trim()).getName();
    }

    /** The dialogs that build a settings step instead. */
    @Override
    public void buildUI(@NotNull SettingsStep step) {
      step.addSettingsComponent(compiler.panel());
    }

    @Override
    public @NotNull Settings getSettings() {
      Path detected = compiler.detected();
      return new Settings(compiler.path(), compiler.version(), detected == null ? null : detected.toString(),
          gitAvailable && gitBox.isSelected());
    }

    @Override
    public @Nullable ValidationInfo validate() {
      String problem = compiler.problem();
      return problem == null ? null : new ValidationInfo(problem, compiler.field());
    }

    @Override
    public boolean isBackgroundJobRunning() {
      return false;
    }
  }
}
