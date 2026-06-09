// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package io.prismio;

import com.intellij.openapi.fileTypes.LanguageFileType;
import io.prismio.utils.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class PsFileType extends LanguageFileType {

  public static final PsFileType INSTANCE = new PsFileType();

  private PsFileType() {
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
    return Icons.FILE;
  }

}
