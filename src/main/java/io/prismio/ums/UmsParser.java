package io.prismio.ums;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * A flat parse, for the same reason as the Prismio one: the compiler's UMS
 * parser produces the model, and everything the IDE offers is derived from the
 * token stream. Structure view walks braces rather than a tree.
 */
public final class UmsParser implements PsiParser {
  @Override
  public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
    PsiBuilder.Marker file = builder.mark();
    while (!builder.eof()) {
      builder.advanceLexer();
    }
    file.done(root);
    return builder.getTreeBuilt();
  }
}
