package io.prismio.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public final class PrismioFormattingModelBuilder implements FormattingModelBuilder {
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    final CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();
    final PsiElement element = formattingContext.getPsiElement();

    return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(),
        new PrismioBlock(element.getNode(), Wrap.createWrap(WrapType.NONE, false),
            Alignment.createAlignment(), codeStyleSettings),
        codeStyleSettings);
  }
}
