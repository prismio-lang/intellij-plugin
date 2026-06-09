package io.prismio.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import io.prismio.PsFileType;
import io.prismio.PrismioLanguage;
import org.jetbrains.annotations.NotNull;

public class PrismioFile extends PsiFileBase {

  public PrismioFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, PrismioLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return PsFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Prismio File";
  }

}
