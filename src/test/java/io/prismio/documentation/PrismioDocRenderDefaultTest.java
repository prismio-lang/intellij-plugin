package io.prismio.documentation;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class PrismioDocRenderDefaultTest extends BasePlatformTestCase {

  private boolean before;
  private boolean applied;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    before = EditorSettingsExternalizable.getInstance().isDocCommentRenderingEnabled();
    applied = PropertiesComponent.getInstance().getBoolean(PrismioDocRenderDefault.APPLIED);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      EditorSettingsExternalizable.getInstance().setDocCommentRenderingEnabled(before);
      PropertiesComponent.getInstance().setValue(PrismioDocRenderDefault.APPLIED, applied);
    } finally {
      super.tearDown();
    }
  }

  /** On the first run the setting is turned on; after the user turns it off, it stays off. */
  public void testAppliedOnceAndNeverAgain() {
    PropertiesComponent properties = PropertiesComponent.getInstance();
    EditorSettingsExternalizable settings = EditorSettingsExternalizable.getInstance();
    properties.setValue(PrismioDocRenderDefault.APPLIED, false);
    settings.setDocCommentRenderingEnabled(false);

    PrismioDocRenderDefault.apply(properties, settings);
    assertTrue(settings.isDocCommentRenderingEnabled());

    settings.setDocCommentRenderingEnabled(false);
    PrismioDocRenderDefault.apply(properties, settings);
    assertFalse("the user's choice is respected after the first run", settings.isDocCommentRenderingEnabled());
  }
}
