package io.prismio.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import io.prismio.navigation.Declaration;
import io.prismio.navigation.DeclarationScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides concise documentation for Prismio declarations. */
@SuppressWarnings("deprecation")
public final class PrismioDocumentationProvider extends AbstractDocumentationProvider {
  @Override
  public @Nullable String generateDoc(
      @NotNull PsiElement element, @Nullable PsiElement originalElement) {
    Declaration declaration = DeclarationScanner.findDeclaration(element);
    if (declaration == null) {
      return null;
    }

    String name = StringUtil.escapeXmlEntities(declaration.getName());
    String kind = StringUtil.escapeXmlEntities(declaration.getKind().getDisplayName());
    return DocumentationMarkup.DEFINITION_START + kind + " <b>" + name + "</b>"
        + DocumentationMarkup.DEFINITION_END + DocumentationMarkup.CONTENT_START + "Declared in "
        + StringUtil.escapeXmlEntities(element.getContainingFile().getName())
        + DocumentationMarkup.CONTENT_END;
  }

  @Override
  public @Nullable String getQuickNavigateInfo(
      @NotNull PsiElement element, @NotNull PsiElement originalElement) {
    Declaration declaration = DeclarationScanner.findDeclaration(element);
    return declaration == null
        ? null
        : declaration.getKind().getDisplayName() + " " + declaration.getName();
  }

  @Override
  public @Nullable String generateHoverDoc(
      @NotNull PsiElement element, @Nullable PsiElement originalElement) {
    return generateDoc(element, originalElement);
  }
}
