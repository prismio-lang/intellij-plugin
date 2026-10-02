package io.prismio.imports;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.prismio.symbols.FnSig;
import io.prismio.symbols.ModuleSummary;
import io.prismio.symbols.StdlibIndex;
import io.prismio.symbols.TypeDecl;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Alt+Enter on a name the compiler could not find: import the standard-library
 * module that declares it.
 *
 * <p>The candidates come from the toolchain's library, not from a list here, so
 * the fix offers whichever module declares the name in the compiler being used.
 * A compiler message that already names the import — {@code a vector literal
 * needs `import std.vec`} — is taken at its word.
 */
public final class AddImportFix implements IntentionAction, HighPriorityAction {

  private static final Pattern NEEDS = Pattern.compile("needs `import ([A-Za-z0-9_.]+)`");
  private static final Pattern UNKNOWN =
      Pattern.compile("unknown (function|identifier|type|name) `([A-Za-z_][A-Za-z0-9_]*)`");

  private final String module;
  private final String name;

  public AddImportFix(@NotNull String module, @NotNull String name) {
    this.module = module;
    this.name = name;
  }

  /** The fixes for one compiler message: one per module that could satisfy it. */
  public static @NotNull List<AddImportFix> forMessage(@NotNull Project project, @NotNull String message) {
    Matcher needs = NEEDS.matcher(message);
    if (needs.find()) {
      return List.of(new AddImportFix(needs.group(1), ""));
    }
    Matcher unknown = UNKNOWN.matcher(message);
    if (!unknown.find()) {
      return List.of();
    }
    String name = unknown.group(2);
    StdlibIndex.Snapshot library = StdlibIndex.getInstance(project).get();
    Set<String> modules = new TreeSet<>();
    for (FnSig fn : library.freeFunctions().getOrDefault(name, List.of())) {
      modules.add(fn.module());
    }
    TypeDecl type = library.types().get(name);
    if (type != null) {
      modules.add(type.module());
    }
    ModuleSummary.Global global = library.globals().get(name);
    if (global != null) {
      modules.add(global.module());
    }
    return modules.stream().map(module -> new AddImportFix(module, name)).toList();
  }

  @Override
  public @NotNull String getText() {
    return name.isEmpty() ? "Import " + module : "Import " + module + " (for " + name + ")";
  }

  @Override
  public @NotNull String getFamilyName() {
    return "Import standard library module";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return file != null && !PrismioImports.isImported(
        PrismioImports.importsOf(file.getViewProvider().getContents()), module);
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
    Document document = PsiDocumentManager.getInstance(project).getDocument(file);
    if (document == null) {
      return;
    }
    PrismioImports.addImport(document, module);
    PsiDocumentManager.getInstance(project).commitDocument(document);
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
