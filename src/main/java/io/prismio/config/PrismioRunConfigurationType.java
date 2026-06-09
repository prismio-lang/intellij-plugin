package io.prismio.config;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;
import io.prismio.utils.Icons;

final class PrismioRunConfigurationType extends ConfigurationTypeBase {

    static final String ID = "PrismioRunConfiguration";
    static final String NAME = "Prismio";


    PrismioRunConfigurationType() {
        super(ID,NAME, "", NotNullLazyValue.createValue(() -> Icons.FILE));
        addFactory(new PrismioConfigurationFactory(this) {});
    }
}
