package io.prismio.ums;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public final class UmsFile extends PsiFileBase {
  public UmsFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, UmsLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return UmsFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "UMS manifest";
  }
}
