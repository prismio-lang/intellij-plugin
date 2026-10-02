package io.prismio.execution;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gutter icon provider placing a green Run triangle next to `fn main()`.
 */
public final class PrismioRunLineMarkerContributor extends RunLineMarkerContributor {

  @Override
  public @Nullable Info getInfo(@NotNull PsiElement element) {
    if (element.getNode() == null || element.getNode().getElementType() != PrismioTypes.IDENTIFIER) {
      return null;
    }
    if (!"main".equals(element.getText())) {
      return null;
    }
    if (!(element.getContainingFile() instanceof PrismioFile)) {
      return null;
    }

    PsiElement prev = element.getPrevSibling();
    while (prev != null && (prev.getNode() == null || prev.getText().trim().isEmpty())) {
      prev = prev.getPrevSibling();
    }

    if (prev == null || prev.getNode() == null
        || prev.getNode().getElementType() != PrismioTypes.KEYWORD
        || !"fn".equals(prev.getText())) {
      return null;
    }

    return new Info(
        AllIcons.Actions.Execute,
        ExecutorAction.getActions(0),
        (e) -> "Run '" + element.getContainingFile().getName() + "'"
    );
  }
}
