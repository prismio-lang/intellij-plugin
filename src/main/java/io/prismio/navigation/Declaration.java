package io.prismio.navigation;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Declaration implements NavigationItem {
  private final SmartPsiElementPointer<PsiElement> pointer;
  private final String name;
  private final DeclarationKind kind;

  Declaration(@NotNull PsiElement element, @NotNull String name, @NotNull DeclarationKind kind) {
    pointer = SmartPointerManager.createPointer(element);
    this.name = name;
    this.kind = kind;
  }

  public @NotNull DeclarationKind getKind() {
    return kind;
  }

  public @Nullable PsiElement getElement() {
    return pointer.getElement();
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Override
      public @NotNull String getPresentableText() {
        return name;
      }

      @Override
      public @Nullable String getLocationString() {
        PsiElement element = pointer.getElement();
        return element == null || element.getContainingFile() == null
            ? null
            : element.getContainingFile().getName();
      }

      @Override
      public @NotNull Icon getIcon(boolean unused) {
        return kind.getIcon();
      }
    };
  }

  @Override
  public void navigate(boolean requestFocus) {
    PsiElement element = pointer.getElement();
    if (element != null && element.getContainingFile() != null) {
      new OpenFileDescriptor(element.getProject(), element.getContainingFile().getVirtualFile(),
          element.getTextOffset())
          .navigate(requestFocus);
    }
  }

  @Override
  public boolean canNavigate() {
    PsiElement element = pointer.getElement();
    return element != null && element.isValid() && element.getContainingFile() != null;
  }

  @Override
  public boolean canNavigateToSource() {
    return canNavigate();
  }
}
