package io.prismio.execution;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import io.prismio.icons.PrismioIcons;

public final class PrismioRunConfigurationType extends ConfigurationTypeBase {
  public static final String ID = "PrismioRunConfiguration";

  public PrismioRunConfigurationType() {
    super(ID, "Prismio", "Run Prismio applications", PrismioIcons.FILE);
    addFactory(new PrismioConfigurationFactory(this));
  }

  public static PrismioRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(PrismioRunConfigurationType.class);
  }
}
