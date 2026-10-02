package io.prismio.ums;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;

public final class UmsQuoteHandler extends SimpleTokenSetQuoteHandler {
  public UmsQuoteHandler() {
    super(UmsTypes.STRING);
  }
}
