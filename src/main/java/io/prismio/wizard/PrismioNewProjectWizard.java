package io.prismio.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.GeneratorNewProjectWizard;
import com.intellij.ide.wizard.GitNewProjectWizardStep;
import com.intellij.ide.wizard.NewProjectWizardBaseStep;
import com.intellij.ide.wizard.NewProjectWizardChainStep;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.RootNewProjectWizardStep;
import io.prismio.icons.PrismioIcons;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

/**
 * File | New | Project | Prismio: a name and a location, an optional Git repository, then the project {@code prismio init}
 * writes -- {@code build.ums}, {@code src/main.psm} and a {@code .gitignore}.
 */
public final class PrismioNewProjectWizard implements GeneratorNewProjectWizard {

  @Override
  public @NotNull String getId() {
    return "io.prismio.project";
  }

  @Override
  public @NotNull String getName() {
    return "Prismio";
  }

  @Override
  public @NotNull Icon getIcon() {
    return PrismioIcons.FILE;
  }

  @Override
  public @NotNull String getDescription() {
    return "A Prismio project with a build.ums manifest and a main program";
  }

  @Override
  public @NotNull NewProjectWizardStep createStep(@NotNull WizardContext context) {
    return new NewProjectWizardChainStep<>(new RootNewProjectWizardStep(context))
        .nextStep(NewProjectWizardBaseStep::new)
        // "Create Git repository", as every language's wizard offers; absent where Git is not installed.
        .nextStep(GitNewProjectWizardStep::new)
        .nextStep(PrismioProjectStep::new);
  }
}
