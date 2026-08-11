package io.prismio;

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class PrismioParsingTest extends BasePlatformTestCase {
  public void testParsesCorePrismioSyntaxWithoutErrors() {
    myFixture.configureByText(
        PrismioFileType.INSTANCE, "fn add(left: Int, right: Int) -> Int { return left + right }");

    assertEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(), PsiErrorElement.class));
  }

  public void testParsesCommentsCollectionsAndControlFlowWithoutErrors() {
    myFixture.configureByText(PrismioFileType.INSTANCE,
        "/* values */ fn main() { let values = [1, 2, 3]; if true { // ok\n return } }");

    assertEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(), PsiErrorElement.class));
  }
}
