package io.prismio;

import com.intellij.formatting.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import io.prismio.formatter.PrismioBlock;
import org.jetbrains.annotations.NotNull;

final class PrismioFormattingModelBuilder implements FormattingModelBuilder {

  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    final CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();
    final PsiElement element = formattingContext.getPsiElement();

    return FormattingModelProvider.createFormattingModelForPsiFile(
            element.getContainingFile(),
            new PrismioBlock(
                    element.getNode(),
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    codeStyleSettings
            ),
            codeStyleSettings
    );
  }
}