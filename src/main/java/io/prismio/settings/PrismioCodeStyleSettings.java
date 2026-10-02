package io.prismio.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public final class PrismioCodeStyleSettings extends CustomCodeStyleSettings {
  public PrismioCodeStyleSettings(CodeStyleSettings settings) {
    super(PrismioCodeStyleSettings.class.getSimpleName(), settings);
  }
}
