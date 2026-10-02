package io.prismio.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.prismio.PrismioFileType;
import io.prismio.psi.PrismioFile;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Declarations across every Prismio file the project indexes.
 *
 * <p>One implementation, used by both Go to Declaration and reference
 * resolution, so the two cannot disagree about what a name means.
 */
public final class PrismioProjectDeclarations {

  private PrismioProjectDeclarations() {}

  /** Every declaration of {@code name} outside {@code exclude}, in index order. */
  public static @NotNull List<PsiElement> allNamed(
      @NotNull Project project, @NotNull String name, @Nullable PsiFile exclude) {
    List<PsiElement> found = new ArrayList<>();
    PsiManager manager = PsiManager.getInstance(project);

    for (VirtualFile virtualFile :
        FileTypeIndex.getFiles(PrismioFileType.INSTANCE, GlobalSearchScope.allScope(project))) {
      PsiFile file = manager.findFile(virtualFile);
      if (!(file instanceof PrismioFile) || file.equals(exclude)) {
        continue;
      }
      for (Declaration declaration : DeclarationScanner.collectCached(file)) {
        if (name.equals(declaration.getName()) && declaration.getElement() != null) {
          found.add(declaration.getElement());
        }
      }
    }
    return found;
  }

  public static @Nullable PsiElement firstNamed(
      @NotNull Project project, @NotNull String name, @Nullable PsiFile exclude) {
    List<PsiElement> found = allNamed(project, name, exclude);
    return found.isEmpty() ? null : found.get(0);
  }
}
