package io.prismio.ums;

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

public final class UmsFormattingModelBuilder implements FormattingModelBuilder {
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
    PsiElement element = formattingContext.getPsiElement();
    return FormattingModelProvider.createFormattingModelForPsiFile(
        element.getContainingFile(),
        new UmsBlock(element.getNode(), Wrap.createWrap(WrapType.NONE, false),
            Alignment.createAlignment(), settings),
        settings);
  }
}
