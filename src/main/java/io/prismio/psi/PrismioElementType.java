// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package io.prismio.psi;

import com.intellij.psi.tree.IElementType;
import io.prismio.PrismioLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PrismioElementType extends IElementType {

  public PrismioElementType(@NotNull @NonNls String debugName) {
    super(debugName, PrismioLanguage.INSTANCE);
  }

}
