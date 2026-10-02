package io.prismio.execution;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public final class PrismioRunConfigurationEditor extends SettingsEditor<PrismioRunConfiguration> {

  private final ComboBox<String> commandField;
  private final JBCheckBox releaseField;
  private final TextFieldWithBrowseButton filePathField;
  private final JBTextField targetField;
  private final JBTextField programArgsField;
  private final TextFieldWithBrowseButton workingDirField;
  private final JPanel panel;

  public PrismioRunConfigurationEditor(@NotNull Project project) {
    // The CLI's own commands, then every command the project's manifests declare.
    commandField = new ComboBox<>(UmsRunConfigurations.commandNames(project).toArray(String[]::new));
    commandField.setEditable(true);
    releaseField = new JBCheckBox("Release profile (--release)");

    filePathField = new TextFieldWithBrowseButton();
    filePathField.addBrowseFolderListener(
        project,
        FileChooserDescriptorFactory.createSingleFileDescriptor("psm")
            .withTitle("Select Prismio Source File")
    );

    targetField = new JBTextField();
    targetField.getEmptyText().setText("the project's only executable");

    programArgsField = new JBTextField();

    workingDirField = new TextFieldWithBrowseButton();
    workingDirField.addBrowseFolderListener(
        project,
        FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle("Select Working Directory")
    );

    panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Command:", commandField)
        .addComponent(releaseField)
        .addLabeledComponent("Prismio file:", filePathField)
        .addLabeledComponent("Or project target:", targetField)
        .addLabeledComponent("Arguments:", programArgsField)
        .addLabeledComponent("Working directory:", workingDirField)
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();
  }

  @Override
  protected void resetEditorFrom(@NotNull PrismioRunConfiguration config) {
    commandField.setSelectedItem(config.getCommand());
    releaseField.setSelected(config.isRelease());
    filePathField.setText(config.getFilePath());
    targetField.setText(config.getTarget());
    programArgsField.setText(config.getProgramArgs());
    workingDirField.setText(config.getWorkingDirectory());
  }

  @Override
  protected void applyEditorTo(@NotNull PrismioRunConfiguration config) {
    Object command = commandField.getEditor().getItem();
    config.setCommand(command == null ? "" : command.toString());
    config.setRelease(releaseField.isSelected());
    config.setFilePath(filePathField.getText().trim());
    config.setTarget(targetField.getText().trim());
    config.setProgramArgs(programArgsField.getText().trim());
    config.setWorkingDirectory(workingDirField.getText().trim());
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return panel;
  }
}
