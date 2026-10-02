package io.prismio.symbols;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import io.prismio.toolchain.PrismioToolchainService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The standard library of the toolchain this project uses, as declarations.
 *
 * <p>Read from the toolchain rather than written into the plugin, so a newer or
 * older compiler brings its own library with it: a method it adds is offered, a
 * method it removes is not. Two layouts are understood:
 *
 * <ul>
 *   <li>a checkout's {@code std/*.psm}, named {@code std.<file>};
 *   <li>an installed toolchain's {@code stdlib/*.plib}. Each carries the module's
 *       name and its source as the "interface" section ({@code tools/package.py},
 *       {@code build_plib}), which is exactly what a signature reader needs.
 * </ul>
 *
 * <p>Rebuilt when the directory or any file in it changes, checked at most once
 * a second: completion asks on every keystroke.
 */
@Service(Service.Level.PROJECT)
public final class StdlibIndex {

  private static final long RECHECK_NANOS = 1_000_000_000L;

  private final Project project;
  private volatile Snapshot snapshot = Snapshot.EMPTY;
  private volatile String key = "";
  private volatile long checkedAt;

  public StdlibIndex(@NotNull Project project) {
    this.project = project;
  }

  public static @NotNull StdlibIndex getInstance(@NotNull Project project) {
    return project.getService(StdlibIndex.class);
  }

  /** The current library, rebuilt first if the toolchain's files changed. Never null; empty without a toolchain. */
  public @NotNull Snapshot get() {
    long now = System.nanoTime();
    if (now - checkedAt < RECHECK_NANOS && snapshot != Snapshot.EMPTY) {
      return snapshot;
    }
    checkedAt = now;
    Path dir = PrismioToolchainService.getInstance(project).getStdlibDirectory();
    String current = dir == null ? "" : fingerprint(dir);
    if (!current.equals(key)) {
      synchronized (this) {
        if (!current.equals(key)) {
          snapshot = dir == null ? Snapshot.EMPTY : Snapshot.of(dir, read(dir));
          key = current;
        }
      }
    }
    return snapshot;
  }

  private static @NotNull String fingerprint(@NotNull Path dir) {
    StringBuilder out = new StringBuilder(dir.toString());
    try (Stream<Path> files = Files.list(dir)) {
      files.filter(StdlibIndex::isModuleFile).sorted().forEach(file -> {
        try {
          out.append('|').append(file.getFileName()).append(':')
              .append(Files.getLastModifiedTime(file).toMillis()).append(':')
              .append(Files.size(file));
        } catch (IOException ignored) {
          // A file that vanished between the listing and the stat: the next check sees it gone.
        }
      });
    } catch (IOException e) {
      return "";
    }
    return out.toString();
  }

  private static boolean isModuleFile(@NotNull Path file) {
    String name = file.getFileName().toString();
    return name.endsWith(".psm") || name.endsWith(".plib");
  }

  /** Every module in {@code dir}, the {@code .psm} winning when a module is there in both forms. */
  static @NotNull List<ModuleSummary> read(@NotNull Path dir) {
    Map<String, ModuleSummary> modules = new TreeMap<>();
    try (Stream<Path> files = Files.list(dir)) {
      for (Path file : files.filter(StdlibIndex::isModuleFile).sorted().toList()) {
        String fileName = file.getFileName().toString();
        try {
          if (fileName.endsWith(".psm")) {
            String module = "std." + fileName.substring(0, fileName.length() - 4);
            modules.put(module, SignatureParser.parse(Files.readString(file), module));
          } else {
            Interface plib = readPlib(Files.readAllBytes(file));
            if (plib != null && !modules.containsKey(plib.module())) {
              modules.put(plib.module(), SignatureParser.parse(plib.source(), plib.module()));
            }
          }
        } catch (IOException | RuntimeException ignored) {
          // One unreadable module costs its own completions, not everyone else's.
        }
      }
    } catch (IOException e) {
      return List.of();
    }
    return List.copyOf(modules.values());
  }

  record Interface(@NotNull String module, @NotNull String source) {}

  /**
   * {@code "PRPLIB3\n" u32 module_len u64 interface_len u32 section_count module interface ...},
   * little-endian, as {@code tools/package.py} writes it. An unknown version is
   * skipped rather than guessed at.
   */
  static @Nullable Interface readPlib(byte[] bytes) {
    byte[] magic = "PRPLIB3\n".getBytes(StandardCharsets.US_ASCII);
    if (bytes.length < magic.length + 16) {
      return null;
    }
    for (int k = 0; k < magic.length; k++) {
      if (bytes[k] != magic[k]) {
        return null;
      }
    }
    ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    buffer.position(magic.length);
    long moduleLength = Integer.toUnsignedLong(buffer.getInt());
    long interfaceLength = buffer.getLong();
    buffer.getInt();
    long start = buffer.position();
    if (moduleLength < 0 || interfaceLength < 0 || start + moduleLength + interfaceLength > bytes.length) {
      return null;
    }
    String module = new String(bytes, (int) start, (int) moduleLength, StandardCharsets.UTF_8);
    String source = new String(bytes, (int) (start + moduleLength), (int) interfaceLength,
        StandardCharsets.UTF_8);
    return new Interface(module, source);
  }

  /** One toolchain's library, with the lookups completion needs built once. */
  public static final class Snapshot {
    static final Snapshot EMPTY = new Snapshot(null, List.of());

    private final @Nullable Path directory;
    private final Map<String, ModuleSummary> modules = new LinkedHashMap<>();
    private final Map<String, List<FnSig>> freeFunctions = new TreeMap<>();
    private final Map<String, TypeDecl> types = new TreeMap<>();
    private final Map<String, ModuleSummary.Global> globals = new TreeMap<>();
    private final SymbolTable table = new SymbolTable();

    private Snapshot(@Nullable Path directory, @NotNull List<ModuleSummary> summaries) {
      this.directory = directory;
      for (ModuleSummary summary : summaries) {
        modules.put(summary.name(), summary);
        table.add(summary, SymbolTable.EXPORTED);
        for (FnSig fn : summary.functions()) {
          if (fn.owner() == null && fn.visibleOutside()) {
            freeFunctions.computeIfAbsent(fn.name(), k -> new ArrayList<>()).add(fn);
          }
        }
        for (TypeDecl type : summary.types()) {
          types.putIfAbsent(type.name(), type);
        }
        for (ModuleSummary.Global global : summary.globals()) {
          if (global.owner() == null) {
            globals.putIfAbsent(global.name(), global);
          }
        }
      }
    }

    static @NotNull Snapshot of(@NotNull Path directory, @NotNull List<ModuleSummary> summaries) {
      return new Snapshot(directory, summaries);
    }

    public @Nullable Path directory() {
      return directory;
    }

    public boolean isEmpty() {
      return modules.isEmpty();
    }

    public @NotNull Map<String, ModuleSummary> modules() {
      return Collections.unmodifiableMap(modules);
    }

    /** Public free functions by name, every overload, across every module. */
    public @NotNull Map<String, List<FnSig>> freeFunctions() {
      return Collections.unmodifiableMap(freeFunctions);
    }

    public @NotNull Map<String, TypeDecl> types() {
      return Collections.unmodifiableMap(types);
    }

    public @NotNull Map<String, ModuleSummary.Global> globals() {
      return Collections.unmodifiableMap(globals);
    }

    public @NotNull SymbolTable table() {
      return table;
    }
  }
}
