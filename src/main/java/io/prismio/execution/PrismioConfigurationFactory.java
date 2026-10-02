package io.prismio.execution;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class PrismioConfigurationFactory extends ConfigurationFactory {

  public PrismioConfigurationFactory(@NotNull ConfigurationType type) {
    super(type);
  }

  @Override
  public @NotNull String getId() {
    return "Prismio";
  }

  @Override
  public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new PrismioRunConfiguration(project, this, "Prismio");
  }
}
