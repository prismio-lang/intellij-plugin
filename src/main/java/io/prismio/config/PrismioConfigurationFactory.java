package io.prismio.config;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrismioConfigurationFactory extends ConfigurationFactory {

    public PrismioConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return PrismioRunConfigurationType.ID;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new PrismioRunConfiguration(project, this, "Prismio");
    }

    @Nullable
    @Override
    public Class<? extends BaseState> getOptionsClass() {
        return PrismioRunConfigurationOptions.class;
    }
}
