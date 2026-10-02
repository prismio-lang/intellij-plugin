package io.prismio.navigation;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import io.prismio.PrismioLanguage;
import io.prismio.lexer.PrismioLexer;
import io.prismio.psi.PrismioTokenSets;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrismioFindUsagesProvider implements FindUsagesProvider {
  @Override
  public @NotNull WordsScanner getWordsScanner() {
    return new DefaultWordsScanner(new PrismioLexer(), PrismioTokenSets.IDENTIFIERS,
        PrismioTokenSets.COMMENTS, TokenSet.EMPTY);
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement element) {
    return element.getLanguage().isKindOf(PrismioLanguage.INSTANCE)
        && element.getNode().getElementType() == PrismioTypes.IDENTIFIER;
  }

  @Override
  public @Nullable String getHelpId(@NotNull PsiElement element) {
    return null;
  }

  @Override
  public @NotNull String getType(@NotNull PsiElement element) {
    Declaration declaration = DeclarationScanner.findDeclaration(element);
    return declaration == null ? "identifier" : declaration.getKind().getDisplayName();
  }

  @Override
  public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
    return element.getText();
  }

  @Override
  public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    return element.getText();
  }
}
