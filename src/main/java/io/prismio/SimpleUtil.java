package io.prismio;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioProperty;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimpleUtil {

  /**
   * Searches the entire project for Prismio language files with properties matching the given key.
   *
   * @param project current project
   * @param key     key to match
   * @return matching properties
   */
  public static List<PrismioProperty> findProperties(Project project, String key) {
    List<PrismioProperty> result = new ArrayList<>();
    Collection<VirtualFile> virtualFiles =
        FileTypeIndex.getFiles(PsFileType.INSTANCE, GlobalSearchScope.allScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      PrismioFile simpleFile = (PrismioFile) PsiManager.getInstance(project).findFile(virtualFile);
      if (simpleFile != null) {
        PrismioProperty[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, PrismioProperty.class);
        if (properties != null) {
          for (PrismioProperty property : properties) {
            if (key.equals(property.getKey())) {
              result.add(property);
            }
          }
        }
      }
    }
    return result;
  }

  public static List<PrismioProperty> findProperties(Project project) {
    List<PrismioProperty> result = new ArrayList<>();
    Collection<VirtualFile> virtualFiles =
        FileTypeIndex.getFiles(PsFileType.INSTANCE, GlobalSearchScope.allScope(project));
    for (VirtualFile virtualFile : virtualFiles) {
      PrismioFile simpleFile = (PrismioFile) PsiManager.getInstance(project).findFile(virtualFile);
      if (simpleFile != null) {
        PrismioProperty[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, PrismioProperty.class);
        if (properties != null) {
          Collections.addAll(result, properties);
        }
      }
    }
    return result;
  }

  /**
   * Attempts to collect any comment elements above the Prismio property.
   * Uses addFirst() on a LinkedList to accumulate comments in reading order without Guava.
   */
  public static @NotNull String findDocumentationComment(PrismioProperty property) {
    LinkedList<String> result = new LinkedList<>();
    PsiElement element = property.getPrevSibling();
    while (element instanceof PsiComment || element instanceof PsiWhiteSpace) {
      if (element instanceof PsiComment) {
        String commentText = element.getText().replaceFirst("[//]+", "");
        result.addFirst(commentText);
      }
      element = element.getPrevSibling();
    }
    return StringUtil.join(result, "\n ");
  }

}
