package io.prismio.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import io.prismio.highlighter.PsSyntaxHighlighter;
import io.prismio.utils.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Comprehensive color settings page for Prismio
 * Allows users to customize all syntax highlighting colors
 */
public class PrismioColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            // Keywords
            new AttributesDescriptor("Keywords//Keyword", PsSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Keywords//Type Keyword", PsSyntaxHighlighter.TYPE_KEYWORD),
            
            // Literals
            new AttributesDescriptor("Literals//String", PsSyntaxHighlighter.STRING),
            new AttributesDescriptor("Literals//Number", PsSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Literals//Boolean", PsSyntaxHighlighter.BOOLEAN),
            new AttributesDescriptor("Literals//Character", PsSyntaxHighlighter.CHARACTER),
            
            // Identifiers
            new AttributesDescriptor("Identifiers//Identifier", PsSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Identifiers//Function Declaration", PsSyntaxHighlighter.FUNCTION_DECLARATION),
            new AttributesDescriptor("Identifiers//Function Call", PsSyntaxHighlighter.FUNCTION_CALL),
            new AttributesDescriptor("Identifiers//Parameter", PsSyntaxHighlighter.PARAMETER),
            new AttributesDescriptor("Identifiers//Struct Name", PsSyntaxHighlighter.STRUCT_NAME),
            new AttributesDescriptor("Identifiers//Enum Name", PsSyntaxHighlighter.ENUM_NAME),
            
            // Operators and Separators
            new AttributesDescriptor("Operators and Separators//Operator", PsSyntaxHighlighter.OPERATOR),
            new AttributesDescriptor("Operators and Separators//Separator (Braces, Brackets)", PsSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor("Operators and Separators//Comma", PsSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Operators and Separators//Semicolon", PsSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor("Operators and Separators//Dot", PsSyntaxHighlighter.DOT),
            
            // Comments
            new AttributesDescriptor("Comments//Line Comment", PsSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Comments//Block Comment", PsSyntaxHighlighter.BLOCK_COMMENT),
            
            // Special
            new AttributesDescriptor("Special//Bad Character", PsSyntaxHighlighter.BAD_CHARACTER)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new PsSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return """
            // Prismio Language Demo - Complete Syntax Showcase
            /* 
             * Multi-line comment demonstrating
             * comprehensive syntax highlighting
             */
            
            import prismio.io
            import prismio.collections
            
            // External function declarations
            extern fn println(msg: String)
            extern fn println_int(value: Int)
            extern fn str_length(s: String) -> Int
            
            // Struct definition
            struct Point {
                x: Int,
                y: Int
            }
            
            struct Person {
                name: String,
                age: Int,
                active: Bool
            }
            
            // Enum definition
            enum Direction {
                North,
                South,
                East,
                West
            }
            
            enum Result {
                Success,
                Error
            }
            
            // Trait definition
            trait Drawable {
                fn draw()
            }
            
            // Implementation
            impl Point {
                fn distance(self) -> Int {
                    return self.x + self.y
                }
                
                fn new(x: Int, y: Int) -> Point {
                    return Point { x: x, y: y }
                }
            }
            
            // Global constants
            let MAX_SIZE = 1000
            let PI = 3.14159
            let GREETING = "Hello, Prismio!"
            
            // Function with parameters and return type
            fn calculate(a: Int, b: Int, op: Char) -> Int {
                let mut result = 0
                
                if (op == '+') {
                    result = a + b
                } else {
                    if (op == '-') {
                        result = a - b
                    } else {
                        if (op == '*') {
                            result = a * b
                        } else {
                            result = a / b
                        }
                    }
                }
                
                return result
            }
            
            // Recursive function
            fn fibonacci(n: Int) -> Int {
                if (n <= 1) {
                    return n
                } else {
                    let a = fibonacci(n - 1)
                    let b = fibonacci(n - 2)
                    return a + b
                }
            }
            
            // Function with loops
            fn sum_to_n(n: Int) -> Int {
                let mut sum = 0
                let mut i = 1
                
                while (i <= n) {
                    sum = sum + i
                    i = i + 1
                }
                
                return sum
            }
            
            // Function with arrays
            fn process_array() {
                let numbers: [Int] = [1, 2, 3, 4, 5]
                let matrix: [[Int]] = [[1, 2], [3, 4]]
                
                let first = numbers[0]
                let value = matrix[0][1]
                
                println_int(first)
                println_int(value)
            }
            
            // Match expression
            fn classify(value: Int) -> String {
                match value {
                    0 => "zero",
                    1 => "one",
                    _ => "other"
                }
            }
            
            // Main entry point
            fn main() {
                // Variable declarations
                let x = 42
                let y = 3.14
                let name = "Alice"
                let flag = true
                let ch = 'A'
                
                // Mutable variables
                let mut counter = 0
                let mut sum = 0
                
                // Function calls
                let result = calculate(10, 5, '+')
                let fib = fibonacci(10)
                let total = sum_to_n(100)
                
                // String operations
                let greeting = "Hello, World!"
                let len = str_length(greeting)
                
                // Output
                println(greeting)
                println_int(result)
                println_int(fib)
                
                // Control flow
                if (counter < 10) {
                    counter = counter + 1
                }
                
                while (sum < 100) {
                    sum = sum + counter
                    counter = counter + 1
                }
                
                // For loop
                for (i in range) {
                    println_int(i)
                }
                
                // Infinite loop with break
                loop {
                    if (counter > 20) {
                        break
                    }
                    counter = counter + 1
                }
                
                // Create struct instance
                let point = Point { x: 10, y: 20 }
                let distance = point.distance()
                
                // Enum usage
                let dir = Direction::North
                
                println("Program completed successfully!")
            }
            """;
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Prismio";
    }
}