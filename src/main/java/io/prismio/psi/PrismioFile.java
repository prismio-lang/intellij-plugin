package io.prismio.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import io.prismio.PrismioFileType;
import io.prismio.PrismioLanguage;
import org.jetbrains.annotations.NotNull;

public class PrismioFile extends PsiFileBase {
  public PrismioFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, PrismioLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return PrismioFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Prismio File";
  }
}
