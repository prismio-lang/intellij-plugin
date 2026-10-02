package io.prismio.imports;

import com.intellij.openapi.editor.Document;
import io.prismio.symbols.ModuleSummary;
import io.prismio.symbols.SignatureParser;
import io.prismio.symbols.Tok;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Which modules a file imports, and adding one it does not.
 *
 * <p>Every shape {@code parseImportStatement} in {@code src/parse/parser.psm}
 * accepts counts: {@code import std.io}, {@code import std.io as out},
 * {@code import std.*}, {@code import {io, string} from std} and
 * {@code import * from std}. {@code import std.string.trim} names one
 * declaration, so it does not make the rest of {@code std.string} available.
 */
public final class PrismioImports {

  private PrismioImports() {}

  public static @NotNull List<ModuleSummary.Import> importsOf(@NotNull CharSequence text) {
    return SignatureParser.parse(text, "").imports();
  }

  public static boolean isImported(@NotNull List<ModuleSummary.Import> imports, @NotNull String module) {
    for (ModuleSummary.Import anImport : imports) {
      if (!anImport.wildcard() && anImport.path().equals(module)) {
        return true;
      }
      if (anImport.wildcard() && module.startsWith(anImport.path() + ".")
          && module.indexOf('.', anImport.path().length() + 1) < 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds {@code import module} unless the file already has it. Goes into the
   * import block in alphabetical order when the block is sorted, else after its
   * last line; a file with no imports gets one below its opening comment, which
   * in this codebase is the module's description.
   */
  public static void addImport(@NotNull Document document, @NotNull String module) {
    CharSequence text = document.getCharsSequence();
    List<ModuleSummary.Import> imports = importsOf(text);
    if (isImported(imports, module)) {
      return;
    }
    String line = "import " + module;
    if (!imports.isEmpty()) {
      boolean sorted = true;
      for (int k = 1; k < imports.size(); k++) {
        if (imports.get(k - 1).path().compareTo(imports.get(k).path()) > 0) {
          sorted = false;
          break;
        }
      }
      if (sorted) {
        for (ModuleSummary.Import anImport : imports) {
          if (!anImport.grouped() && anImport.path().compareTo(module) > 0) {
            int lineStart = lineStart(text, anImport.start());
            document.insertString(lineStart, line + "\n");
            return;
          }
        }
      }
      ModuleSummary.Import last = imports.get(imports.size() - 1);
      document.insertString(lineEnd(text, last.end()), "\n" + line);
      return;
    }

    List<Tok> tokens = Tok.lex(text);
    int headerEnd = -1;
    int previousEnd = 0;
    for (Tok token : tokens) {
      if (!token.isComment()) {
        break;
      }
      if (headerEnd >= 0 && blankLineBetween(text, previousEnd, token.start())) {
        break;
      }
      headerEnd = token.end();
      previousEnd = token.end();
    }
    // A comment that runs straight into the first declaration documents that
    // declaration, not the file; the import goes above it.
    int firstCode = text.length();
    for (Tok token : tokens) {
      if (!token.isComment()) {
        firstCode = token.start();
        break;
      }
    }
    if (headerEnd >= 0 && (firstCode == text.length() || blankLineBetween(text, headerEnd, firstCode))) {
      document.insertString(headerEnd, "\n\n" + line);
      return;
    }
    document.insertString(0, line + (text.length() == 0 ? "\n" : "\n\n"));
  }

  private static boolean blankLineBetween(CharSequence text, int from, int to) {
    int newlines = 0;
    for (int k = from; k < to && k < text.length(); k++) {
      if (text.charAt(k) == '\n' && ++newlines >= 2) {
        return true;
      }
    }
    return false;
  }

  private static int lineStart(CharSequence text, int offset) {
    int k = Math.min(offset, text.length());
    while (k > 0 && text.charAt(k - 1) != '\n') {
      k--;
    }
    return k;
  }

  private static int lineEnd(CharSequence text, int offset) {
    int k = offset;
    while (k < text.length() && text.charAt(k) != '\n') {
      k++;
    }
    return k;
  }
}
