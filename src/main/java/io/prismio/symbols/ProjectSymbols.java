package io.prismio.symbols;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import io.prismio.PrismioFileType;
import io.prismio.toolchain.PrismioToolchainService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The symbols in reach of a file: its own declarations, the project's other
 * files, the members the compiler lowers itself, and the toolchain's standard
 * library — asked in that order, so a name the program declares wins.
 */
public final class ProjectSymbols {

  private static final Key<CachedValue<ModuleSummary>> SUMMARY = Key.create("prismio.summary");
  private static final Key<CachedValue<SymbolTable>> PROJECT_TABLE = Key.create("prismio.projectTable");

  private ProjectSymbols() {}

  /** The file's declarations, re-read only when its text changes. */
  public static @NotNull ModuleSummary summary(@NotNull PsiFile file) {
    return CachedValuesManager.getCachedValue(file, SUMMARY, () -> CachedValueProvider.Result.create(
        SignatureParser.parse(file.getViewProvider().getContents(), moduleName(file)), file));
  }

  public static @NotNull SymbolTable.Scope scope(@NotNull PsiFile file) {
    PsiFile original = file.getOriginalFile();
    Project project = original.getProject();
    SymbolTable own = new SymbolTable();
    own.add(summary(original), SymbolTable.ALL);
    List<SymbolTable> tables = new ArrayList<>();
    tables.add(own);
    tables.add(projectTable(project));
    tables.add(CompilerIntrinsics.table(PrismioToolchainService.getInstance(project).getCompilerExecutable()));
    tables.add(StdlibIndex.getInstance(project).get().table());
    return new SymbolTable.Scope(tables);
  }

  /**
   * Every other Prismio file in the project, as one table. Rebuilt after any PSI
   * change; each file's summary is cached on its own, so a rebuild is a merge
   * rather than a re-read.
   */
  private static @NotNull SymbolTable projectTable(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, PROJECT_TABLE, () -> {
      SymbolTable table = new SymbolTable();
      Path stdlib = StdlibIndex.getInstance(project).get().directory();
      String stdlibPath = stdlib == null ? null : stdlib.toString();
      PsiManager manager = PsiManager.getInstance(project);
      for (VirtualFile vf : FileTypeIndex.getFiles(PrismioFileType.INSTANCE,
          GlobalSearchScope.projectScope(project))) {
        String path = vf.getPath();
        // Build output and the library itself: the standard library is the
        // toolchain's, read by StdlibIndex, even when the project is its checkout.
        if (path.contains("/.prismio/") || path.contains("/build/")
            || stdlibPath != null && path.startsWith(stdlibPath)) {
          continue;
        }
        PsiFile psi = manager.findFile(vf);
        if (psi != null) {
          table.add(summary(psi), SymbolTable.PACKAGE);
        }
      }
      return CachedValueProvider.Result.create(table, PsiModificationTracker.MODIFICATION_COUNT);
    }, false);
  }

  /** A project file's module name as an importer from its source root writes it: {@code parse.stmt}. */
  public static @NotNull String moduleName(@NotNull PsiFile file) {
    VirtualFile vf = file.getOriginalFile().getVirtualFile();
    if (vf == null) {
      String name = file.getName();
      return name.endsWith(".psm") ? name.substring(0, name.length() - 4) : name;
    }
    String dotted = dottedName(vf, file.getProject());
    return dotted == null ? vf.getNameWithoutExtension() : dotted;
  }

  static @Nullable String dottedName(@NotNull VirtualFile vf, @NotNull Project project) {
    String path = vf.getPath();
    for (VirtualFile root : ProjectRootManager.getInstance(project).getContentRoots()) {
      String rootPath = root.getPath();
      if (path.startsWith(rootPath + "/")) {
        String rel = path.substring(rootPath.length() + 1);
        if (rel.startsWith("src/")) {
          rel = rel.substring(4);
        }
        if (rel.endsWith(".psm")) {
          rel = rel.substring(0, rel.length() - 4);
        }
        return rel.replace('/', '.');
      }
    }
    return null;
  }
}
