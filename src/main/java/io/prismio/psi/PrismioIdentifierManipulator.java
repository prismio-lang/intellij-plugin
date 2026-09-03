package io.prismio.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * How the platform rewrites an identifier's text.
 *
 * <p>Required for rename: the refactoring resolves each reference and then asks
 * a manipulator to change it. Without one registered, renaming fails outright
 * with "No ElementManipulator instance registered" rather than degrading.
 */
public final class PrismioIdentifierManipulator
    extends AbstractElementManipulator<PrismioIdentifier> {

  @Override
  public @Nullable PrismioIdentifier handleContentChange(
      @NotNull PrismioIdentifier element, @NotNull TextRange range, String newContent)
      throws IncorrectOperationException {
    String replaced = range.replace(element.getText(), newContent);
    return element.replaceWithText(replaced) instanceof PrismioIdentifier renamed
        ? renamed
        : null;
  }
}
