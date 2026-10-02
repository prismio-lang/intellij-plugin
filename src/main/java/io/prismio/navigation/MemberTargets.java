package io.prismio.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.prismio.psi.PrismioTypes;
import io.prismio.symbols.FnSig;
import io.prismio.symbols.Member;
import io.prismio.symbols.ModuleSummary;
import io.prismio.symbols.ProjectSymbols;
import io.prismio.symbols.StdlibIndex;
import io.prismio.symbols.SymbolTable;
import io.prismio.symbols.TypeInference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Where {@code receiver.name} is declared, by the receiver's type.
 *
 * <p>{@code xs.contains(1)} on a {@code Vec<Int>} is the {@code contains} in
 * {@code std.vec}, not every {@code contains} in the project and the library.
 * The type comes from {@link TypeInference}, which answers only when the source
 * makes it certain, so a receiver it cannot type yields nothing here and the
 * caller falls back to listing every declaration of the name.
 */
final class MemberTargets {

  private MemberTargets() {}

  /** The declarations {@code name} can mean after this dot; empty when the receiver's type is unknown. */
  static @NotNull List<PsiElement> resolve(@NotNull PsiElement name) {
    PsiElement before = name.getPrevSibling();
    while (before != null && before.getNode() != null
        && before.getNode().getElementType() == com.intellij.psi.TokenType.WHITE_SPACE) {
      before = before.getPrevSibling();
    }
    if (before == null || before.getNode() == null || before.getNode().getElementType() != PrismioTypes.DOT) {
      return List.of();
    }
    PsiFile file = name.getContainingFile().getOriginalFile();
    CharSequence text = file.getViewProvider().getContents();
    SymbolTable.Scope scope = ProjectSymbols.scope(file);
    List<ModuleSummary.Import> imports = ProjectSymbols.summary(file).imports();
    TypeInference.Receiver receiver =
        TypeInference.receiverAt(text, name.getTextRange().getStartOffset(), scope, imports);
    if (receiver == null) {
      return List.of();
    }

    List<FnSig> candidates = new ArrayList<>();
    String wanted = name.getText();
    if (receiver.module() != null) {
      ModuleSummary module = StdlibIndex.getInstance(file.getProject()).get().modules().get(receiver.module());
      if (module != null) {
        for (FnSig fn : module.functions()) {
          if (fn.owner() == null && fn.name().equals(wanted)) {
            candidates.add(fn);
          }
        }
      }
    } else if (receiver.type() != null) {
      List<Member> members = receiver.isStatic()
          ? scope.staticMembers(receiver.type())
          : scope.instanceMembers(receiver.type());
      for (Member member : members) {
            if (member.name().equals(wanted) && member.function() != null) {
          if (!member.function().module().isEmpty()) {
            candidates.add(member.function());
          } else if (!receiver.isStatic()) {
            // The compiler lowers this member itself, so it has no declaration; the library
            // function it calls (an array's `contains` is std's `contains(items: [T], ...)`) does.
            for (FnSig fn : scope.functions(wanted)) {
              FnSig.Param first = fn.params().isEmpty() ? null : fn.params().get(0);
              if (first != null && first.type() != null
                  && first.type().memberKey().equals(receiver.type().memberKey())) {
                candidates.add(fn);
              }
            }
          }
        }
      }
    }

    Set<PsiElement> targets = new LinkedHashSet<>();
    for (FnSig fn : candidates) {
      PsiElement target = declarationOf(file, fn);
      if (target != null) {
        targets.add(target);
      }
    }
    return new ArrayList<>(targets);
  }

  private static @Nullable PsiElement declarationOf(@NotNull PsiFile current, @NotNull FnSig fn) {
    PsiFile file = fn.module().equals(ProjectSymbols.moduleName(current)) ? current : moduleFile(current.getProject(), fn.module());
    if (file == null) {
      return null;
    }
    PsiElement at = file.findElementAt(fn.offset());
    return at != null && at.getText().equals(fn.name()) ? at : file;
  }

  /** The file of a dotted module: the toolchain's own for {@code std.*}, else by path under the project. */
  static @Nullable PsiFile moduleFile(@NotNull Project project, @NotNull String module) {
    PsiManager manager = PsiManager.getInstance(project);
    if (module.startsWith("std.")) {
      Path directory = StdlibIndex.getInstance(project).get().directory();
      if (directory != null) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByNioFile(
            directory.resolve(module.substring("std.".length()).replace('.', '/') + ".psm"));
        PsiFile psi = vf == null ? null : manager.findFile(vf);
        if (psi != null) {
          return psi;
        }
      }
    }
    return ImportPaths.findModule(project, module.replace('.', '/') + ".psm");
  }
}
