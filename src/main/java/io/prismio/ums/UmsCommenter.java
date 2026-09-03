package io.prismio.ums;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * `//` for Ctrl+/, and no block form.
 *
 * <p>UMS accepts `#` as well, and the lexer here reads both, but a commenter has
 * to pick one to *write*. `//` matches what `prismio init` generates and what
 * every manifest in the Prismio repository uses.
 */
public final class UmsCommenter implements Commenter {
  @Override
  public @Nullable String getLineCommentPrefix() {
    return "//";
  }

  @Override
  public @Nullable String getBlockCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getBlockCommentSuffix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentSuffix() {
    return null;
  }
}
