package io.prismio.ums;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

/**
 * Spellcheck prose, not names.
 *
 * <p>Comments and string values are worth checking — a `description` is a
 * sentence a person reads. Identifiers are not: every block name in a manifest
 * is a term of art, and checking them would underline `prismio`, `testImplementation`
 * and every target the project happens to have.
 */
public final class UmsSpellcheckingStrategy extends SpellcheckingStrategy {
  @Override
  public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
    var type = element.getNode() != null ? element.getNode().getElementType() : null;
    if (type != null && (UmsTypes.COMMENTS.contains(type) || UmsTypes.STRING.equals(type))) {
      return super.getTokenizer(element);
    }
    return EMPTY_TOKENIZER;
  }
}
