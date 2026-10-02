package io.prismio.navigation;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.prismio.PrismioFileType;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * `import a.b` as a file to open.
 *
 * <p>The compiler turns a dotted module name into a path — `ir.expr` becomes
 * `ir/expr.psm`, and `std.io` is looked for beside the entry, one level up, and
 * finally in the installed `stdlib`. An editor cannot know which of those a
 * given project uses, so this asks a question it can answer: which indexed file
 * has a path ending in that relative path. In a checkout that is exactly the
 * file the compiler would read.
 */
public final class ImportPaths {

  private ImportPaths() {}

  /**
   * The module the identifier under the caret names, if it sits in an import.
   *
   * <p>Any segment resolves to the whole module: clicking `std` in `import
   * std.io` opens `std/io.psm`, because that is the only file the statement
   * names and a directory is not a navigation target.
   */
  public static @Nullable PsiFile resolveModuleAt(@NotNull PsiElement identifier) {
    List<String> path = importSegmentsAround(identifier);
    if (path.isEmpty()) {
      return null;
    }

    StringBuilder relative = new StringBuilder();
    for (String segment : path) {
      if (relative.length() > 0) {
        relative.append('/');
      }
      relative.append(segment);
    }
    relative.append(".psm");

    return findModule(identifier.getProject(), relative.toString());
  }

  /** The indexed file whose path ends in {@code relative}, e.g. {@code project/host.psm}. */
  static @Nullable PsiFile findModule(@NotNull com.intellij.openapi.project.Project project, @NotNull String relative) {
    return findByRelativePath(project, relative);
  }

  /**
   * The dotted segments of the import statement containing {@code identifier},
   * or an empty list when it is not in one. Supports hyphenated segment names.
   */
  private static List<String> importSegmentsAround(PsiElement identifier) {
    // Walk back over `name.name-hyphen.name` to the `import` on the same line
    PsiElement cursor = prevOnSameLine(identifier);
    while (cursor != null) {
      if (isImportKeyword(cursor)) {
        break;
      }
      if (isDot(cursor) || isSegmentToken(cursor)) {
        cursor = prevOnSameLine(cursor);
        continue;
      }
      break;
    }
    if (cursor == null || !isImportKeyword(cursor)) {
      return List.of();
    }

    // Then forward on the same line, collecting the dotted and hyphenated name
    List<String> segments = new ArrayList<>();
    StringBuilder currentSegment = new StringBuilder();
    PsiElement forward = nextOnSameLine(cursor);
    while (forward != null) {
      if (isDot(forward)) {
        if (currentSegment.length() > 0) {
          segments.add(currentSegment.toString());
          currentSegment.setLength(0);
        }
      } else if (isSegmentToken(forward)) {
        currentSegment.append(forward.getText());
      } else {
        break;
      }
      forward = nextOnSameLine(forward);
    }
    if (currentSegment.length() > 0) {
      segments.add(currentSegment.toString());
    }
    return segments;
  }

  private static @Nullable PsiFile findByRelativePath(com.intellij.openapi.project.Project project, String relative) {
    PsiManager manager = PsiManager.getInstance(project);
    String suffix = "/" + relative;

    for (VirtualFile virtualFile : FileTypeIndex.getFiles(
        PrismioFileType.INSTANCE, GlobalSearchScope.allScope(project))) {
      String path = virtualFile.getPath();
      if (path.endsWith(suffix) || path.equals(relative) || virtualFile.getName().equals(relative)) {
        PsiFile file = manager.findFile(virtualFile);
        if (file != null) {
          return file;
        }
      }
    }
    return null;
  }

  private static @Nullable PsiElement prevOnSameLine(PsiElement element) {
    PsiElement prev = element.getPrevSibling();
    while (prev != null && isWhitespaceOnSameLine(prev)) {
      prev = prev.getPrevSibling();
    }
    return prev;
  }

  private static @Nullable PsiElement nextOnSameLine(PsiElement element) {
    PsiElement next = element.getNextSibling();
    while (next != null && isWhitespaceOnSameLine(next)) {
      next = next.getNextSibling();
    }
    return next;
  }

  private static boolean isWhitespaceOnSameLine(PsiElement element) {
    return element.getNode() != null
        && element.getNode().getElementType() == com.intellij.psi.TokenType.WHITE_SPACE
        && !element.getText().contains("\n");
  }

  private static boolean isImportKeyword(PsiElement element) {
    return element != null
        && element.getNode() != null
        && element.getNode().getElementType() == PrismioTypes.KEYWORD
        && "import".equals(element.getText());
  }

  private static boolean isDot(PsiElement element) {
    return element != null
        && element.getNode() != null
        && element.getNode().getElementType() == PrismioTypes.DOT;
  }

  private static boolean isSegmentToken(PsiElement element) {
    if (element == null || element.getNode() == null) {
      return false;
    }
    var type = element.getNode().getElementType();
    return type == PrismioTypes.IDENTIFIER
        || (type == PrismioTypes.ARITHMETIC_OP && "-".equals(element.getText()));
  }
}
