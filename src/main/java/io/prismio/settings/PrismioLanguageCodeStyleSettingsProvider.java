package io.prismio.settings;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import io.prismio.PrismioLanguage;
import org.jetbrains.annotations.NotNull;

public final class PrismioLanguageCodeStyleSettingsProvider
    extends LanguageCodeStyleSettingsProvider {
  @Override
  public @NotNull Language getLanguage() {
    return PrismioLanguage.INSTANCE;
  }

  @Override
  public void customizeSettings(
      @NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS", "SPACE_AFTER_COMMA",
          "SPACE_BEFORE_METHOD_CALL_PARENTHESES", "SPACE_WITHIN_PARENTHESES",
          "SPACE_WITHIN_BRACKETS");
      consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Around operators");
      consumer.renameStandardOption("SPACE_AFTER_COMMA", "After comma");
    } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
    } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      consumer.showStandardOptions("KEEP_LINE_BREAKS", "KEEP_FIRST_COLUMN_COMMENT");
    }
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    return """
        struct Point {
          x: Int,
          y: Int
        }

        fn distance(point: Point) -> Int {
          let squared = point.x * point.x + point.y * point.y
          return squared
        }
        """;
  }
}
