package io.prismio.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import io.prismio.navigation.PrismioNameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An identifier token, as a PSI element that can carry a reference.
 *
 * <p>**Why this class exists rather than a {@code PsiReferenceContributor}.**
 * The platform never asks a plain {@code LeafPsiElement} for contributed
 * references: {@code PsiReferenceService} returned zero for one of these even
 * with a contributor registered and its pattern matching. References have to
 * come from the element itself, so the element has to be ours.
 *
 * <p>It is created by {@link io.prismio.psi.PrismioAstFactory} and keeps the
 * ordinary {@link PrismioTypes#IDENTIFIER} element type. That is deliberate: the
 * annotator, formatter, completion and navigation all key on that type, and
 * making identifiers a *composite* node instead would have changed the type
 * every one of them sees.
 */
public final class PrismioIdentifier extends LeafPsiElement implements PsiNamedElement {

  public PrismioIdentifier(@NotNull IElementType type, @NotNull CharSequence text) {
    super(type, text);
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    PsiReference reference = getReference();
    return reference == null ? PsiReference.EMPTY_ARRAY : new PsiReference[] {reference};
  }

  @Override
  public @Nullable PsiReference getReference() {
    return PrismioNameReference.forElement(this);
  }

  @Override
  public @NotNull String getName() {
    return getText();
  }

  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    // A leaf's text is its identity, so renaming is a replacement. The platform
    // rewrites the references it found separately. `replaceWithText` answers with
    // the tree node rather than the PSI element, and for a leaf the two are the
    // same object.
    LeafElement replaced = replaceWithText(name);
    return replaced instanceof PsiElement element ? element : this;
  }
}
