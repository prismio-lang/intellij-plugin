// This is a generated file. Not intended for manual editing.
package io.prismio.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class PrismioVisitor extends PsiElementVisitor {

  public void visitProperty(@NotNull PrismioProperty o) {
    visitNamedElement(o);
  }

  public void visitNamedElement(@NotNull PrismioNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }
}