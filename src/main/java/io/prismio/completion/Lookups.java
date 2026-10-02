package io.prismio.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import io.prismio.icons.PrismioIcons;
import io.prismio.imports.PrismioImports;
import io.prismio.symbols.FnSig;
import io.prismio.symbols.Member;
import io.prismio.symbols.ModuleSummary;
import io.prismio.symbols.TypeDecl;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

/**
 * How each kind of completion looks in the popup and what choosing it writes.
 *
 * <p>The layout follows the JetBrains language plugins: name, then the
 * parameters in grey, then — for a declaration from a module the file does not
 * import yet — that module in parentheses, and the result type on the right.
 * Choosing an item from an unimported module writes the import too.
 */
final class Lookups {

  static final String DUMMY = CompletionUtil.DUMMY_IDENTIFIER_TRIMMED;

  static final InsertHandler<LookupElement> ANGLE_BRACKETS = (context, item) -> {
    context.getDocument().insertString(context.getTailOffset(), "<>");
    context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() - 1);
  };

  private Lookups() {}

  /** One entry for a function and its overloads with the same name in one module. */
  static @NotNull LookupElement function(@NotNull List<FnSig> overloads, @NotNull String importModule,
      boolean library, double priority) {
    FnSig first = overloads.get(0);
    Set<String> shapes = new LinkedHashSet<>();
    boolean anyParams = false;
    for (FnSig fn : overloads) {
      shapes.add(fn.parameterText(false));
      anyParams |= !fn.params().isEmpty();
    }
    String tail = first.parameterText(false);
    if (shapes.size() > 1) {
      tail += "  +" + (shapes.size() - 1) + (shapes.size() == 2 ? " overload" : " overloads");
    }
    LookupElementBuilder builder = LookupElementBuilder.create(first, first.name())
        .withIcon(AllIcons.Nodes.Function)
        .withTailText(tail, true)
        .withTypeText(first.returnType() == null ? null : first.returnType().toString())
        .withInsertHandler(new CallInsertHandler(true, anyParams, importModule));
    if (!importModule.isEmpty()) {
      builder = builder.appendTailText("  (" + importModule + ")", true);
    } else if (library) {
      builder = builder.withTypeText(first.returnType() == null ? first.module() : first.returnType().toString());
    }
    return PrioritizedLookupElement.withPriority(builder, priority);
  }

  static @NotNull LookupElement member(@NotNull Member member, @NotNull String importModule) {
    FnSig fn = member.function();
    boolean call = member.isCall();
    boolean asMethod = member.origin() == Member.Origin.EXTENSION;
    LookupElementBuilder builder = LookupElementBuilder.create(fn != null ? fn : member, member.name())
        .withIcon(icon(member.kind()))
        .withTypeText(member.type() == null ? null : member.type().toString())
        .withBoldness(member.origin() == Member.Origin.OWN || member.origin() == Member.Origin.BUILT_IN)
        .withInsertHandler(new CallInsertHandler(call,
            call && fn != null && !fn.callParams(asMethod).isEmpty(), importModule));
    if (call && fn != null) {
      builder = builder.withTailText(fn.parameterText(asMethod), true);
    }
    if (!importModule.isEmpty()) {
      builder = builder.appendTailText("  (" + importModule + ")", true);
    }
    double priority = switch (member.kind()) {
      case FIELD -> 100;
      case PROPERTY, VARIANT, CONSTANT -> 95;
      default -> switch (member.origin()) {
        case OWN, BUILT_IN -> 90;
        case TRAIT -> 60;
        case EXTENSION -> 50;
      };
    };
    return PrioritizedLookupElement.withPriority(builder, priority);
  }

  static @NotNull LookupElement type(@NotNull TypeDecl type, @NotNull String importModule, double priority) {
    LookupElementBuilder builder = LookupElementBuilder.create(type, type.name())
        .withIcon(icon(type))
        .withTypeText(type.kind().name().toLowerCase())
        .withInsertHandler(new CallInsertHandler(false, false, importModule));
    if (!type.typeParameters().isEmpty()) {
      builder = builder.withTailText("<" + String.join(", ", type.typeParameters()) + ">", true);
    }
    if (!importModule.isEmpty()) {
      builder = builder.appendTailText("  (" + importModule + ")", true);
    }
    return PrioritizedLookupElement.withPriority(builder, priority);
  }

  static @NotNull LookupElement global(@NotNull ModuleSummary.Global global, @NotNull String importModule,
      double priority) {
    LookupElementBuilder builder = LookupElementBuilder.create(global, global.name())
        .withIcon(AllIcons.Nodes.Variable)
        .withTypeText(global.type() == null ? null : global.type().toString())
        .withInsertHandler(new CallInsertHandler(false, false, importModule));
    if (!importModule.isEmpty()) {
      builder = builder.appendTailText("  (" + importModule + ")", true);
    }
    return PrioritizedLookupElement.withPriority(builder, priority);
  }

  /** A module after {@code import}, described by the first sentence of its opening comment. */
  static @NotNull LookupElement module(@NotNull ModuleSummary module, @NotNull String text, boolean imported) {
    String leaf = module.name().substring(module.name().lastIndexOf('.') + 1);
    LookupElementBuilder builder = LookupElementBuilder.create(module, text)
        .withLookupString(leaf)
        .withIcon(PrismioIcons.FILE)
        .withTailText(summaryLine(module.doc()), true)
        .withTypeText(imported ? "imported" : "std");
    return PrioritizedLookupElement.withPriority(builder, imported ? 10 : 50);
  }

  static @NotNull LookupElement stdPackage(boolean completeWithDot) {
    LookupElementBuilder builder = LookupElementBuilder.create("std")
        .withIcon(AllIcons.Nodes.Package)
        .withTypeText("standard library")
        .bold();
    if (completeWithDot) {
      builder = builder.withInsertHandler((context, item) -> {
        int tail = context.getTailOffset();
        CharSequence chars = context.getDocument().getCharsSequence();
        if (tail >= chars.length() || chars.charAt(tail) != '.') {
          context.getDocument().insertString(tail, ".");
        }
        context.getEditor().getCaretModel().moveToOffset(tail + 1);
        AutoPopupController.getInstance(context.getProject()).scheduleAutoPopup(context.getEditor());
      });
    }
    return PrioritizedLookupElement.withPriority(builder, 60);
  }

  static @NotNull Icon icon(@NotNull TypeDecl type) {
    return switch (type.kind()) {
      case STRUCT -> AllIcons.Nodes.Class;
      case ENUM -> AllIcons.Nodes.Enum;
      case TRAIT -> AllIcons.Nodes.Interface;
    };
  }

  private static @NotNull Icon icon(@NotNull Member.Kind kind) {
    return switch (kind) {
      case FIELD -> AllIcons.Nodes.Field;
      case PROPERTY -> AllIcons.Nodes.Property;
      case METHOD, STATIC_METHOD -> AllIcons.Nodes.Method;
      case CONSTANT, VARIANT -> AllIcons.Nodes.Constant;
    };
  }

  /** The first sentence of a module's description, short enough for the popup's grey tail. */
  static @NotNull String summaryLine(@NotNull String doc) {
    if (doc.isBlank()) {
      return "";
    }
    String line = doc.strip().split("\n", 2)[0];
    int dash = line.indexOf(" -- ");
    if (dash > 0 && dash < line.length() - 4) {
      line = line.substring(dash + 4);
    }
    if (line.length() > 60) {
      line = line.substring(0, 57) + "...";
    }
    return "  " + line;
  }

  /**
   * Writes the call's parentheses — caret between them when the function takes
   * arguments, after them when it does not — and the import it needs.
   */
  static final class CallInsertHandler implements InsertHandler<LookupElement> {
    private final boolean parens;
    private final boolean hasParams;
    private final String importModule;

    CallInsertHandler(boolean parens, boolean hasParams, @NotNull String importModule) {
      this.parens = parens;
      this.hasParams = hasParams;
      this.importModule = importModule;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      Document document = context.getDocument();
      if (parens) {
        int tail = context.getTailOffset();
        CharSequence chars = document.getCharsSequence();
        if (tail < chars.length() && chars.charAt(tail) == '(') {
          context.getEditor().getCaretModel().moveToOffset(tail + 1);
        } else {
          document.insertString(tail, "()");
          context.getEditor().getCaretModel().moveToOffset(hasParams ? tail + 1 : tail + 2);
        }
        if (hasParams) {
          AutoPopupController.getInstance(context.getProject())
              .autoPopupParameterInfo(context.getEditor(), null);
        }
      }
      if (!importModule.isEmpty()) {
        PrismioImports.addImport(document, importModule);
      }
    }
  }
}
