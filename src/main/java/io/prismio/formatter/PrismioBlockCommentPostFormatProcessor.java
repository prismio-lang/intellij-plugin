package io.prismio.formatter;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Post-format processor that formats the interior lines of multiline block comments
 * to have a 3-space indentation relative to the comment's base indent, matching
 * Prismio's comment style ({@code COMMENT_CONTENT_INDENT = "   "}).
 */
public class PrismioBlockCommentPostFormatProcessor implements PostFormatProcessor {
  private static final String COMMENT_CONTENT_INDENT = "   "; // 3 spaces

  @Override
  public @NotNull PsiElement processElement(
      @NotNull PsiElement source, @NotNull CodeStyleSettings settings) {
    if (source.getContainingFile() instanceof PrismioFile) {
      processText(source.getContainingFile(), source.getTextRange(), settings);
    }
    return source;
  }

  @Override
  public @NotNull TextRange processText(
      @NotNull PsiFile source,
      @NotNull TextRange rangeToReformat,
      @NotNull CodeStyleSettings settings) {
    if (!(source instanceof PrismioFile)) {
      return rangeToReformat;
    }

    Document document = PsiDocumentManager.getInstance(source.getProject()).getDocument(source);
    if (document == null) {
      return rangeToReformat;
    }

    PsiDocumentManager.getInstance(source.getProject()).commitDocument(document);

    List<PsiElement> blockComments = new ArrayList<>();
    collectBlockComments(source, rangeToReformat, blockComments);
    if (blockComments.isEmpty()) {
      return rangeToReformat;
    }

    // Process comments in reverse order so character offsets in earlier parts of the document remain valid
    for (int i = blockComments.size() - 1; i >= 0; i--) {
      PsiElement comment = blockComments.get(i);
      TextRange commentRange = comment.getTextRange();
      String originalText = comment.getText();

      // Only process multiline block comments starting with /*
      if (!originalText.startsWith("/*") || !originalText.contains("\n")) {
        continue;
      }

      int startOffset = commentRange.getStartOffset();
      int startLine = document.getLineNumber(startOffset);
      int lineStartOffset = document.getLineStartOffset(startLine);
      String baseIndent =
          getIndentation(document.getText(new TextRange(lineStartOffset, startOffset)));

      String formattedComment = formatBlockComment(originalText, baseIndent);
      if (!formattedComment.equals(originalText)) {
        document.replaceString(
            commentRange.getStartOffset(), commentRange.getEndOffset(), formattedComment);
      }
    }

    PsiDocumentManager.getInstance(source.getProject()).commitDocument(document);
    return rangeToReformat;
  }

  private static void collectBlockComments(
      @NotNull PsiElement root, @NotNull TextRange range, @NotNull List<PsiElement> result) {
    for (PsiElement child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getTextRange().intersects(range)) {
        if (child.getNode() != null
            && (child.getNode().getElementType() == PrismioTypes.BLOCK_COMMENT
                || child.getNode().getElementType() == PrismioTypes.DOC_COMMENT)) {
          result.add(child);
        } else if (child.getChildren().length > 0) {
          collectBlockComments(child, range, result);
        }
      }
    }
  }

  public static String formatBlockComment(@NotNull String text, @NotNull String baseIndent) {
    String[] lines = text.split("\n", -1);
    if (lines.length <= 1) {
      return text;
    }
    // Only format multiline block comments where /* (or /** of a documentation comment, which the
    // compiler reads as the same comment) is alone on the first line and not enclosing nested blocks
    String opener = lines[0].trim();
    if (!(opener.equals("/*") || opener.equals("/**")) || text.substring(2).contains("/*")) {
      return text;
    }

    StringBuilder result = new StringBuilder();
    result.append(lines[0]); // First line contains /*

    if (isStarred(lines)) {
      // A leading star per line, as in a documentation comment: the stars line up one column
      // in from the opener, under its own `*`, and the closer follows them.
      String starIndent = baseIndent + " ";
      for (int i = 1; i < lines.length; i++) {
        result.append("\n");
        String trimmed = lines[i].trim();
        if (i == lines.length - 1) {
          result.append(starIndent).append("*/");
        } else if (!trimmed.isEmpty()) {
          result.append(starIndent).append(trimmed);
        }
      }
      return result.toString();
    }

    String contentIndent = baseIndent + COMMENT_CONTENT_INDENT;

    for (int i = 1; i < lines.length; i++) {
      result.append("\n");
      String line = lines[i];
      String trimmed = line.trim();

      if (i == lines.length - 1 && trimmed.equals("*/")) {
        result.append(baseIndent).append("*/");
      } else if (trimmed.isEmpty()) {
        // Keep blank lines empty
      } else {
        int leadingSpaces = countLeadingSpaces(line);
        int extraIndent = Math.max(0, leadingSpaces - contentIndent.length());
        result.append(contentIndent);
        if (extraIndent > 0 && leadingSpaces > baseIndent.length()) {
          result.append(" ".repeat(extraIndent));
        }
        result.append(trimmed);
      }
    }

    return result.toString();
  }

  /** Every line after the opener starts with {@code *}, and the last is the bare closer. */
  private static boolean isStarred(String[] lines) {
    if (!lines[lines.length - 1].trim().equals("*/")) {
      return false;
    }
    boolean any = false;
    for (int i = 1; i < lines.length - 1; i++) {
      String trimmed = lines[i].trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      if (!trimmed.startsWith("*")) {
        return false;
      }
      any = true;
    }
    return any;
  }

  private static String getIndentation(String prefix) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < prefix.length(); i++) {
      char c = prefix.charAt(i);
      if (c == ' ' || c == '\t') {
        sb.append(c);
      } else {
        break;
      }
    }
    return sb.toString();
  }

  private static int countLeadingSpaces(String line) {
    int count = 0;
    while (count < line.length() && (line.charAt(count) == ' ' || line.charAt(count) == '\t')) {
      count++;
    }
    return count;
  }
}
