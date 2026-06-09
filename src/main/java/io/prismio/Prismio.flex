package io.prismio;

import com.intellij.psi.tree.IElementType;
import psi.io.prismio.PrismioTypes;
import com.intellij.psi.TokenType;

%%

%public
%class PsLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

CRLF=\R
WHITE_SPACE=[\ \n\t\f\r]
SINGLE_LINE_COMMENT=("//")[^\r\n]*
MULTILINE_COMMENT="/*" ([^*]|"*"+[^*/])* "*"+ "/"

STRING_LITERAL=\"([^\"\\]|\\.)*\"
CHARACTER_LITERAL='([^'\\]|\\.)'
INTEGER=-?[0-9]+(_[0-9]+)*
FLOAT=-?[0-9]*\.[0-9]+([eE][+-]?[0-9]+)?
BOOLEAN="true"|"false"

KEYWORD="fn"|"let"|"mut"|"if"|"else"|"while"|"for"|"return"|"struct"|"enum"|"trait"|"impl"|"extern"|"import"|"in"|"loop"|"match"|"break"|"continue"
TYPE_KEYWORD="Int"|"Bool"|"Char"|"String"|"Float"

IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*

%%

<YYINITIAL> {
  {SINGLE_LINE_COMMENT}     { return PrismioTypes.SINGLE_LINE_COMMENT; }
  {MULTILINE_COMMENT}       { return PrismioTypes.MULTILINE_COMMENT; }

  {KEYWORD}                 { return PrismioTypes.KEYWORD; }
  {TYPE_KEYWORD}            { return PrismioTypes.TYPE_KEYWORD; }
  {BOOLEAN}                 { return PrismioTypes.BOOLEAN; }

  {STRING_LITERAL}          { return PrismioTypes.STRING_LITERAL; }
  {CHARACTER_LITERAL}       { return PrismioTypes.CHARACTER_LITERAL; }
  {FLOAT}                   { return PrismioTypes.FLOAT; }
  {INTEGER}                 { return PrismioTypes.INTEGER; }

  // Specific operators
  "->"                      { return PrismioTypes.ARROW; }
  "=>"                      { return PrismioTypes.FAT_ARROW; }
  "<="|">="|"=="|"!="       { return PrismioTypes.RELATIONAL_OP; }
  "+="|"-="|"*="|"/="|"%="  { return PrismioTypes.ASSIGNMENT_OP; }
  "++"|"--"                 { return PrismioTypes.UNARY_OP; }
  "&&"|"||"                 { return PrismioTypes.LOGICAL_OP; }
  [+\-*/%]                  { return PrismioTypes.ARITHMETIC_OP; }
  [<>=!]                    { return PrismioTypes.COMPARISON; }
  [&|]                      { return PrismioTypes.BITWISE; }

  // Specific separators
  "("                       { return PrismioTypes.LPAREN; }
  ")"                       { return PrismioTypes.RPAREN; }
  "{"                       { return PrismioTypes.LBRACE; }
  "}"                       { return PrismioTypes.RBRACE; }
  "["                       { return PrismioTypes.LBRACKET; }
  "]"                       { return PrismioTypes.RBRACKET; }
  ","                       { return PrismioTypes.COMMA; }
  ":"                       { return PrismioTypes.COLON; }
  "."                       { return PrismioTypes.DOT; }
  ";"                       { return PrismioTypes.SEMICOLON; }

  {IDENTIFIER}              { return PrismioTypes.IDENTIFIER; }

  {WHITE_SPACE}             { return TokenType.WHITE_SPACE; }
}

[^]                         { return TokenType.BAD_CHARACTER; }