package io.prismio.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Cmd+Click and Cmd+B on a name.
 *
 * <p>Resolution is by name, because the plugin has no type information: the
 * compiler owns the AST and this side sees a flat token stream. That is enough
 * for the case people actually use — jumping from a call to the `fn` that
 * declares it — and where a name is declared more than once the platform shows
 * the usual chooser rather than guessing.
 *
 * <p>The current file is searched first. A local declaration shadows one from
 * elsewhere in the project, which matches how a reader thinks about it and
 * avoids a chooser for the common case of a helper used in the file that
 * defines it.
 *
 * <p>What this deliberately does not do: resolve a method call through its
 * receiver's type. `point.distance()` finds every `distance` in the project, not
 * the one on `Point`. Narrowing that correctly needs the type checker, and a
 * wrong single answer would be worse than an honest list.
 */
public final class PrismioGotoDeclarationHandler implements GotoDeclarationHandler {

  @Override
  public PsiElement @Nullable [] getGotoDeclarationTargets(
      @Nullable PsiElement source, int offset, Editor editor) {
    if (source == null || source.getNode() == null) {
      return null;
    }
    if (source.getNode().getElementType() != PrismioTypes.IDENTIFIER) {
      return null;
    }
    PsiFile file = source.getContainingFile();
    if (!(file instanceof PrismioFile)) {
      return null;
    }

    String name = source.getText();
    if (name == null || name.isEmpty()) {
      return null;
    }

    // `import std.io` navigates to the module, not to something called `io`.
    PsiFile module = ImportPaths.resolveModuleAt(source);
    if (module != null) {
      return new PsiElement[] {module};
    }

    List<PsiElement> targets = declarationsNamed(file, name, source);
    if (targets.isEmpty()) {
      targets = PrismioProjectDeclarations.allNamed(file.getProject(), name, file);
    }
    return targets.isEmpty() ? null : targets.toArray(PsiElement.EMPTY_ARRAY);
  }

  private static List<PsiElement> declarationsNamed(
      PsiFile file, String name, @Nullable PsiElement exclude) {
    List<PsiElement> targets = new ArrayList<>();
    for (Declaration declaration : DeclarationScanner.collectCached(file)) {
      if (!name.equals(declaration.getName())) {
        continue;
      }
      PsiElement element = declaration.getElement();
      // Clicking the declaration itself should do nothing, not offer to jump to
      // where the caret already is.
      if (element != null && element != exclude
          && element.getTextRange().getStartOffset() != offsetOf(exclude)) {
        targets.add(element);
      }
    }
    return targets;
  }

  private static int offsetOf(@Nullable PsiElement element) {
    return element == null ? -1 : element.getTextRange().getStartOffset();
  }

  @Override
  public @Nullable String getActionText(@NotNull com.intellij.openapi.actionSystem.DataContext context) {
    return null;
  }
}
