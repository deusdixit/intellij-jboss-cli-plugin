# Syntax design and research

## Sources

Research started before project scaffolding. The EAP 8 configuration guide links
to the separate Management CLI Guide. Detailed CLI references are published under
earlier EAP releases; upstream source is additional evidence, not a guarantee of
identical behavior in every EAP patch or WildFly release.

- [EAP 8 management](https://docs.redhat.com/en/documentation/red_hat_jboss_enterprise_application_platform/8.0/html/configuration_guide/eap-mgmt-overview_default)
- [CLI requests, escaping, headers, control flow, redirection](https://docs.redhat.com/en/documentation/red_hat_jboss_enterprise_application_platform/7.4/html/management_cli_guide/creating_executing_requests)
- [CLI command and batch reference](https://docs.redhat.com/en/documentation/red_hat_jboss_enterprise_application_platform/7.4/html/management_cli_guide/reference-material_default)
- [Variables and property substitution](https://docs.redhat.com/en/documentation/red_hat_jboss_enterprise_application_platform/7.3/html/management_cli_guide/config_management_cli)
- [WildFly value conversion](https://github.com/wildfly/wildfly-core/blob/main/cli/src/main/java/org/jboss/as/cli/ArgumentValueConverter.java)
- [WildFly line processing](https://github.com/wildfly/wildfly-core/blob/main/cli/src/main/java/org/jboss/as/cli/impl/CommandContextImpl.java)
- [Conditional handler](https://github.com/wildfly/wildfly-core/blob/main/cli/src/main/java/org/jboss/as/cli/handlers/ifelse/IfHandler.java)
- [Loop help](https://github.com/wildfly/wildfly-core/blob/main/cli/src/main/resources/help/for.txt)
- [Header and rollout syntax](https://developer.jboss.org/docs/DOC-17599)

## Rules that shape the implementation

1. Requests use `[address]:operation[(parameters)][{headers}]`. Addresses have
   slash-separated type/name pairs; the root, relative paths, quoted names,
   escaped separators, wildcards, and substitution are accepted.
2. Parameters use commas. Boolean parameters can omit `=true`; `!parameter`
   denotes false. Operation names are not a fixed list of keywords.
3. Values include recursive lists and objects, `key=value` CLI entries,
   `"key" => value` DMR entries, parenthesized DMR properties, byte literals,
   numeric suffixes, `undefined`, and protected brace-delimited text.
4. Trailing operation headers use semicolons. `--headers={...}` belongs to
   command arguments. `operation-headers` is the underlying request field.
   Rollout plans have group policies, `,` sequencing, and `^` concurrency.
5. `set identifier=value` declares variables. `$identifier` is CLI substitution;
   `${property:default}` is a separate expression form. Expressions may nest.
   Backtick substitution is preserved as a token rather than executed.
6. `if condition of command` evaluates the command response. Conditions support
   comparisons, regular-expression matching, `&&`, `||`, and parentheses.
   `try` has optional catch/finally branches and ends with `end-try`.
   `for identifier in command` iterates a result collection and ends with `done`.
7. `batch` and resumed named batches indent until `run-batch`, `discard-batch`,
   or `holdback-batch`. `batch -l` and help requests do not open blocks.
8. Unescaped physical newlines end statements. Backslash continuations are
   parser trivia but remain concrete formatter tokens so they cannot be removed.
   Full-line comments, including indented comments in scripts, start with `#`.
   An embedded `#` remains part of the argument.
9. A command's argument syntax may vary by handler. General positional and
   named arguments remain extensible, including nested `--operation` requests.
   Pipes and output redirection are represented explicitly.

## Editor recovery versus execution validity

The parser is intentionally tolerant of unfinished input and supports nested
control-flow trees for editing. It does not certify runtime validity. For example,
the CLI restricts nested `if` and nested `for` blocks, disallows some control-flow
commands in active batches, and requires at least one catch/finally branch in a
completed try. Server model constraints, variable resolution, and command-specific
option validation are outside this plugin's scope.

Unfinished strings and expressions stop before an uncontinued newline. Grammar-Kit
pinning retains partially entered constructs. Recovery stops at delimiters or the
next logical line, and a fallback error node consumes otherwise unrecognized input
without losing the rest of the file. Parentheses and list/map contents remain
structured; the lexer never treats a colon inside a quoted string or an expression
as an operation separator.

The formatter adjusts indentation and deliberately preserves intra-line spacing:
unquoted values, escaped spaces, and command arguments may depend on it. It does
not automatically insert new physical line breaks or continuation backslashes.
