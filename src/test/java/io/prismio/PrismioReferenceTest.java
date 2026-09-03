package io.prismio;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usageView.UsageInfo;
import java.util.Collection;

/**
 * References are what Find Usages and highlight-usages-under-caret are built on.
 *
 * <p>Both silently reported nothing before there was a reference to search for,
 * which is the failure mode this file exists to prevent: a feature that is
 * registered, appears in the menu, and answers "no usages" for every symbol.
 */
public class PrismioReferenceTest extends BasePlatformTestCase {

  private PsiReference referenceAtCaret(String source) {
    myFixture.configureByText(PrismioFileType.INSTANCE, source);
    return myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
  }

  public void testANameResolvesToItsDeclaration() {
    PsiReference reference = referenceAtCaret("""
        fn helper(value: Int) -> Int { return value }

        fn main() -> Int { return hel<caret>per(1) }
        """);
    assertNotNull("an identifier should carry a reference", reference);
    PsiElement resolved = reference.resolve();
    assertNotNull("the reference should resolve", resolved);
    assertEquals("helper", resolved.getText());
    assertTrue("it should resolve to the declaration, not the call",
        resolved.getTextRange().getStartOffset() < myFixture.getCaretOffset());
  }

  public void testADeclarationIsNotAReferenceToItself() {
    // Otherwise Find Usages lists the declaration among its own usages.
    assertNull(referenceAtCaret("fn help<caret>er(value: Int) -> Int { return value }"));
  }

  public void testFindUsagesFindsTheCallSites() {
    myFixture.configureByText(PrismioFileType.INSTANCE, """
        fn helper(value: Int) -> Int { return value }

        fn main() -> Int { return helper(1) + helper(2) }
        """);
    PsiElement declaration =
        myFixture.getFile().findElementAt(myFixture.getFile().getText().indexOf("helper"));
    assertNotNull(declaration);

    Collection<UsageInfo> usages = myFixture.findUsages(declaration);
    assertEquals("both call sites should be found, and the declaration should not be",
        2, usages.size());
  }

  public void testResolutionCrossesFiles() {
    myFixture.addFileToProject("helpers.psm",
        "fn shared(value: Int) -> Int { return value }\n");
    PsiReference reference = referenceAtCaret("""
        fn main() -> Int { return sha<caret>red(1) }
        """);
    assertNotNull(reference);
    PsiElement resolved = reference.resolve();
    assertNotNull("a name declared in another file should resolve", resolved);
    assertEquals("helpers.psm", resolved.getContainingFile().getName());
  }

  public void testUnknownNamesResolveToNothingWithoutErroring() {
    PsiReference reference = referenceAtCaret("""
        fn main() -> Int { return absent<caret>Name(1) }
        """);
    assertNotNull("the reference exists even when it does not resolve", reference);
    assertNull(reference.resolve());
    assertTrue("an unresolved name must be soft, or the IDE reports it as an error",
        reference.isSoft());
  }
}
