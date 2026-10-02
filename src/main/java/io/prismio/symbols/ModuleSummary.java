package io.prismio.symbols;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * What one module declares, read off its source by {@link SignatureParser}.
 *
 * @param name the name an importer writes: {@code std.io}
 * @param doc the module's opening comment, which in this codebase says what the module is for
 * @param functions free functions and every {@code impl} member, told apart by {@link FnSig#owner}
 * @param globals module-level {@code let} bindings: {@code process} in {@code std.process}
 * @param imports what the module imports, in source order
 */
public record ModuleSummary(
    @NotNull String name,
    @NotNull String doc,
    @NotNull List<FnSig> functions,
    @NotNull List<TypeDecl> types,
    @NotNull List<Global> globals,
    @NotNull List<Import> imports) {

  /** A module-level {@code let}, or an associated constant when {@code owner} is set ({@code Int.MAX}). */
  public record Global(@NotNull String name, @NotNull String module, @Nullable TypeRef owner,
      @Nullable TypeRef type, @NotNull String doc, int offset) {}

  /**
   * One module an {@code import} names.
   *
   * @param path the module path, or the package path for a wildcard
   * @param wildcard {@code import std.*} or {@code import * from std}
   * @param alias the {@code as} name, or null
   * @param start where the statement starts in the text
   * @param end where the statement ends in the text
   * @param grouped written as one entry of {@code import {a, b} from dir}
   */
  public record Import(@NotNull String path, boolean wildcard, @Nullable String alias,
      int start, int end, boolean grouped) {}
}
