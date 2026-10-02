package io.prismio.wizard;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.dsl.builder.AlignX;
import com.intellij.ui.dsl.builder.BuilderKt;
import com.intellij.ui.dsl.builder.Panel;
import com.intellij.util.ui.NamedColorUtil;
import com.intellij.util.ui.UIUtil;
import io.prismio.toolchain.PrismioToolchainService;
import kotlin.Unit;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The "Compiler:" controls of a New Project dialog: a path with Browse, an Auto-Detect button, and
 * the compiler's version beneath as proof it works. Shared by the dialog IntelliJ IDEA shows and the
 * one the other IDEs show, so they cannot drift apart.
 *
 * <p>Create must wait for {@link #problem()} to be null: a field that holds a compiler answering
 * {@code --version}.
 */
final class CompilerSelector {

  /** A success green that reads on both the light and the dark theme. */
  private static final JBColor OK_COLOR = new JBColor(0x1E7B34, 0x6CC27B);

  private final TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
  private final JButton detect = new JButton("Auto-Detect");
  private final JBLabel versionLabel = new JBLabel();
  private final Path detected = PrismioToolchainService.detectInstalledCompiler();
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

  /** What {@code --version} said for {@link #checkedPath}; null for a path that is not a compiler. */
  private volatile @Nullable String version;
  private volatile @Nullable String checkedPath;

  private enum Status { NEUTRAL, OK, ERROR }

  CompilerSelector() {
    field.addBrowseFolderListener(null,
        FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
            .withTitle("Select Prismio Compiler Executable"));
    field.setText(detected != null ? detected.toString() : "");
    field.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent event) {
        refresh();
      }
    });
    detect.addActionListener(event -> {
      Path found = PrismioToolchainService.detectInstalledCompiler();
      if (found != null) {
        field.setText(found.toString());
      } else {
        show(Status.ERROR, "No prismio found on PATH, in PRISMIO / PRISMIO_HOME or in the usual install folders.");
      }
    });
    refresh();
  }

  /**
   * The two rows: the path with Auto-Detect, and the version beneath. The path takes the row's
   * spare width and the button stays its own size at the end. {@code validationOnApply} is for the
   * dialog that applies its panel (IDEA's); the other asks {@link #problem()} itself.
   */
  void addRows(@NotNull Panel panel) {
    panel.row("Compiler:", row -> {
      row.cell(field)
          .align(AlignX.FILL)
          .resizableColumn()
          .validationOnApply((builder, component) -> {
            String problem = problem();
            return problem == null ? null : builder.error(problem);
          });
      row.cell(detect);
      return Unit.INSTANCE;
    });
    panel.row("", row -> {
      row.cell(versionLabel).align(AlignX.FILL);
      return Unit.INSTANCE;
    });
  }

  /** The panel of {@link #addRows}, for a dialog that wants a component and not a builder. */
  @NotNull JComponent panel() {
    return BuilderKt.panel(panel -> {
      addRows(panel);
      return Unit.INSTANCE;
    });
  }

  /** Called when the field or what the compiler answered changes, so a dialog can validate again. */
  void onChange(@NotNull Runnable listener) {
    listeners.add(listener);
  }

  /** The field, for a validation message to point at. */
  TextFieldWithBrowseButton field() {
    return field;
  }

  /** The path in the field, trimmed. */
  @NotNull String path() {
    return field.getText().trim();
  }

  /** The path of the compiler this machine finds by itself, or null. */
  @Nullable Path detected() {
    return detected;
  }

  /** The version of {@link #path()}, when it has been asked and answered; else null. */
  @Nullable String version() {
    return path().equals(checkedPath) ? version : null;
  }

  /** Asks the compiler in the field its version, off the UI thread, and shows what it said. */
  private void refresh() {
    String text = path();
    if (text.isEmpty()) {
      checkedPath = text;
      version = null;
      show(Status.NEUTRAL, "Select the Prismio compiler, or press Auto-Detect.");
      listeners.forEach(Runnable::run);
      return;
    }
    show(Status.NEUTRAL, "Checking...");
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      String answer = versionOf(text);
      ApplicationManager.getApplication().invokeLater(() -> {
        if (text.equals(path())) {
          checkedPath = text;
          version = answer;
          if (answer != null) {
            show(Status.OK, "prismio " + answer);
          } else {
            show(Status.ERROR, "Not a working Prismio compiler: --version did not answer.");
          }
          listeners.forEach(Runnable::run);
        }
      }, ModalityState.any());
    });
  }

  /** The reason Create must wait, or null: refuses an empty field and anything that is not a compiler. */
  @Nullable String problem() {
    String text = path();
    if (text.isEmpty()) {
      return "Select the Prismio compiler, or press Auto-Detect";
    }
    // The check on the typing thread may not have come back yet; ask here rather than guess.
    if (!text.equals(checkedPath)) {
      checkedPath = text;
      version = versionOf(text);
    }
    return version == null ? "Not a working Prismio compiler: --version did not answer" : null;
  }

  private static @Nullable String versionOf(@NotNull String path) {
    try {
      return PrismioToolchainService.compilerVersion(Path.of(path));
    } catch (InvalidPathException e) {
      return null;
    }
  }

  /** The line under the field: a green tick and the version, a red cross and the reason, or grey text. */
  private void show(@NotNull Status status, @NotNull String text) {
    versionLabel.setText(text);
    switch (status) {
      case OK -> {
        versionLabel.setIcon(AllIcons.General.InspectionsOK);
        versionLabel.setForeground(OK_COLOR);
      }
      case ERROR -> {
        versionLabel.setIcon(AllIcons.General.Error);
        versionLabel.setForeground(NamedColorUtil.getErrorForeground());
      }
      default -> {
        versionLabel.setIcon(null);
        versionLabel.setForeground(UIUtil.getContextHelpForeground());
      }
    }
  }
}
