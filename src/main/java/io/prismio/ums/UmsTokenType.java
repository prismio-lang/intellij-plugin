package io.prismio.ums;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class UmsTokenType extends IElementType {
  public UmsTokenType(@NotNull @NonNls String debugName) {
    super(debugName, UmsLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "UmsTokenType." + super.toString();
  }
}
