package io.prismio.psi;

import com.intellij.psi.tree.IElementType;
import io.prismio.PrismioLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PrismioTokenType extends IElementType {
  public PrismioTokenType(@NotNull @NonNls String debugName) {
    super(debugName, PrismioLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "PrismioTokenType." + super.toString();
  }
}
