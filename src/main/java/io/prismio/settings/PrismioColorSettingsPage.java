package io.prismio.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import io.prismio.highlighter.PrismioSyntaxHighlighter;
import io.prismio.icons.PrismioIcons;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Comprehensive color settings page for Prismio
 * Allows users to customize all syntax highlighting colors
 */
public final class PrismioColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[] {// Keywords
      new AttributesDescriptor("Keywords//Keyword", PrismioSyntaxHighlighter.KEYWORD),
      new AttributesDescriptor("Keywords//Type Keyword", PrismioSyntaxHighlighter.TYPE_KEYWORD),

      // Literals
      new AttributesDescriptor("Literals//String", PrismioSyntaxHighlighter.STRING),
      new AttributesDescriptor("Literals//Number", PrismioSyntaxHighlighter.NUMBER),
      new AttributesDescriptor("Literals//Boolean", PrismioSyntaxHighlighter.BOOLEAN),
      new AttributesDescriptor("Literals//Character", PrismioSyntaxHighlighter.CHARACTER),

      // Identifiers
      new AttributesDescriptor("Identifiers//Identifier", PrismioSyntaxHighlighter.IDENTIFIER),
      new AttributesDescriptor(
          "Identifiers//Function Declaration", PrismioSyntaxHighlighter.FUNCTION_DECLARATION),
      new AttributesDescriptor(
          "Identifiers//Function Call", PrismioSyntaxHighlighter.FUNCTION_CALL),
      new AttributesDescriptor("Identifiers//Method Call", PrismioSyntaxHighlighter.METHOD_CALL),
      new AttributesDescriptor("Identifiers//Parameter", PrismioSyntaxHighlighter.PARAMETER),
      new AttributesDescriptor(
          "Identifiers//Local Variable", PrismioSyntaxHighlighter.LOCAL_VARIABLE),
      new AttributesDescriptor(
          "Identifiers//Mutable Variable", PrismioSyntaxHighlighter.MUTABLE_VARIABLE),
      new AttributesDescriptor("Identifiers//Constant", PrismioSyntaxHighlighter.CONSTANT),
      new AttributesDescriptor("Identifiers//Field", PrismioSyntaxHighlighter.FIELD),
      new AttributesDescriptor("Identifiers//Struct Name", PrismioSyntaxHighlighter.STRUCT_NAME),
      new AttributesDescriptor("Identifiers//Enum Name", PrismioSyntaxHighlighter.ENUM_NAME),
      new AttributesDescriptor("Identifiers//Trait Name", PrismioSyntaxHighlighter.TRAIT_NAME),
      new AttributesDescriptor("Identifiers//Enum Variant", PrismioSyntaxHighlighter.ENUM_VARIANT),
      new AttributesDescriptor(
          "Identifiers//Type Reference", PrismioSyntaxHighlighter.TYPE_REFERENCE),
      new AttributesDescriptor("Identifiers//Import Path", PrismioSyntaxHighlighter.IMPORT_PATH),

      // Operators and Separators
      new AttributesDescriptor(
          "Operators and Separators//Operator", PrismioSyntaxHighlighter.OPERATOR),
      new AttributesDescriptor("Operators and Separators//Separator (Braces, Brackets)",
          PrismioSyntaxHighlighter.SEPARATOR),
      new AttributesDescriptor("Operators and Separators//Comma", PrismioSyntaxHighlighter.COMMA),
      new AttributesDescriptor(
          "Operators and Separators//Semicolon", PrismioSyntaxHighlighter.SEMICOLON),
      new AttributesDescriptor("Operators and Separators//Dot", PrismioSyntaxHighlighter.DOT),

      // Comments
      new AttributesDescriptor("Comments//Line Comment", PrismioSyntaxHighlighter.LINE_COMMENT),
      new AttributesDescriptor("Comments//Block Comment", PrismioSyntaxHighlighter.BLOCK_COMMENT),

      // Special
      new AttributesDescriptor("Special//Bad Character", PrismioSyntaxHighlighter.BAD_CHARACTER)};

  @Nullable
  @Override
  public Icon getIcon() {
    return PrismioIcons.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new PrismioSyntaxHighlighter();
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
            
            import <importPath>prismio.io</importPath>
            import prismio.collections
            
            // External function declarations
            extern fn println(msg: String)
            extern fn println_int(value: Int)
            extern fn str_length(s: String) -> Int
            
            // Struct definition
            struct <structName>Point</structName> {
                <field>x</field>: Int,
                y: Int
            }
            
            struct Person {
                name: String,
                age: Int,
                active: Bool
            }
            
            // Enum definition
            enum <enumName>Direction</enumName> {
                <enumVariant>North</enumVariant>,
                South,
                East,
                West
            }
            
            enum Result {
                Success,
                Error
            }
            
            // Trait definition
            trait <traitName>Drawable</traitName> {
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
            let <constant>MAX_SIZE</constant> = 1000
            let PI = 3.14159
            let GREETING = "Hello, Prismio!"
            
            // Function with parameters and return type
            fn <functionDeclaration>calculate</functionDeclaration>(<parameter>a</parameter>: Int, b: Int, op: Char) -> Int {
                let mut <mutableVariable>result</mutableVariable> = 0
                
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
                
                let <localVariable>first</localVariable> = numbers[0]
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
                let result = <functionCall>calculate</functionCall>(10, 5, '+')
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
                let distance = point.<methodCall>distance</methodCall>()
                
                // Enum usage
                let dir = Direction::North
                
                println("Program completed successfully!")
            }
            """;
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return Map.ofEntries(
        Map.entry("functionDeclaration", PrismioSyntaxHighlighter.FUNCTION_DECLARATION),
        Map.entry("functionCall", PrismioSyntaxHighlighter.FUNCTION_CALL),
        Map.entry("methodCall", PrismioSyntaxHighlighter.METHOD_CALL),
        Map.entry("parameter", PrismioSyntaxHighlighter.PARAMETER),
        Map.entry("localVariable", PrismioSyntaxHighlighter.LOCAL_VARIABLE),
        Map.entry("mutableVariable", PrismioSyntaxHighlighter.MUTABLE_VARIABLE),
        Map.entry("constant", PrismioSyntaxHighlighter.CONSTANT),
        Map.entry("field", PrismioSyntaxHighlighter.FIELD),
        Map.entry("structName", PrismioSyntaxHighlighter.STRUCT_NAME),
        Map.entry("enumName", PrismioSyntaxHighlighter.ENUM_NAME),
        Map.entry("traitName", PrismioSyntaxHighlighter.TRAIT_NAME),
        Map.entry("enumVariant", PrismioSyntaxHighlighter.ENUM_VARIANT),
        Map.entry("typeReference", PrismioSyntaxHighlighter.TYPE_REFERENCE),
        Map.entry("importPath", PrismioSyntaxHighlighter.IMPORT_PATH));
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
