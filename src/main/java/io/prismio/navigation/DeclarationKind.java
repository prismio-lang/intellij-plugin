package io.prismio.navigation;

import com.intellij.icons.AllIcons;
import javax.swing.Icon;

public enum DeclarationKind {
  FUNCTION("function", AllIcons.Nodes.Function),
  STRUCT("struct", AllIcons.Nodes.Class),
  ENUM("enum", AllIcons.Nodes.Enum),
  TRAIT("trait", AllIcons.Nodes.Interface),
  IMPLEMENTATION("implementation", AllIcons.Nodes.AbstractClass),
  CONSTANT("global binding", AllIcons.Nodes.Constant);

  private final String displayName;
  private final Icon icon;

  DeclarationKind(String displayName, Icon icon) {
    this.displayName = displayName;
    this.icon = icon;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Icon getIcon() {
    return icon;
  }

  static DeclarationKind fromKeyword(String keyword, int braceDepth) {
    return switch (keyword) {
      case "fn" -> FUNCTION;
      case "struct" -> braceDepth == 0 ? STRUCT : null;
      case "enum" -> braceDepth == 0 ? ENUM : null;
      case "trait" -> braceDepth == 0 ? TRAIT : null;
      case "impl" -> braceDepth == 0 ? IMPLEMENTATION : null;
      case "let" -> braceDepth == 0 ? CONSTANT : null;
      default -> null;
    };
  }
}
