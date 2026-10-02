package io.prismio.documentation;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Turns "Render documentation comments" on, once, so documentation comments show rendered.
 *
 * <p>The setting is IntelliJ's own, global and off by default, and there is no public way to
 * enable it for one language or one editor. So it is switched on the first time the plugin runs and
 * never again: a user who turns it off afterwards (Settings | Editor | General | Appearance, or the
 * gutter icon) keeps it off, and one who already had it on is not touched.
 */
public final class PrismioDocRenderDefault implements ProjectActivity {

  /** Set once the default has been applied, whatever the setting was then. */
  static final String APPLIED = "io.prismio.docRenderDefaultApplied";

  @Override
  public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    apply(PropertiesComponent.getInstance(), EditorSettingsExternalizable.getInstance());
    return Unit.INSTANCE;
  }

  static void apply(@NotNull PropertiesComponent properties, @NotNull EditorSettingsExternalizable settings) {
    if (properties.getBoolean(APPLIED)) {
      return;
    }
    properties.setValue(APPLIED, true);
    if (!settings.isDocCommentRenderingEnabled()) {
      settings.setDocCommentRenderingEnabled(true);
    }
  }
}
