package io.prismio.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.util.execution.ParametersListUtil;
import io.prismio.toolchain.PrismioToolchainService;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PrismioRunProfileState extends CommandLineState {

  private final PrismioRunConfiguration configuration;

  public PrismioRunProfileState(
      @NotNull ExecutionEnvironment environment,
      @NotNull PrismioRunConfiguration configuration) {
    super(environment);
    this.configuration = configuration;
    addConsoleFilters(new PrismioConsoleFilter(environment.getProject()));
  }

  @Override
  protected @NotNull ProcessHandler startProcess() throws ExecutionException {
    Path compiler = PrismioToolchainService.getInstance(configuration.getProject()).getCompilerExecutable();
    String exePath = compiler != null ? compiler.toString() : "prismio";

    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(exePath);
    cmd.addParameters(arguments(configuration.getCommand(), configuration.getFilePath(),
        configuration.getTarget(), configuration.getProgramArgs(), configuration.isRelease()));

    String workDir = configuration.getWorkingDirectory();
    if (workDir == null || workDir.isBlank()) {
      workDir = configuration.getProject().getBasePath();
    }
    if (workDir != null) {
      cmd.setWorkDirectory(workDir);
    }

    // The run console is a pipe, so the program would see "not a terminal" and drop its colours;
    // the console renders ANSI, so say so. A user's NO_COLOR or own FORCE_COLOR is left alone.
    if (System.getenv("NO_COLOR") == null && System.getenv("FORCE_COLOR") == null) {
      cmd.withEnvironment("FORCE_COLOR", "1");
    }

    // The coloured handler: a plain OSProcessHandler would print the escape bytes as text.
    OSProcessHandler handler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(cmd);
    ProcessTerminatedListener.attach(handler);
    return handler;
  }

  /**
   * The arguments after the compiler's name.
   *
   * <p>{@code run <file> [-- args]}, or in project mode -- no file -- {@code run [target] [-- args]}.
   * Program arguments follow a {@code --}: without it {@code prismio run} reads them as its own
   * and refuses the first it does not know (P1050). A program's exit status comes back as the
   * process's, so the console's "exit code N" is the program's answer, not a compiler failure;
   * the compiler's own failures are the {@code error[P....]} lines above it.
   */
  static List<String> arguments(String filePath, String target, String programArgs) {
    return arguments(PrismioRunConfiguration.RUN, filePath, target, programArgs, false);
  }

  /**
   * Every command the CLI's usage lists: {@code build [--release] [target]},
   * {@code test [--release] [target]}, {@code clean [--release]}, and
   * {@code <name> [args...]} for a manifest command, which receives its arguments
   * as they are -- its steps splice them in where the manifest writes {@code args}.
   */
  static List<String> arguments(String command, String filePath, String target, String programArgs,
      boolean release) {
    List<String> arguments = new ArrayList<>();
    String name = command == null || command.isBlank() ? PrismioRunConfiguration.RUN : command.trim();
    boolean hasFile = filePath != null && !filePath.isBlank();
    boolean hasTarget = target != null && !target.isBlank();
    switch (name) {
      case PrismioRunConfiguration.BUILD, PrismioRunConfiguration.TEST, PrismioRunConfiguration.CLEAN -> {
        arguments.add(name);
        if (release) {
          arguments.add("--release");
        }
        if (hasTarget && !name.equals(PrismioRunConfiguration.CLEAN)) {
          arguments.add(target.trim());
        }
        return arguments;
      }
      case PrismioRunConfiguration.RUN -> {
      }
      default -> {
        arguments.add(name);
        if (programArgs != null && !programArgs.isBlank()) {
          arguments.addAll(ParametersListUtil.parse(programArgs));
        }
        return arguments;
      }
    }
    arguments.add("run");
    if (hasFile) {
      arguments.add(filePath);
    } else {
      if (release) {
        arguments.add("--release");
      }
      if (hasTarget) {
        arguments.add(target.trim());
      }
    }
    if (programArgs != null && !programArgs.isBlank()) {
      arguments.add("--");
      // Quoted as a shell would: "a b" is one argument, not two.
      arguments.addAll(ParametersListUtil.parse(programArgs));
    }
    return arguments;
  }
}
