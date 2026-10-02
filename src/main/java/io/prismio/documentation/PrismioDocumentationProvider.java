package io.prismio.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.prismio.navigation.Declaration;
import io.prismio.navigation.DeclarationScanner;
import io.prismio.navigation.PrismioScopeResolver;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides rich documentation, signatures, and doc comments for Prismio declarations. */
@SuppressWarnings("deprecation")
public final class PrismioDocumentationProvider extends AbstractDocumentationProvider {

  @Override
  public @Nullable String generateDoc(
      @NotNull PsiElement element, @Nullable PsiElement originalElement) {
    PsiElement target = resolveTarget(element);
    if (target == null) {
      return null;
    }

    String signature = extractSignature(target);
    String docComment = extractDocComment(target);
    PsiFile file = target.getContainingFile();
    String fileName = file != null ? file.getName() : "unknown";

    StringBuilder html = new StringBuilder();
    html.append(DocumentationMarkup.DEFINITION_START);
    if (signature != null && !signature.isBlank()) {
      html.append("<pre>").append(StringUtil.escapeXmlEntities(signature)).append("</pre>");
    } else {
      html.append("<b>").append(StringUtil.escapeXmlEntities(target.getText())).append("</b>");
    }
    html.append(DocumentationMarkup.DEFINITION_END);

    html.append(DocumentationMarkup.CONTENT_START);
    if (docComment != null && !docComment.isBlank()) {
      html.append("<p>").append(formatDocComment(docComment)).append("</p>");
    }
    html.append("<p style='color: gray; font-size: small;'>Declared in ")
        .append(StringUtil.escapeXmlEntities(fileName))
        .append("</p>");
    html.append(DocumentationMarkup.CONTENT_END);

    return html.toString();
  }

  @Override
  public @Nullable String getQuickNavigateInfo(
      @NotNull PsiElement element, @Nullable PsiElement originalElement) {
    PsiElement target = resolveTarget(element);
    if (target == null) {
      return null;
    }
    String signature = extractSignature(target);
    return signature != null ? signature : target.getText();
  }

  @Override
  public @Nullable String generateHoverDoc(
      @NotNull PsiElement element, @Nullable PsiElement originalElement) {
    return generateDoc(element, originalElement);
  }

  private static @Nullable PsiElement resolveTarget(@NotNull PsiElement element) {
    if (PrismioScopeResolver.isDeclarationSite(element)) {
      return element;
    }
    Declaration decl = DeclarationScanner.findDeclaration(element);
    if (decl != null && decl.getElement() != null) {
      return decl.getElement();
    }
    PsiElement lexical = PrismioScopeResolver.resolveLexical(element);
    if (lexical != null) {
      return lexical;
    }
    return null;
  }

  private static @Nullable String extractSignature(@NotNull PsiElement target) {
    PsiFile file = target.getContainingFile();
    if (file == null) return null;
    CharSequence text = file.getViewProvider().getContents();

    // Walk backwards to the keyword or statement start
    int start = target.getTextOffset();
    while (start > 0 && text.charAt(start - 1) != '\n' && text.charAt(start - 1) != '{' && text.charAt(start - 1) != '}') {
      start--;
    }
    while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
      start++;
    }

    // Walk forward to '{' or newline
    int end = target.getTextOffset();
    while (end < text.length()) {
      char c = text.charAt(end);
      if (c == '{' || c == '\n') {
        break;
      }
      end++;
    }

    if (start < end) {
      return text.subSequence(start, end).toString().trim();
    }
    return null;
  }

  private static @Nullable String extractDocComment(@NotNull PsiElement target) {
    PsiFile file = target.getContainingFile();
    if (file == null) return null;
    CharSequence text = file.getViewProvider().getContents();

    int start = target.getTextOffset();
    while (start > 0 && text.charAt(start - 1) != '\n') {
      start--;
    }

    // Check lines above
    List<String> commentLines = new ArrayList<>();
    int lineEnd = start - 1;
    while (lineEnd > 0) {
      int lineStart = lineEnd;
      while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
        lineStart--;
      }
      String line = text.subSequence(lineStart, lineEnd).toString().trim();
      if (line.startsWith("///")) {
        commentLines.add(0, line.substring(3).trim());
      } else if (line.endsWith("*/")) {
        // block comment
        int blockStart = text.toString().lastIndexOf("/**", lineEnd);
        if (blockStart >= 0) {
          String block = text.subSequence(blockStart, lineEnd).toString();
          return cleanBlockComment(block);
        }
        break;
      } else if (line.isEmpty()) {
        break;
      } else {
        break;
      }
      lineEnd = lineStart - 1;
    }

    if (!commentLines.isEmpty()) {
      return String.join("\n", commentLines);
    }
    return null;
  }

  private static String cleanBlockComment(String raw) {
    String clean = raw.replaceAll("^/\\*\\*?", "").replaceAll("\\*/$", "");
    StringBuilder sb = new StringBuilder();
    for (String line : clean.split("\\R")) {
      String trimmed = line.trim().replaceAll("^\\*\\s?", "");
      if (!trimmed.isEmpty()) {
        sb.append(trimmed).append("\n");
      }
    }
    return sb.toString().trim();
  }

  private static String formatDocComment(String comment) {
    String escaped = StringUtil.escapeXmlEntities(comment);
    return escaped.replace("\n\n", "</p><p>").replace("\n", "<br/>");
  }
}
