package io.prismio.navigation;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import io.prismio.PrismioFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrismioChooseByNameContributor implements ChooseByNameContributorEx {
  @Override
  public void processNames(@NotNull Processor<? super String> processor,
      @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    if (scope.getProject() == null) {
      return;
    }
    PsiManager psiManager = PsiManager.getInstance(scope.getProject());
    for (var virtualFile : FileTypeIndex.getFiles(PrismioFileType.INSTANCE, scope)) {
      PsiFile file = psiManager.findFile(virtualFile);
      if (file != null) {
        for (Declaration declaration : DeclarationScanner.collect(file)) {
          if (!processor.process(declaration.getName())) {
            return;
          }
        }
      }
    }
  }

  @Override
  public void processElementsWithName(@NotNull String name,
      @NotNull Processor<? super NavigationItem> processor,
      @NotNull FindSymbolParameters parameters) {
    PsiManager psiManager = PsiManager.getInstance(parameters.getProject());
    for (var virtualFile :
        FileTypeIndex.getFiles(PrismioFileType.INSTANCE, parameters.getSearchScope())) {
      PsiFile file = psiManager.findFile(virtualFile);
      if (file != null) {
        for (Declaration declaration : DeclarationScanner.collect(file)) {
          if (name.equals(declaration.getName()) && !processor.process(declaration)) {
            return;
          }
        }
      }
    }
  }
}
