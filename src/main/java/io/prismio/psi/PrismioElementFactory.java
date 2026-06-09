// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package io.prismio.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import io.prismio.PsFileType;

public class PrismioElementFactory {

  public static PrismioProperty createProperty(Project project, String name) {
    final PrismioFile file = createFile(project, name);
    return (PrismioProperty) file.getFirstChild();
  }

  public static PrismioFile createFile(Project project, String text) {
    String name = "dummy.psm";
    return (PrismioFile) PsiFileFactory.getInstance(project).createFileFromText(name, PsFileType.INSTANCE, text);
  }

  public static PrismioProperty createProperty(Project project, String name, String value) {
    final PrismioFile file = createFile(project, name + " = " + value);
    return (PrismioProperty) file.getFirstChild();
  }

  public static PsiElement createCRLF(Project project) {
    final PrismioFile file = createFile(project, "\n");
    return file.getFirstChild();
  }

}
