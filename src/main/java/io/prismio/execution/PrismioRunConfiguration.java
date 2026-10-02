package io.prismio.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrismioRunConfiguration extends LocatableConfigurationBase<PrismioRunProfileState> {

  public static final String RUN = "run";
  public static final String BUILD = "build";
  public static final String TEST = "test";
  public static final String CLEAN = "clean";

  private String command = RUN;
  private boolean release;
  private boolean generated;
  private String filePath = "";
  private String target = "";
  private String programArgs = "";
  private String workingDirectory = "";

  public PrismioRunConfiguration(
      @NotNull Project project,
      @NotNull ConfigurationFactory factory,
      @Nullable String name) {
    super(project, factory, name);
  }

  /**
   * The CLI command: {@code run}, {@code build}, {@code test}, {@code clean}, or the
   * name of a {@code command} the project's build.ums declares ({@code prismio <name>}).
   */
  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command == null || command.isBlank() ? RUN : command.trim();
  }

  /** {@code --release}: the release profile, for the project commands that take one. */
  public boolean isRelease() {
    return release;
  }

  public void setRelease(boolean release) {
    this.release = release;
  }

  /**
   * Made from a build.ums by {@link UmsRunConfigurations}, which removes it again
   * when the target or command it runs leaves the manifest. One a user made or
   * copied is never touched.
   */
  public boolean isGenerated() {
    return generated;
  }

  public void setGenerated(boolean generated) {
    this.generated = generated;
  }

  /** Whether this runs a manifest's project command rather than one source file. */
  public boolean isProjectCommand() {
    return !RUN.equals(command) || filePath.isBlank();
  }

  @Override
  public @Nullable javax.swing.Icon getIcon() {
    return switch (command) {
      case RUN -> null;
      case BUILD -> com.intellij.icons.AllIcons.Actions.Compile;
      case TEST -> com.intellij.icons.AllIcons.RunConfigurations.Junit;
      case CLEAN -> com.intellij.icons.AllIcons.Actions.GC;
      default -> com.intellij.icons.AllIcons.Actions.Execute;
    };
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath != null ? filePath : "";
  }

  /** A target of the project's build.ums, run when no file is set. Empty means the only executable. */
  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target != null ? target : "";
  }

  public String getProgramArgs() {
    return programArgs;
  }

  public void setProgramArgs(String programArgs) {
    this.programArgs = programArgs != null ? programArgs : "";
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory != null ? workingDirectory : "";
  }

  @Override
  public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new PrismioRunConfigurationEditor(getProject());
  }

  @Override
  public @Nullable RunProfileState getState(
      @NotNull Executor executor,
      @NotNull ExecutionEnvironment environment) {
    return new PrismioRunProfileState(environment, this);
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    setCommand(element.getAttributeValue("command", RUN));
    release = Boolean.parseBoolean(element.getAttributeValue("release", "false"));
    generated = Boolean.parseBoolean(element.getAttributeValue("generated", "false"));
    filePath = element.getAttributeValue("filePath", "");
    target = element.getAttributeValue("target", "");
    programArgs = element.getAttributeValue("programArgs", "");
    workingDirectory = element.getAttributeValue("workingDirectory", "");
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    element.setAttribute("command", command);
    element.setAttribute("release", Boolean.toString(release));
    element.setAttribute("generated", Boolean.toString(generated));
    element.setAttribute("filePath", filePath);
    element.setAttribute("target", target);
    element.setAttribute("programArgs", programArgs);
    element.setAttribute("workingDirectory", workingDirectory);
  }
}
