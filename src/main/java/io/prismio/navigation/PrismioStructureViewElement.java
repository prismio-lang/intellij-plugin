package io.prismio.navigation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class PrismioStructureViewElement
    implements StructureViewTreeElement, SortableTreeElement {
  private final Object value;

  public PrismioStructureViewElement(@NotNull Object value) {
    this.value = value;
  }

  @Override
  public @NotNull Object getValue() {
    return value;
  }

  @Override
  public void navigate(boolean requestFocus) {
    if (value instanceof Navigatable navigatable) {
      navigatable.navigate(requestFocus);
    }
  }

  @Override
  public boolean canNavigate() {
    return value instanceof Navigatable navigatable && navigatable.canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return value instanceof Navigatable navigatable && navigatable.canNavigateToSource();
  }

  @Override
  public @NotNull String getAlphaSortKey() {
    if (value instanceof Declaration declaration) {
      return declaration.getName();
    }
    if (value instanceof PsiFile file) {
      return file.getName();
    }
    return "";
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    if (value instanceof Declaration declaration) {
      return declaration.getPresentation();
    }
    if (value instanceof PsiFile file && file.getPresentation() != null) {
      return file.getPresentation();
    }
    return new PresentationData(getAlphaSortKey(), null, null, null);
  }

  @Override
  public TreeElement @NotNull[] getChildren() {
    if (value instanceof PsiFile file) {
      return DeclarationScanner.collect(file)
          .stream()
          .map(PrismioStructureViewElement::new)
          .toArray(TreeElement[] ::new);
    }
    return EMPTY_ARRAY;
  }
}
