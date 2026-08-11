package io.prismio.navigation;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrismioStructureViewModel
    extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
  public PrismioStructureViewModel(@Nullable Editor editor, @NotNull PsiFile file) {
    super(file, editor, new PrismioStructureViewElement(file));
  }

  @Override
  public Sorter @NotNull[] getSorters() {
    return new Sorter[] {Sorter.ALPHA_SORTER};
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return element.getValue() instanceof Declaration;
  }
}
