package io.prismio.template;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import io.prismio.icons.PrismioIcons;
import org.jetbrains.annotations.NotNull;

public final class CreatePrismioFileAction extends CreateFileFromTemplateAction {
  @Override
  protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory,
      CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle("New Prismio File").addKind("Prismio File", PrismioIcons.FILE, "PrismioFile");
  }

  @Override
  protected String getActionName(
      PsiDirectory directory, @NotNull String newName, String templateName) {
    return "Create Prismio File: " + newName;
  }
}
