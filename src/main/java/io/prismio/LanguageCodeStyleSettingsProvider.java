// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package io.prismio;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import org.jetbrains.annotations.NotNull;

final class LanguageCodeStyleSettingsProvider extends com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider {

  @NotNull
  @Override
  public Language getLanguage() {
    return PrismioLanguage.INSTANCE;
  }

  @Override
  public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions(
              "SPACE_AROUND_ASSIGNMENT_OPERATORS",
              "SPACE_AFTER_COMMA",
              "SPACE_BEFORE_METHOD_CALL_PARENTHESES",
              "SPACE_WITHIN_PARENTHESES",
              "SPACE_WITHIN_BRACKETS"
      );
      consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Around operators");
      consumer.renameStandardOption("SPACE_AFTER_COMMA", "After comma");
    } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
    } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      consumer.showStandardOptions(
              "KEEP_LINE_BREAKS",
              "KEEP_FIRST_COLUMN_COMMENT"
      );
    }
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    return """
        # You are reading the ".properties" entry.
        ! The exclamation mark can also mark text as comments.
        website = https://en.wikipedia.org/

        language = English
        # The backslash below tells the application to continue reading
        # the value onto the next line.
        message = Welcome to \\
                  Wikipedia!
        # Add spaces to the key
        key\\ with\\ spaces = This is the value that could be looked up with the key "key with spaces".
        # Unicode
        tab : \\u0009""";
  }

}
