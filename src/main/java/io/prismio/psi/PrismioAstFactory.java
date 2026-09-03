package io.prismio.psi;

import com.intellij.lang.ASTFactory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gives identifier tokens a PSI class of their own.
 *
 * <p>Returning null for everything else leaves the platform's default leaf in
 * place, so this adds one class rather than taking over element creation.
 */
public final class PrismioAstFactory extends ASTFactory {
  @Override
  public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
    return type == PrismioTypes.IDENTIFIER ? new PrismioIdentifier(type, text) : null;
  }
}
