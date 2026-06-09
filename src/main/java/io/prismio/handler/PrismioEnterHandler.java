package io.prismio.handler;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import io.prismio.psi.PrismioFile;
import org.jetbrains.annotations.NotNull;

/**
 * Enter handler for Prismio to provide proper indentation when pressing Enter.
 * Handles indentation inside braces with proper closing brace alignment.
 */
public class PrismioEnterHandler extends EnterHandlerDelegateAdapter {

    @Override
    public Result postProcessEnter(
            @NotNull PsiFile file,
            @NotNull Editor editor,
            @NotNull DataContext dataContext) {

        if (!(file instanceof PrismioFile)) {
            return Result.Continue;
        }

        Document document = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();
        CharSequence text = document.getCharsSequence();

        // Get current line number (where caret is after Enter)
        int currentLineNumber = document.getLineNumber(caretOffset);
        if (currentLineNumber == 0) {
            return Result.Continue;
        }

        // Get previous line info
        int prevLineNumber = currentLineNumber - 1;
        int prevLineStart = document.getLineStartOffset(prevLineNumber);
        int prevLineEnd = document.getLineEndOffset(prevLineNumber);
        String prevLine = text.subSequence(prevLineStart, prevLineEnd).toString();
        String trimmedPrevLine = prevLine.trim();

        // Calculate previous line's indentation
        String prevIndent = getIndentation(prevLine);

        // Get current line info
        int currentLineStart = document.getLineStartOffset(currentLineNumber);
        int currentLineEnd = document.getLineEndOffset(currentLineNumber);
        String currentLine = currentLineEnd > currentLineStart
                ? text.subSequence(currentLineStart, currentLineEnd).toString()
                : "";
        String trimmedCurrentLine = currentLine.trim();

        // Check if previous line ends with { (need to increase indent)
        if (trimmedPrevLine.endsWith("{")) {
            String newIndent = prevIndent + "    "; // 4 spaces

            // Check if current line starts with } (we pressed Enter between { and })
            if (trimmedCurrentLine.startsWith("}")) {
                // Insert the indented cursor line, then newline with closing brace at correct
                // indent
                // Current state: cursor is at start of line with }
                // We need: cursor on indented line, } on next line with original indent

                // Find where } starts on current line
                int bracePos = currentLine.indexOf('}');
                if (bracePos >= 0) {
                    // Replace from line start to after brace with: indent + newline + prevIndent +
                    // }
                    String replacement = newIndent + "\n" + prevIndent + "}";
                    int replaceEnd = currentLineStart + bracePos + 1;

                    document.replaceString(currentLineStart, replaceEnd, replacement);
                    // Move caret to end of indented line (before the newline)
                    editor.getCaretModel().moveToOffset(currentLineStart + newIndent.length());

                    return Result.Stop;
                }
            } else {
                // Just add indentation to current line
                document.insertString(currentLineStart, newIndent);
                editor.getCaretModel().moveToOffset(currentLineStart + newIndent.length());
                return Result.Stop;
            }
        }

        // Check if current line starts with } (dedent for closing brace)
        if (trimmedCurrentLine.startsWith("}")) {
            // Closing brace should have one less indent level than previous line content
            String dedentedIndent = dedent(prevIndent);

            // Replace current line's indentation
            int contentStart = currentLineStart;
            for (int i = currentLineStart; i < currentLineEnd && i < text.length(); i++) {
                char c = text.charAt(i);
                if (c != ' ' && c != '\t') {
                    contentStart = i;
                    break;
                }
            }

            if (contentStart > currentLineStart) {
                document.replaceString(currentLineStart, contentStart, dedentedIndent);
            } else {
                document.insertString(currentLineStart, dedentedIndent);
            }
            editor.getCaretModel().moveToOffset(currentLineStart + dedentedIndent.length());
            return Result.Stop;
        }

        // Maintain current indentation for other cases
        if (!prevIndent.isEmpty()) {
            document.insertString(currentLineStart, prevIndent);
            editor.getCaretModel().moveToOffset(currentLineStart + prevIndent.length());
            return Result.Default;
        }

        return Result.Continue;
    }

    private String getIndentation(String line) {
        StringBuilder indent = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indent.append(c);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    private String dedent(String indent) {
        // Remove 4 spaces or 1 tab from indent
        if (indent.endsWith("    ")) {
            return indent.substring(0, indent.length() - 4);
        } else if (indent.endsWith("\t")) {
            return indent.substring(0, indent.length() - 1);
        }
        return indent;
    }
}
