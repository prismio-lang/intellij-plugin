package io.prismio.settings;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.lang.Language;
import io.prismio.PrismioLanguage;
import org.jetbrains.annotations.NotNull;

public final class PrismioCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  @Override
  public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
    return new PrismioCodeStyleSettings(settings);
  }

  // Without a language the platform derives the configurable's id from the display name,
  // and logs that as an error each time Settings opens.
  @Override
  public @NotNull Language getLanguage() {
    return PrismioLanguage.INSTANCE;
  }

  @Override
  public String getConfigurableDisplayName() {
    return "Prismio";
  }

  @Override
  public @NotNull CodeStyleConfigurable createConfigurable(
      @NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
    return new CodeStyleAbstractConfigurable(
        settings, modelSettings, getConfigurableDisplayName()) {
      @Override
      protected @NotNull CodeStyleAbstractPanel createPanel(
          @NotNull CodeStyleSettings panelSettings) {
        return new PrismioCodeStylePanel(getCurrentSettings(), panelSettings);
      }
    };
  }

  private static final class PrismioCodeStylePanel extends TabbedLanguageCodeStylePanel {
    private PrismioCodeStylePanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
      super(PrismioLanguage.INSTANCE, currentSettings, settings);
    }
  }
}
