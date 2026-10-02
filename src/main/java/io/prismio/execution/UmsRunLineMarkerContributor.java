package io.prismio.execution;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import io.prismio.ums.UmsFile;
import io.prismio.ums.UmsManifest;
import io.prismio.ums.UmsTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A run icon beside every runnable declaration of a build.ums: each
 * {@code executable}, {@code test} and {@code command}. What the icon runs is
 * decided by {@link UmsRunConfigurationProducer}, so it is the same
 * configuration the run widget lists.
 */
public final class UmsRunLineMarkerContributor extends RunLineMarkerContributor {

  @Override
  public @Nullable Info getInfo(@NotNull PsiElement element) {
    if (element.getNode() == null || element.getNode().getElementType() != UmsTypes.IDENTIFIER
        || !(element.getContainingFile() instanceof UmsFile file)) {
      return null;
    }
    String word = element.getText();
    if (!word.equals("executable") && !word.equals("test") && !word.equals("command")) {
      return null;
    }
    UmsManifest manifest = UmsManifest.read(file.getViewProvider().getContents());
    int offset = element.getTextRange().getStartOffset();
    Object declaration = manifest.declarationAt(offset);
    String name;
    if (declaration instanceof UmsManifest.Target target && target.start() == offset
        && (target.isExecutable() || target.isTest())) {
      name = (target.isTest() ? "Test '" : "Run '") + target.name() + "'";
    } else if (declaration instanceof UmsManifest.Command command && command.start() == offset) {
      name = "Run 'prismio " + command.name() + "'";
    } else {
      return null;
    }
    return new Info(AllIcons.RunConfigurations.TestState.Run, ExecutorAction.getActions(0), e -> name);
  }
}
