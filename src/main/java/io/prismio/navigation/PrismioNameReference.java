package io.prismio.navigation;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import io.prismio.psi.PrismioFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A name, resolved to the declaration it means.
 *
 * <p>Soft, because an unresolved name is not an error here: the plugin has no
 * type information and no view of the toolchain's own `std`, so "I could not
 * find it" and "it does not exist" are not the same statement. A hard reference
 * would underline correct code in red.
 */
public final class PrismioNameReference extends PsiReferenceBase<PsiElement> {

  private PrismioNameReference(PsiElement element) {
    super(element, new TextRange(0, element.getTextLength()), /* soft = */ true);
  }

  /** Null when the element is a declaration: a name is not a reference to itself. */
  public static @Nullable PrismioNameReference forElement(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (!(file instanceof PrismioFile)) {
      return null;
    }
    // Otherwise Find Usages lists a declaration among its own usages, and
    // renaming one would try to rewrite the name twice.
    if (DeclarationScanner.findDeclaration(element) != null) {
      return null;
    }
    return new PrismioNameReference(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    PsiFile file = getElement().getContainingFile();
    if (file == null) {
      return null;
    }
    String name = getElement().getText();

    // The file wins over the project, matching the Go to Declaration handler.
    for (Declaration declaration : DeclarationScanner.collectCached(file)) {
      if (name.equals(declaration.getName()) && declaration.getElement() != null) {
        return declaration.getElement();
      }
    }
    return PrismioProjectDeclarations.firstNamed(file.getProject(), name, file);
  }

  @Override
  public Object @NotNull [] getVariants() {
    // Completion is contributed separately and is context-aware; a list of every
    // declaration here would shadow it with a worse one.
    return EMPTY_ARRAY;
  }
}
