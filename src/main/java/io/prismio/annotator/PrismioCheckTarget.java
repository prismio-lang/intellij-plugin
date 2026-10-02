package io.prismio.annotator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The program a file is checked through: {@code prismio check <entry> --overlay <file> <buffer>},
 * with {@code --module <module>} when the file is a module checked on its own.
 *
 * <p>A Prismio module is not checkable alone. Every module of a program shares the names the
 * merge gives the whole program, so {@code src/parse/stmt.psm} uses {@code Parser} without
 * importing the module that declares it, and its imports resolve against the directory of the
 * program's entry, not its own ({@code import lexer.token} is {@code src/lexer/token.psm}).
 * Checking the file as if it were a program reports both as errors that are not there.
 *
 * <p>{@link #plan} lists the programs to try, most likely first; the compiler answers P1075
 * for one that never reads the file, and the caller moves on to the next.
 */
public record PrismioCheckTarget(@NotNull Path entry, @Nullable String module) {

  /** Programs tried at most, besides the file itself, for a file that is not one. */
  private static final int MAX_PROGRAMS = 6;

  private static final Pattern MAIN =
      Pattern.compile("^\\s*(?:public\\s+)?fn\\s+main\\s*\\(", Pattern.MULTILINE);
  private static final Pattern ENTRY =
      Pattern.compile("^\\s*entry\\s*=\\s*\"([^\"]+)\"", Pattern.MULTILINE);

  /** Whether the file is a program: it declares {@code fn main}. */
  public static boolean declaresMain(@NotNull CharSequence text) {
    return MAIN.matcher(text).find();
  }

  /**
   * Where to check {@code file}, whose current text is {@code text}, in the order to try:
   *
   * <ol>
   *   <li>a file that declares {@code main} is its own program;
   *   <li>a module of a standard library ({@code std/map.psm} beside {@code std/io.psm} and
   *       {@code std/string.psm}) is checked alone as {@code std.map};
   *   <li>the {@code entry} of each target in the nearest {@code build.ums};
   *   <li>any program in the file's directory or an ancestor, up to that manifest (or
   *       {@code boundary}, the project's directory, when there is none), that imports the file
   *       by the module name it has from there;
   *   <li>the file alone, which is right for a program and the best there is otherwise.
   * </ol>
   */
  public static @NotNull List<PrismioCheckTarget> plan(
      @NotNull Path file, @NotNull CharSequence text, @Nullable Path boundary) {
    Path self = file.toAbsolutePath().normalize();
    List<PrismioCheckTarget> targets = new ArrayList<>();
    if (declaresMain(text)) {
      targets.add(new PrismioCheckTarget(self, null));
      return targets;
    }
    String stdModule = standardLibraryModule(self);
    if (stdModule != null) {
      targets.add(new PrismioCheckTarget(self, stdModule));
      return targets;
    }

    Set<Path> programs = new LinkedHashSet<>();
    Path manifestDir = null;
    for (Path dir = self.getParent(); dir != null; dir = dir.getParent()) {
      Path manifest = dir.resolve("build.ums");
      if (Files.isRegularFile(manifest)) {
        manifestDir = dir;
        Matcher m = ENTRY.matcher(read(manifest));
        while (m.find()) {
          programs.add(dir.resolve(m.group(1)).normalize());
        }
        break;
      }
    }
    for (Path dir = self.getParent(); dir != null; dir = dir.getParent()) {
      String module = moduleName(dir, self);
      for (Path candidate : psmFilesIn(dir)) {
        if (candidate.equals(self)) {
          continue;
        }
        String candidateText = read(candidate);
        if (declaresMain(candidateText) && importsModule(candidateText, module)) {
          programs.add(candidate);
        }
      }
      if (dir.equals(manifestDir)) {
        break;
      }
      if (manifestDir == null) {
        // Without a manifest the search ends at the project's directory; a file outside any
        // project (or a project with no such ancestor) is looked for in its own directory only,
        // not read through every folder up to the filesystem root.
        Path root = boundary == null ? null : boundary.toAbsolutePath().normalize();
        if (root == null || !self.startsWith(root) || dir.equals(root)) {
          break;
        }
      }
    }
    programs.remove(self);
    programs.stream().limit(MAX_PROGRAMS).forEach(p -> targets.add(new PrismioCheckTarget(p, null)));
    targets.add(new PrismioCheckTarget(self, null));
    return targets;
  }

  /**
   * {@code std.<leaf>} for a file in a standard library directory, or null. The directory is
   * recognised by name and by the two modules every program's I/O goes through, so a project's
   * own folder that happens to be called {@code std} is not given the library's access.
   */
  static @Nullable String standardLibraryModule(@NotNull Path file) {
    Path dir = file.getParent();
    if (dir == null || dir.getFileName() == null || !"std".equals(dir.getFileName().toString())) {
      return null;
    }
    if (!Files.isRegularFile(dir.resolve("io.psm")) || !Files.isRegularFile(dir.resolve("string.psm"))) {
      return null;
    }
    String name = file.getFileName().toString();
    return "std." + name.substring(0, name.length() - ".psm".length());
  }

  /** The module {@code file} is imported as from {@code root}: {@code root/parse/stmt.psm} is {@code parse.stmt}. */
  static @NotNull String moduleName(@NotNull Path root, @NotNull Path file) {
    String relative = root.relativize(file).toString().replace('\\', '/');
    if (relative.endsWith(".psm")) {
      relative = relative.substring(0, relative.length() - ".psm".length());
    }
    return relative.replace('/', '.');
  }

  /**
   * Whether {@code text} imports {@code module}: by name ({@code import parse.stmt}, with a
   * selection or an alias after it), with its package ({@code import parse.*},
   * {@code import * from parse}), or in a group ({@code import {stmt, expr} from parse}).
   */
  static boolean importsModule(@NotNull CharSequence text, @NotNull String module) {
    String quoted = Pattern.quote(module);
    if (Pattern.compile("^\\s*import\\s+" + quoted + "(?:[\\s.;]|$)", Pattern.MULTILINE).matcher(text).find()) {
      return true;
    }
    int dot = module.lastIndexOf('.');
    if (dot < 0) {
      return false;
    }
    String pkg = Pattern.quote(module.substring(0, dot));
    String leaf = module.substring(dot + 1);
    if (Pattern.compile("^\\s*import\\s+" + pkg + "\\.\\*", Pattern.MULTILINE).matcher(text).find()
        || Pattern.compile("^\\s*import\\s+\\*\\s+from\\s+" + pkg + "\\b", Pattern.MULTILINE).matcher(text).find()) {
      return true;
    }
    Matcher group = Pattern.compile("^\\s*import\\s*\\{([^}]*)}\\s*from\\s+" + pkg + "\\b", Pattern.MULTILINE)
        .matcher(text);
    while (group.find()) {
      for (String entry : group.group(1).split(",")) {
        String[] words = entry.trim().split("\\s+");
        if (words.length > 0 && words[0].equals(leaf)) {
          return true;
        }
      }
    }
    return false;
  }

  private static @NotNull List<Path> psmFilesIn(@NotNull Path dir) {
    try (Stream<Path> children = Files.list(dir)) {
      return children.filter(p -> p.toString().endsWith(".psm") && Files.isRegularFile(p))
          .map(p -> p.toAbsolutePath().normalize())
          .sorted()
          .toList();
    } catch (IOException | SecurityException e) {
      return List.of();
    }
  }

  private static @NotNull String read(@NotNull Path path) {
    try {
      return Files.readString(path);
    } catch (IOException | SecurityException e) {
      return "";
    }
  }
}
