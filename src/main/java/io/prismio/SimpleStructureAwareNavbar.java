package io.prismio;

import com.intellij.icons.AllIcons;
import com.intellij.ide.navigationToolbar.AbstractNavBarModelExtension;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioProperty;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Navigation bar model extension for Prismio.
 * Shows file and property names in the IDE navigation bar.
 */
final class SimpleStructureAwareNavbar extends AbstractNavBarModelExtension {

  @Override
  public @Nullable String getPresentableText(Object object) {
    if (object instanceof PrismioFile) {
      return ((PrismioFile) object).getName();
    }
    if (object instanceof PrismioProperty) {
      return ((PrismioProperty) object).getName();
    }
    return null;
  }

  @Override
  @Nullable
  public Icon getIcon(Object object) {
    if (object instanceof PrismioProperty) {
      return AllIcons.Nodes.Property;
    }
    return null;
  }

}
