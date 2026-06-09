// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package io.prismio.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.prismio.psi.PrismioProperty;
import io.prismio.psi.PrismioTypes;
import io.prismio.psi.PrismioElementFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PrismioPsiImplUtil {

  public static String getKey(PrismioProperty element) {
    ASTNode keyNode = element.getNode().findChildByType(PrismioTypes.KEY);
    if (keyNode != null) {
      // IMPORTANT: Convert embedded escaped spaces to simple spaces
      return keyNode.getText().replaceAll("\\\\ ", " ");
    } else {
      return null;
    }
  }

  public static String getValue(PrismioProperty element) {
    // STATEMENT is a deprecated token type that the current lexer never emits.
    // Fall back to the first IDENTIFIER or STRING_LITERAL child as the "value" representation.
    ASTNode valueNode = element.getNode().findChildByType(PrismioTypes.IDENTIFIER);
    if (valueNode == null) {
      valueNode = element.getNode().findChildByType(PrismioTypes.STRING_LITERAL);
    }
    if (valueNode != null) {
      return valueNode.getText();
    }
    return null;
  }

  public static String getName(PrismioProperty element) {
    return getKey(element);
  }

  public static PsiElement setName(PrismioProperty element, String newName) {
    ASTNode keyNode = element.getNode().findChildByType(PrismioTypes.KEY);
    if (keyNode != null) {
      PrismioProperty property = PrismioElementFactory.createProperty(element.getProject(), newName);
      ASTNode newKeyNode = property.getFirstChild().getNode();
      element.getNode().replaceChild(keyNode, newKeyNode);
    }
    return element;
  }

  public static PsiElement getNameIdentifier(PrismioProperty element) {
    ASTNode keyNode = element.getNode().findChildByType(PrismioTypes.KEY);
    if (keyNode != null) {
      return keyNode.getPsi();
    } else {
      return null;
    }
  }

  public static ItemPresentation getPresentation(final PrismioProperty element) {
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return element.getKey();
      }

      @Nullable
      @Override
      public String getLocationString() {
        PsiFile containingFile = element.getContainingFile();
        return containingFile == null ? null : containingFile.getName();
      }

      @Override
      public Icon getIcon(boolean unused) {
        return element.getIcon(0);
      }
    };
  }

}
