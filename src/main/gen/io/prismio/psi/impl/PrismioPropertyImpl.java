// This is a generated file. Not intended for manual editing.
package io.prismio.psi.impl;

import io.prismio.psi.PrismioProperty;
import io.prismio.psi.PrismioVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import io.prismio.psi.*;
import com.intellij.navigation.ItemPresentation;

public class PrismioPropertyImpl extends PrismioNamedElementImpl implements PrismioProperty {

  public PrismioPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PrismioVisitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PrismioVisitor) accept((PrismioVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public String getKey() {
    return PrismioPsiImplUtil.getKey(this);
  }

  @Override
  public String getValue() {
    return PrismioPsiImplUtil.getValue(this);
  }

  @Override
  public String getName() {
    return PrismioPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String newName) {
    return PrismioPsiImplUtil.setName(this, newName);
  }

  @Override
  public PsiElement getNameIdentifier() {
    return PrismioPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public ItemPresentation getPresentation() {
    return PrismioPsiImplUtil.getPresentation(this);
  }
}