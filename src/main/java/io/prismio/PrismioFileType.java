// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the
// Apache 2.0 license.

package io.prismio;

import com.intellij.openapi.fileTypes.LanguageFileType;
import io.prismio.icons.PrismioIcons;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public final class PrismioFileType extends LanguageFileType {
  public static final PrismioFileType INSTANCE = new PrismioFileType();

  private PrismioFileType() {
    super(PrismioLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Prismio File";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Prismio language file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "psm";
  }

  @Override
  public Icon getIcon() {
    return PrismioIcons.FILE;
  }
}
