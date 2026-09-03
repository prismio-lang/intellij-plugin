package io.prismio.ums;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * What the manifest's identifiers mean.
 *
 * <p>None of these are keywords — {@code ums/parser/} emits a plain identifier
 * for every one, and the meaning is added by {@code ums/model/lowering.psm}. The
 * tables here mirror that file so the editor recognises exactly what the
 * compiler recognises, and colours nothing it would reject.
 *
 * <p>Each entry carries its own description because completion, documentation
 * and the annotator all want the same sentence, and three copies of it drift.
 */
public final class UmsWords {

  private UmsWords() {}

  /** Blocks that may appear at the top level, in the order a manifest wants them. */
  public static final Map<String, String> TOP_LEVEL_BLOCKS = ordered(
      "toolchain", "Selects the project-local compiler. Must be the first block.",
      "project", "Project identity: name, version, and the Prismio version it needs.",
      "targets", "The artifacts this project builds.",
      "commands", "Commands the project owns, invoked as `prismio <name>`.",
      "dependencies", "Packages this project needs.");

  /** Keys inside `project { }`. */
  public static final Map<String, String> PROJECT_KEYS = ordered(
      "name", "Package name. Letters, digits, `-`, `_` and `.`.",
      "version", "This project's version, as `x.y.z`.",
      "prismio", "The Prismio version this project requires.",
      "description", "One line about the package. Must not be empty.",
      "license", "An SPDX expression, such as `Apache-2.0`.",
      "licenseFile", "A project-relative licence file. Mutually exclusive with `license`.",
      "authors", "An array of author strings.");

  /** Keys inside `toolchain { }`. */
  public static final Map<String, String> TOOLCHAIN_KEYS = ordered(
      "host", "Project-relative path to the compiler that owns this project's commands.");

  /** Declarations inside `targets { }`. */
  public static final Map<String, String> TARGET_KINDS = ordered(
      "executable", "A program. Links the Prismio runtime only, unless it names a component.",
      "library", "Modelled and validated; artifact emission is not implemented yet.",
      "test", "A program that exits 0 when it passes. `prismio test` builds and runs it.");

  /** Keys and blocks inside a target body. */
  public static final Map<String, String> TARGET_BODY = ordered(
      "entry", "The source file this target is built from.",
      "link", "Ordered native linker inputs for this target.");

  /** Declarations inside a `link { }` block. */
  public static final Map<String, String> LINK_KINDS = ordered(
      "library", "Passes `-l<name>`.",
      "search", "A project-root-relative native search path.",
      "file", "An exact project-root-relative object or library.",
      "framework", "A Mach-O framework. Ignored on other targets.",
      "component", "A toolchain-owned bundle. The only one is `prismio.backend`.");

  /** Declarations inside `dependencies { }`. */
  public static final Map<String, String> DEPENDENCY_SCOPES = ordered(
      "implementation", "A dependency this project uses internally.",
      "api", "A dependency this project exposes in its own API.",
      "testImplementation", "A dependency only the test targets need.");

  /** Declarations inside `commands { }`. */
  public static final Map<String, String> COMMAND_KINDS = ordered(
      "command", "One command, invoked as `prismio <name>`.");

  /** Keys and steps inside a `command(...)` body. */
  public static final Map<String, String> COMMAND_BODY = ordered(
      "description", "One line shown when the command is listed.",
      "build", "Builds a declared target. Takes exactly one target name.",
      "run", "Runs a declared target, a `.py` script, or a `.psm` tool.",
      "shell", "Runs an external program. Portability is yours.");

  /**
   * The one identifier that is a value rather than a name: inside a `run` or
   * `shell` step it splices in whatever the user typed after the command name,
   * keeping its position among the fixed arguments.
   */
  public static final String ARGS_MARKER = "args";

  /** The only component the toolchain defines. */
  public static final Set<String> COMPONENTS = Set.of("prismio.backend");

  /** Every identifier the manifest gives meaning to, for a quick "is this known" test. */
  public static final Set<String> ALL_KNOWN = collect();

  private static Map<String, String> ordered(String... pairs) {
    Map<String, String> out = new LinkedHashMap<>();
    for (int i = 0; i < pairs.length; i += 2) {
      out.put(pairs[i], pairs[i + 1]);
    }
    return Map.copyOf(out);
  }

  private static Set<String> collect() {
    var names = new java.util.HashSet<String>();
    for (Map<String, String> table : java.util.List.of(TOP_LEVEL_BLOCKS, PROJECT_KEYS,
        TOOLCHAIN_KEYS, TARGET_KINDS, TARGET_BODY, LINK_KINDS, DEPENDENCY_SCOPES,
        COMMAND_KINDS, COMMAND_BODY)) {
      names.addAll(table.keySet());
    }
    names.add(ARGS_MARKER);
    return Set.copyOf(names);
  }
}
