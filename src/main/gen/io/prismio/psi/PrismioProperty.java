// This is a generated file. Not intended for manual editing.
package io.prismio.psi;

import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface PrismioProperty extends PrismioNamedElement {

  String getKey();

  String getValue();

  String getName();

  PsiElement setName(String newName);

  PsiElement getNameIdentifier();

  ItemPresentation getPresentation();
}