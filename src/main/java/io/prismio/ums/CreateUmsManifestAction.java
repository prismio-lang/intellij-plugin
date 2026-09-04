package io.prismio.ums;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import io.prismio.icons.PrismioIcons;
import org.jetbrains.annotations.NotNull;

/**
 * New > UMS Manifest.
 *
 * <p>The template is the shape `prismio init` writes, minus the parts that only
 * make sense once a project has them: no `dependencies` block, because there is
 * no registry to fetch from, and no `toolchain` block, because a project only
 * needs one when it builds its own compiler.
 */
public final class CreateUmsManifestAction extends CreateFileFromTemplateAction {
  @Override
  protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory,
      CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle("New UMS Manifest")
        .addKind("UMS Manifest", PrismioIcons.UMS, "UmsManifest");
  }

  @Override
  protected String getActionName(
      PsiDirectory directory, @NotNull String newName, String templateName) {
    return "Create UMS Manifest: " + newName;
  }
}
