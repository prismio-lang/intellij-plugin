package io.prismio.ums;

import com.intellij.openapi.fileTypes.LanguageFileType;
import io.prismio.icons.PrismioIcons;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public final class UmsFileType extends LanguageFileType {
  public static final UmsFileType INSTANCE = new UmsFileType();

  private UmsFileType() {
    super(UmsLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "UMS Manifest";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Prismio project manifest";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "ums";
  }

  @Override
  public Icon getIcon() {
    return PrismioIcons.FILE;
  }
}
