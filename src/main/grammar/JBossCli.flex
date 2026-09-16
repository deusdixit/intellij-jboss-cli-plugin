package dev.jbosscli.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import static dev.jbosscli.psi.JBossCliTypes.*;

%%
%public
%class _JBossCliLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%state CONTINUED
%{
  // Consume arbitrarily nested property expressions as one restartable token.
  private IElementType expression() {
    int end = zzMarkedPos;
    int depth = 1;
    while (end < zzEndRead) {
      char c = zzBuffer.charAt(end);
      if (c == '\r' || c == '\n') break;
      end++;
      if (c == '\\' && end < zzEndRead) { end++; continue; }
      if (c == '{') depth++;
      if (c == '}' && --depth == 0) break;
    }
    zzMarkedPos = end;
    return EXPRESSION;
  }

  private boolean atLineStart() {
    for (int i = zzStartRead - 1; i >= 0; i--) {
      char c = zzBuffer.charAt(i);
      if (c == '\n' || c == '\r') return true;
      if (c != ' ' && c != '\t' && c != '\f') return false;
    }
    return true;
  }
%}
%eof{ return;
%eof}

NL = \r\n|\r|\n
SPACE = [ \t\f]+
CONT = \\[ \t]*{NL}
ESC = \\[^\r\n]
DQ = \"([^\"\\\r\n]|{ESC}|{CONT})*\"?
SQ = '([^'\\\r\n]|{ESC}|{CONT})*'?
SUB = `([^`\\\r\n]|{ESC}|{CONT})*`?
VAR = \$[:jletter:][:jletterdigit:]*
NUM = [+-]?([0-9]+(\.[0-9]+)?([eE][+-]?[0-9]+)?)([lLfFdD]|BI|BD)?
// Keep structural punctuation separate. Escapes protect delimiters in bare names.
BARE = ([^\u0000-\u0020\\\"'`$#/:=()\[\]\{\},;\^*.!<>~&|]|{ESC})+

%%
<CONTINUED> {NL} { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
{SPACE} { return TokenType.WHITE_SPACE; }
{CONT} {
  int newlineLength = yylength() >= 2 && yycharat(yylength() - 2) == '\r' && yycharat(yylength() - 1) == '\n' ? 2 : 1;
  yypushback(newlineLength);
  yybegin(CONTINUED);
  return CONTINUATION;
}
{NL} { return NEWLINE; }
"#"[^\r\n]* { if (atLineStart()) return COMMENT; yypushback(yylength() - 1); return WORD; }
"${" { return expression(); }
{VAR} {
  // Java identifier character classes include '$'; CLI variable names do not.
  for (int i = 1; i < yylength(); i++) {
    if (yycharat(i) == '$') {
      yypushback(yylength() - i);
      return i == 1 ? WORD : VARIABLE;
    }
  }
  return VARIABLE;
}
{DQ}|{SQ} { return STRING; }
{SUB} { return SUBSTITUTION; }
"if" { return IF; }
"else" { return ELSE; }
"end-if" { return END_IF; }
"of" { return OF; }
"try" { return TRY; }
"catch" { return CATCH; }
"finally" { return FINALLY; }
"end-try" { return END_TRY; }
"for" { return FOR; }
"in" { return IN; }
"done" { return DONE; }
"batch" { return BATCH; }
"run-batch" { return RUN_BATCH; }
"discard-batch" { return DISCARD_BATCH; }
"holdback-batch" { return HOLDBACK_BATCH; }
"set" { return SET; }
"unset" { return UNSET; }
"echo" { return ECHO; }
"rollout" { return ROLLOUT; }
"rollback-across-groups" { return ROLLBACK_ACROSS_GROUPS; }
"--headers" { return HEADERS_OPTION; }
"bytes" { return BYTES; }
"expression" { return EXPRESSION_TYPE; }
"true" { return TRUE; }
"false" { return FALSE; }
"undefined" { return UNDEFINED; }
"=>" { return ARROW; }
"==" { return EQEQ; }
"!=" { return NE; }
">=" { return GE; }
"<=" { return LE; }
"~=" { return MATCH; }
"&&" { return AND; }
"||" { return OR; }
">>" { return APPEND; }
":" { return COLON; }
"/" { return SLASH; }
"=" { return EQ; }
"(" { return LPAREN; }
")" { return RPAREN; }
"[" { return LBRACKET; }
"]" { return RBRACKET; }
"{" { return LBRACE; }
"}" { return RBRACE; }
"," { return COMMA; }
";" { return SEMICOLON; }
"^" { return CARET; }
"*" { return STAR; }
".." { return DOTDOT; }
"." { return DOT; }
">" { return GT; }
"<" { return LT; }
"|" { return PIPE; }
"!" { return BANG; }
{NUM} { return NUMBER; }
{BARE} { return WORD; }
// A lone $, backslash, or operator may be a name being typed. Never stall.
"$"|"\\"|"~"|"&" { return WORD; }
[^] { return TokenType.BAD_CHARACTER; }
