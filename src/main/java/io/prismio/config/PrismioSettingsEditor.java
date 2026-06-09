package io.prismio.config;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PrismioSettingsEditor extends SettingsEditor<PrismioRunConfiguration> {

  private final JPanel myPanel;
  private final TextFieldWithBrowseButton scriptPathField;

  @SuppressWarnings("deprecation")
  public PrismioSettingsEditor() {
    scriptPathField = new TextFieldWithBrowseButton();
    scriptPathField.addBrowseFolderListener(
            "Select Script File",
            null,
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor("psm")
    );

    myPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Script file", scriptPathField)
            .getPanel();
  }

  @Override
  protected void resetEditorFrom(PrismioRunConfiguration prismioRunConfiguration) {
    scriptPathField.setText(prismioRunConfiguration.getScriptName());
  }

  @Override
  protected void applyEditorTo(@NotNull PrismioRunConfiguration prismioRunConfiguration) {
    prismioRunConfiguration.setScriptName(scriptPathField.getText());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myPanel;
  }

}