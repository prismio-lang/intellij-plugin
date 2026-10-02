package io.prismio;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Rename follows from the reference model: the platform renames a declaration by
 * rewriting the references that resolve to it, so this could not work before
 * identifiers carried references.
 */
public class PrismioRenameTest extends BasePlatformTestCase {

  public void testRenamingADeclarationRewritesItsCallSites() {
    myFixture.configureByText(PrismioFileType.INSTANCE, """
        fn help<caret>er(value: Int) -> Int { return value }

        fn main() -> Int { return helper(1) + helper(2) }
        """);
    myFixture.renameElementAtCaret("renamed");
    myFixture.checkResult("""
        fn renamed(value: Int) -> Int { return value }

        fn main() -> Int { return renamed(1) + renamed(2) }
        """);
  }

  public void testRenamingDoesNotTouchAnUnrelatedName() {
    myFixture.configureByText(PrismioFileType.INSTANCE, """
        fn help<caret>er(value: Int) -> Int { return value }

        fn other() -> Int { return helperish(1) }
        """);
    myFixture.renameElementAtCaret("renamed");
    myFixture.checkResult("""
        fn renamed(value: Int) -> Int { return value }

        fn other() -> Int { return helperish(1) }
        """);
  }
}
