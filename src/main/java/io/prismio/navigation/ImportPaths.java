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
    List<PsiElement> path = importPathAround(identifier);
    if (path.isEmpty()) {
      return null;
    }

    StringBuilder relative = new StringBuilder();
    for (PsiElement segment : path) {
      if (relative.length() > 0) {
        relative.append('/');
      }
      relative.append(segment.getText());
    }
    relative.append(".psm");

    return findByRelativePath(identifier, relative.toString());
  }

  /**
   * The dotted segments of the import statement containing {@code identifier},
   * or an empty list when it is not in one.
   */
  private static List<PsiElement> importPathAround(PsiElement identifier) {
    // Walk back over `name.name.name` to the `import` that introduces it.
    PsiElement cursor = previousSignificant(identifier);
    int expectedDots = 0;
    while (cursor != null) {
      IElementTypeHolder holder = new IElementTypeHolder(cursor);
      if (holder.isDot()) {
        expectedDots++;
        cursor = previousSignificant(cursor);
        continue;
      }
      if (holder.isIdentifier() && expectedDots > 0) {
        expectedDots--;
        cursor = previousSignificant(cursor);
        continue;
      }
      break;
    }
    if (cursor == null || !"import".equals(cursor.getText())
        || cursor.getNode().getElementType() != PrismioTypes.KEYWORD) {
      return List.of();
    }

    // Then forward, collecting the whole dotted name.
    List<PsiElement> segments = new ArrayList<>();
    PsiElement forward = nextSignificant(cursor);
    while (forward != null) {
      IElementTypeHolder holder = new IElementTypeHolder(forward);
      if (holder.isIdentifier()) {
        segments.add(forward);
      } else if (!holder.isDot()) {
        break;
      }
      forward = nextSignificant(forward);
    }
    return segments;
  }

  private static @Nullable PsiFile findByRelativePath(PsiElement context, String relative) {
    PsiManager manager = PsiManager.getInstance(context.getProject());
    String suffix = "/" + relative;

    for (VirtualFile virtualFile : FileTypeIndex.getFiles(
        PrismioFileType.INSTANCE, GlobalSearchScope.allScope(context.getProject()))) {
      String path = virtualFile.getPath();
      if (path.endsWith(suffix) || path.equals(relative)) {
        PsiFile file = manager.findFile(virtualFile);
        if (file != null) {
          return file;
        }
      }
    }
    return null;
  }

  private static @Nullable PsiElement previousSignificant(PsiElement element) {
    PsiElement previous = element.getPrevSibling();
    while (previous != null && isIgnorable(previous)) {
      previous = previous.getPrevSibling();
    }
    return previous;
  }

  private static @Nullable PsiElement nextSignificant(PsiElement element) {
    PsiElement next = element.getNextSibling();
    while (next != null && isIgnorable(next)) {
      next = next.getNextSibling();
    }
    return next;
  }

  private static boolean isIgnorable(PsiElement element) {
    return element.getNode() == null
        || element.getNode().getElementType() == com.intellij.psi.TokenType.WHITE_SPACE
        || io.prismio.psi.PrismioTokenSets.COMMENTS.contains(element.getNode().getElementType());
  }

  /** A tiny reader so the walkers above read as intent rather than as node poking. */
  private record IElementTypeHolder(PsiElement element) {
    boolean isDot() {
      return element.getNode() != null
          && element.getNode().getElementType() == PrismioTypes.DOT;
    }

    boolean isIdentifier() {
      return element.getNode() != null
          && element.getNode().getElementType() == PrismioTypes.IDENTIFIER;
    }
  }
}
