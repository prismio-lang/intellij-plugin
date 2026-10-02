package io.prismio.execution;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.prismio.ums.UmsManifest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The run widget's entries, made from the project's build.ums files.
 *
 * <p>A Prismio project is run through its manifest, the way the CLI runs it:
 * {@code prismio build}, {@code prismio run <executable>}, {@code prismio test}
 * and {@code prismio <name>} for each declared command. Each becomes a run
 * configuration as soon as the project opens, and they follow the manifest:
 * adding a command adds one, removing a target removes its. Only the
 * configurations made here are ever changed or removed — one a user wrote,
 * copied or edited into something else is theirs.
 */
public final class UmsRunConfigurations {

  private UmsRunConfigurations() {}

  /** One configuration a manifest calls for. */
  record Spec(@NotNull String name, @NotNull String command, @NotNull String target,
      @NotNull String workingDirectory) {}

  /** A manifest, its directory, and what it declares. */
  record Located(@NotNull VirtualFile file, @NotNull UmsManifest manifest) {
    @NotNull String directory() {
      return file.getParent().getPath();
    }
  }

  /**
   * The manifests the project runs from. Fixtures and build output carry
   * manifests too — the compiler's own tests are full of them — and each would
   * otherwise add a Build and a Run to the widget.
   */
  static @NotNull List<Located> manifests(@NotNull Project project) {
    List<Located> out = new ArrayList<>();
    for (VirtualFile file : FilenameIndex.getVirtualFilesByName("build.ums",
        GlobalSearchScope.projectScope(project))) {
      // Judged below the project's directory: a project that lives under ~/build/ or
      // ~/fixtures/ is not made of build output.
      String path = file.getPath();
      String base = project.getBasePath();
      if (base != null && path.startsWith(base + "/")) {
        path = path.substring(base.length());
      }
      if (path.contains("/.prismio/") || path.contains("/build/") || path.contains("/fixtures/")
          || path.contains("/testData/") || path.contains("/node_modules/") || path.contains("/.git/")) {
        continue;
      }
      UmsManifest manifest = read(file);
      if (manifest != null) {
        out.add(new Located(file, manifest));
      }
    }
    out.sort((a, b) -> a.file().getPath().compareTo(b.file().getPath()));
    return out;
  }

  static @Nullable UmsManifest read(@NotNull VirtualFile file) {
    Document document = FileDocumentManager.getInstance().getCachedDocument(file);
    try {
      CharSequence text = document != null ? document.getImmutableCharSequence() : VfsUtilCore.loadText(file);
      return UmsManifest.read(text);
    } catch (IOException e) {
      return null;
    }
  }

  /** The configurations the manifests call for, named as the widget shows them. */
  static @NotNull List<Spec> specs(@NotNull List<Located> manifests) {
    List<Spec> specs = new ArrayList<>();
    boolean several = manifests.size() > 1;
    for (Located located : manifests) {
      UmsManifest manifest = located.manifest();
      String project = manifest.projectName() != null ? manifest.projectName()
          : located.file().getParent().getName();
      String suffix = several ? " (" + project + ")" : "";
      String dir = located.directory();
      for (UmsManifest.Target target : manifest.targets()) {
        if (target.isExecutable()) {
          specs.add(new Spec(runName(target.name()), PrismioRunConfiguration.RUN, target.name(), dir));
        }
      }
      if (!manifest.targets().isEmpty()) {
        specs.add(new Spec("Build" + suffix, PrismioRunConfiguration.BUILD, "", dir));
      }
      if (manifest.targets().stream().anyMatch(UmsManifest.Target::isTest)) {
        specs.add(new Spec("Test" + suffix, PrismioRunConfiguration.TEST, "", dir));
      }
      for (UmsManifest.Command command : manifest.commands()) {
        specs.add(new Spec(command.name() + suffix, command.name(), "", dir));
      }
    }
    // Two manifests with a target of the same name: keep both, told apart.
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (Spec spec : specs) {
      counts.merge(spec.name(), 1, Integer::sum);
    }
    List<Spec> unique = new ArrayList<>();
    for (Spec spec : specs) {
      unique.add(counts.get(spec.name()) > 1
          ? new Spec(spec.name() + " (" + lastSegment(spec.workingDirectory()) + ")", spec.command(),
              spec.target(), spec.workingDirectory())
          : spec);
    }
    return unique;
  }

  static @NotNull String runName(@NotNull String target) {
    return "Run " + target;
  }

  private static String lastSegment(String path) {
    int slash = path.lastIndexOf('/');
    return slash >= 0 ? path.substring(slash + 1) : path;
  }

  /** The CLI's commands, then every command a manifest in the project declares. For the editor's list. */
  static @NotNull List<String> commandNames(@NotNull Project project) {
    Set<String> names = new LinkedHashSet<>(List.of(PrismioRunConfiguration.RUN, PrismioRunConfiguration.BUILD,
        PrismioRunConfiguration.TEST, PrismioRunConfiguration.CLEAN));
    if (!DumbService.isDumb(project)) {
      for (Located located : ReadAction.computeBlocking(() -> manifests(project))) {
        for (UmsManifest.Command command : located.manifest().commands()) {
          names.add(command.name());
        }
      }
    }
    return List.copyOf(names);
  }

  /** The manifest target whose entry is {@code path}, and the manifest's directory. */
  static @Nullable Spec runSpecForEntry(@NotNull Project project, @NotNull String path) {
    for (Located located : manifests(project)) {
      for (UmsManifest.Target target : located.manifest().targets()) {
        if (target.isExecutable() && target.entry() != null) {
          VirtualFile entry = located.file().getParent().findFileByRelativePath(target.entry());
          if (entry != null && entry.getPath().equals(path)) {
            List<Spec> specs = specs(manifests(project));
            for (Spec spec : specs) {
              if (spec.command().equals(PrismioRunConfiguration.RUN) && spec.target().equals(target.name())
                  && spec.workingDirectory().equals(located.directory())) {
                return spec;
              }
            }
          }
        }
      }
    }
    return null;
  }

  /** What running the declaration at {@code offset} of a build.ums means. */
  static @Nullable Spec specForDeclaration(@NotNull Project project, @NotNull VirtualFile manifestFile,
      @NotNull UmsManifest manifest, int offset) {
    Object declaration = manifest.declarationAt(offset);
    if (declaration == null) {
      return null;
    }
    String dir = manifestFile.getParent().getPath();
    for (Spec spec : specs(manifests(project))) {
      if (!spec.workingDirectory().equals(dir)) {
        continue;
      }
      if (declaration instanceof UmsManifest.Command command && spec.command().equals(command.name())) {
        return spec;
      }
      if (declaration instanceof UmsManifest.Target target) {
        if (target.isExecutable() && spec.command().equals(PrismioRunConfiguration.RUN)
            && spec.target().equals(target.name())) {
          return spec;
        }
        if (target.isTest() && spec.command().equals(PrismioRunConfiguration.TEST)) {
          return new Spec(spec.name(), PrismioRunConfiguration.TEST, target.name(), dir);
        }
      }
    }
    return null;
  }

  /**
   * A single-file run of a target's entry that carries nothing its user set:
   * no arguments and no working directory. Anything more is kept, as theirs.
   */
  private static boolean supersededFileRun(@NotNull PrismioRunConfiguration configuration,
      @NotNull Set<String> entries) {
    return PrismioRunConfiguration.RUN.equals(configuration.getCommand())
        && entries.contains(configuration.getFilePath())
        && configuration.getProgramArgs().isBlank()
        && configuration.getWorkingDirectory().isBlank()
        && !configuration.isRelease();
  }

  /** Every executable target's entry file, by path. */
  static @NotNull Set<String> entryPaths(@NotNull List<Located> manifests) {
    Set<String> paths = new LinkedHashSet<>();
    for (Located located : manifests) {
      for (UmsManifest.Target target : located.manifest().targets()) {
        if (target.isExecutable() && target.entry() != null) {
          VirtualFile entry = located.file().getParent().findFileByRelativePath(target.entry());
          if (entry != null) {
            paths.add(entry.getPath());
          }
        }
      }
    }
    return paths;
  }

  static void apply(@NotNull PrismioRunConfiguration configuration, @NotNull Spec spec) {
    configuration.setName(spec.name());
    configuration.setCommand(spec.command());
    configuration.setTarget(spec.target());
    configuration.setFilePath("");
    configuration.setWorkingDirectory(spec.workingDirectory());
  }

  // ------------------------------------------------------------ keeping the widget in step

  /** Brings the project's generated configurations in line with its manifests. Call on the EDT. */
  static void sync(@NotNull Project project, @NotNull List<Spec> specs) {
    sync(project, specs, Set.of());
  }

  /**
   * @param entries the paths of every executable target's entry: a plain
   *     {@code Run main.psm} of one of those, from before runs went through the
   *     manifest, is what {@code Run <target>} now does, and is removed with it
   */
  static void sync(@NotNull Project project, @NotNull List<Spec> specs, @NotNull Set<String> entries) {
    RunManager runManager = RunManager.getInstance(project);
    PrismioRunConfigurationType type = PrismioRunConfigurationType.getInstance();
    Map<String, RunnerAndConfigurationSettings> byName = new LinkedHashMap<>();
    for (RunnerAndConfigurationSettings settings : runManager.getConfigurationSettingsList(type)) {
      byName.put(settings.getName(), settings);
    }
    Set<String> wanted = new LinkedHashSet<>();
    RunnerAndConfigurationSettings firstRun = null;
    for (Spec spec : specs) {
      wanted.add(spec.name());
      RunnerAndConfigurationSettings existing = byName.get(spec.name());
      if (existing != null) {
        if (existing.getConfiguration() instanceof PrismioRunConfiguration configuration
            && configuration.isGenerated()) {
          apply(configuration, spec);
        }
        if (firstRun == null && spec.command().equals(PrismioRunConfiguration.RUN)) {
          firstRun = existing;
        }
        continue;
      }
      RunnerAndConfigurationSettings settings =
          runManager.createConfiguration(spec.name(), type.getConfigurationFactories()[0]);
      PrismioRunConfiguration configuration = (PrismioRunConfiguration) settings.getConfiguration();
      apply(configuration, spec);
      configuration.setGenerated(true);
      runManager.addConfiguration(settings);
      if (firstRun == null && spec.command().equals(PrismioRunConfiguration.RUN)) {
        firstRun = settings;
      }
    }
    for (RunnerAndConfigurationSettings settings : byName.values()) {
      if (!wanted.contains(settings.getName())
          && settings.getConfiguration() instanceof PrismioRunConfiguration configuration
          && (configuration.isGenerated() || supersededFileRun(configuration, entries))) {
        runManager.removeConfiguration(settings);
      }
    }
    if (runManager.getSelectedConfiguration() == null && firstRun != null) {
      runManager.setSelectedConfiguration(firstRun);
    }
  }

  /** Reads the manifests off the EDT once indexing allows, then syncs on it. */
  static void scheduleSync(@NotNull Project project) {
    DumbService.getInstance(project).runWhenSmart(() ->
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          if (project.isDisposed()) {
            return;
          }
          List<Located> manifests = ReadAction.computeBlocking(() -> project.isDisposed() ? List.<Located>of() : manifests(project));
          List<Spec> specs = specs(manifests);
          Set<String> entries = ReadAction.computeBlocking(() -> entryPaths(manifests));
          ApplicationManager.getApplication().invokeLater(() -> sync(project, specs, entries), project.getDisposed());
        }));
  }

  /** At startup, and again whenever a build.ums is saved, created, moved or deleted. */
  public static final class Startup implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
      scheduleSync(project);
      AtomicBoolean pending = new AtomicBoolean();
      project.getMessageBus().connect(project).subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
        @Override
        public void after(@NotNull List<? extends VFileEvent> events) {
          for (VFileEvent event : events) {
            String path = event.getPath();
            if (path.endsWith("/build.ums") && pending.compareAndSet(false, true)) {
              ApplicationManager.getApplication().invokeLater(() -> {
                pending.set(false);
                if (!project.isDisposed()) {
                  scheduleSync(project);
                }
              }, project.getDisposed());
              return;
            }
          }
        }
      });
      return Unit.INSTANCE;
    }
  }
}
