package io.prismio.execution;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.prismio.ums.UmsFile;
import io.prismio.ums.UmsManifest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Run from inside a build.ums: the declaration under the caret or gutter icon —
 * {@code executable("app")} runs it, {@code test("t")} tests it,
 * {@code command("dist")} runs {@code prismio dist}.
 */
public final class UmsRunConfigurationProducer extends LazyRunConfigurationProducer<PrismioRunConfiguration> {

  @Override
  public @NotNull ConfigurationFactory getConfigurationFactory() {
    return PrismioRunConfigurationType.getInstance().getConfigurationFactories()[0];
  }

  private static @Nullable UmsRunConfigurations.Spec specAt(@NotNull ConfigurationContext context) {
    PsiElement location = context.getPsiLocation();
    if (location == null || !(location.getContainingFile() instanceof UmsFile)) {
      return null;
    }
    PsiFile file = location.getContainingFile();
    VirtualFile vf = file.getVirtualFile();
    if (vf == null || vf.getParent() == null || DumbService.isDumb(file.getProject())) {
      return null;
    }
    UmsManifest manifest = UmsManifest.read(file.getViewProvider().getContents());
    return UmsRunConfigurations.specForDeclaration(file.getProject(), vf, manifest,
        location.getTextRange().getStartOffset());
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull PrismioRunConfiguration configuration,
      @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
    UmsRunConfigurations.Spec spec = specAt(context);
    if (spec == null) {
      return false;
    }
    UmsRunConfigurations.apply(configuration, spec);
    configuration.setGenerated(true);
    sourceElement.set(context.getPsiLocation());
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull PrismioRunConfiguration configuration,
      @NotNull ConfigurationContext context) {
    UmsRunConfigurations.Spec spec = specAt(context);
    return spec != null && configuration.getFilePath().isBlank()
        && spec.command().equals(configuration.getCommand())
        && spec.target().equals(configuration.getTarget())
        && spec.workingDirectory().equals(configuration.getWorkingDirectory());
  }
}
