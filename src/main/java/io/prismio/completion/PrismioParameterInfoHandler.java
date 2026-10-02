package io.prismio.completion;

import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.prismio.symbols.FnSig;
import io.prismio.symbols.ProjectSymbols;
import io.prismio.symbols.TypeInference;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameter info — the signature popup over a call's arguments, with the one the
 * caret is in highlighted (Ctrl+P, and on its own after completing a call).
 *
 * <p>The call is found in the tokens, and its overloads the way completion finds
 * a member: through the receiver's inferred type, so {@code s.indexOf(} shows
 * {@code String}'s own overloads from the toolchain's library.
 */
public final class PrismioParameterInfoHandler implements ParameterInfoHandler<PsiElement, PrismioParameterInfoHandler.Shown> {

  /** One overload as the popup shows it. */
  public record Shown(@NotNull FnSig function, boolean asMethod) {}

  @Override
  public @Nullable PsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
    TypeInference.Call call = callAt(context.getFile(), context.getOffset());
    if (call == null) {
      return null;
    }
    context.setItemsToShow(call.overloads().stream()
        .map(fn -> new Shown(fn, call.asMethod() || fn.owner() == null && isMethodCall(context, call)))
        .toArray());
    return context.getFile().findElementAt(call.openParen());
  }

  /** A free function reached as {@code x.f(...)} is shown without the parameter {@code x} fills. */
  private static boolean isMethodCall(@NotNull ParameterInfoContext context, @NotNull TypeInference.Call call) {
    CharSequence text = context.getEditor().getDocument().getCharsSequence();
    int k = call.openParen() - 1;
    while (k >= 0 && Character.isJavaIdentifierPart(text.charAt(k))) {
      k--;
    }
    return k >= 0 && text.charAt(k) == '.';
  }

  @Override
  public void showParameterInfo(@NotNull PsiElement element, @NotNull CreateParameterInfoContext context) {
    context.showHint(element, element.getTextRange().getStartOffset(), this);
  }

  @Override
  public @Nullable PsiElement findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
    TypeInference.Call call = callAt(context.getFile(), context.getOffset());
    if (call == null) {
      return null;
    }
    PsiElement element = context.getFile().findElementAt(call.openParen());
    return element != null && element.equals(context.getParameterOwner()) ? element : null;
  }

  @Override
  public void updateParameterInfo(@NotNull PsiElement element, @NotNull UpdateParameterInfoContext context) {
    TypeInference.Call call = callAt(context.getFile(), context.getOffset());
    context.setCurrentParameter(call == null ? -1 : call.argument());
  }

  @Override
  public void updateUI(Shown shown, @NotNull ParameterInfoUIContext context) {
    List<FnSig.Param> params = shown.function().callParams(shown.asMethod());
    if (params.isEmpty()) {
      context.setupUIComponentPresentation("<no parameters>", -1, -1, false, false, false,
          context.getDefaultParameterColor());
      return;
    }
    StringBuilder text = new StringBuilder();
    int highlightStart = -1;
    int highlightEnd = -1;
    for (int k = 0; k < params.size(); k++) {
      if (k > 0) {
        text.append(", ");
      }
      if (k == context.getCurrentParameterIndex()) {
        highlightStart = text.length();
      }
      text.append(params.get(k));
      if (k == context.getCurrentParameterIndex()) {
        highlightEnd = text.length();
      }
    }
    context.setupUIComponentPresentation(text.toString(), highlightStart, highlightEnd, false, false,
        false, context.getDefaultParameterColor());
  }

  private static @Nullable TypeInference.Call callAt(@NotNull PsiFile file, int offset) {
    PsiFile original = file.getOriginalFile();
    return TypeInference.callAt(original.getViewProvider().getContents(), offset,
        ProjectSymbols.scope(original), ProjectSymbols.summary(original).imports());
  }
}
