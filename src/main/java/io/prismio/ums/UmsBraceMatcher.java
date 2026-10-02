package io.prismio.ums;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UmsBraceMatcher implements PairedBraceMatcher {
  private static final BracePair[] PAIRS = new BracePair[] {
      // Structural: a block is what the structure view and folding are built on.
      new BracePair(UmsTypes.LEFT_BRACE, UmsTypes.RIGHT_BRACE, true),
      new BracePair(UmsTypes.LEFT_PAREN, UmsTypes.RIGHT_PAREN, false),
      new BracePair(UmsTypes.LEFT_BRACKET, UmsTypes.RIGHT_BRACKET, false),
  };

  @Override
  public BracePair @NotNull [] getPairs() {
    return PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType type,
      @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
