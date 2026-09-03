package io.prismio.ums;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import io.prismio.icons.PrismioIcons;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The blocks of a manifest, as a tree.
 *
 * <p>Built by matching braces over the flat token stream. A block's label is the
 * identifier that opened it plus its first string argument, because
 * {@code executable} on its own says nothing and {@code executable("prismio")}
 * is the thing the reader is looking for.
 */
public final class UmsStructureViewFactory implements PsiStructureViewFactory {

  @Override
  public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
    if (!(psiFile instanceof UmsFile)) {
      return null;
    }
    return new TreeBasedStructureViewBuilder() {
      @Override
      public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new Model(psiFile);
      }
    };
  }

  private static final class Model extends com.intellij.ide.structureView.TextEditorBasedStructureViewModel {
    private final PsiFile file;

    Model(PsiFile file) {
      super(file);
      this.file = file;
    }

    @Override
    public @NotNull StructureViewTreeElement getRoot() {
      return new Node(file, "manifest", 0, file.getTextLength());
    }

    @Override
    protected @NotNull PsiFile getPsiFile() {
      return file;
    }
  }

  /** One block. Children are the blocks nested directly inside it. */
  private static final class Node implements StructureViewTreeElement, SortableTreeElement {
    private final PsiFile file;
    private final String label;
    private final int bodyStart;
    private final int bodyEnd;

    Node(PsiFile file, String label, int bodyStart, int bodyEnd) {
      this.file = file;
      this.label = label;
      this.bodyStart = bodyStart;
      this.bodyEnd = bodyEnd;
    }

    @Override
    public Object getValue() {
      PsiElement at = file.findElementAt(bodyStart);
      return at != null ? at : file;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
      return new ItemPresentation() {
        @Override
        public @Nullable String getPresentableText() {
          return label;
        }

        @Override
        public @Nullable Icon getIcon(boolean unused) {
          return PrismioIcons.FILE;
        }
      };
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
      List<Entry> tokens = significantTokens(file);
      List<TreeElement> children = new ArrayList<>();

      int depth = 0;
      int index = 0;
      while (index < tokens.size()) {
        Entry token = tokens.get(index);
        if (token.start < bodyStart || token.start >= bodyEnd) {
          index++;
          continue;
        }
        if (UmsTypes.LEFT_BRACE.equals(token.type)) {
          if (depth == 0) {
            int close = matchingBrace(tokens, index);
            String name = labelFor(tokens, index);
            if (name != null) {
              children.add(new Node(file, name, token.start + 1,
                  close < tokens.size() ? tokens.get(close).start : bodyEnd));
            }
          }
          depth++;
        } else if (UmsTypes.RIGHT_BRACE.equals(token.type)) {
          depth = Math.max(0, depth - 1);
        }
        index++;
      }
      return children.toArray(TreeElement.EMPTY_ARRAY);
    }

    @Override
    public void navigate(boolean requestFocus) {
      PsiElement at = file.findElementAt(bodyStart);
      if (at instanceof NavigatablePsiElement navigatable) {
        navigatable.navigate(requestFocus);
      }
    }

    @Override
    public boolean canNavigate() {
      return true;
    }

    @Override
    public boolean canNavigateToSource() {
      return true;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
      return label;
    }
  }

  /** `executable("prismio")` rather than a bare `executable`. */
  private static @Nullable String labelFor(List<Entry> tokens, int braceIndex) {
    int i = braceIndex - 1;
    String argument = null;
    if (i >= 0 && UmsTypes.RIGHT_PAREN.equals(tokens.get(i).type)) {
      int depth = 0;
      int scan = i;
      while (scan >= 0) {
        IElementType type = tokens.get(scan).type;
        if (UmsTypes.RIGHT_PAREN.equals(type)) {
          depth++;
        } else if (UmsTypes.LEFT_PAREN.equals(type)) {
          depth--;
          if (depth == 0) {
            break;
          }
        } else if (depth == 1 && argument == null && UmsTypes.STRING.equals(type)) {
          argument = tokens.get(scan).text;
        }
        scan--;
      }
      i = scan - 1;
    }
    if (i < 0 || !UmsTypes.IDENTIFIER.equals(tokens.get(i).type)) {
      return null;
    }
    String name = tokens.get(i).text;
    return argument == null ? name : name + "(" + argument + ")";
  }

  private static int matchingBrace(List<Entry> tokens, int openIndex) {
    int depth = 0;
    for (int i = openIndex; i < tokens.size(); i++) {
      IElementType type = tokens.get(i).type;
      if (UmsTypes.LEFT_BRACE.equals(type)) {
        depth++;
      } else if (UmsTypes.RIGHT_BRACE.equals(type)) {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }
    return tokens.size();
  }

  private static List<Entry> significantTokens(PsiFile file) {
    List<Entry> out = new ArrayList<>();
    for (ASTNode node : file.getNode().getChildren(null)) {
      IElementType type = node.getElementType();
      if (UmsTypes.COMMENTS.contains(type) || com.intellij.psi.TokenType.WHITE_SPACE.equals(type)) {
        continue;
      }
      out.add(new Entry(type, node.getText(), node.getStartOffset()));
    }
    return out;
  }

  private record Entry(IElementType type, String text, int start) {}
}
