# JBoss / WildFly CLI for IntelliJ IDEA

Kotlin language plugin for JBoss EAP 8 / WildFly management scripts (`*.cli`).

## Features

- File association and a CLI icon.
- Grammar-Kit parser, generated PSI, and JFlex lexer.
- Highlighting for addresses, operations, command names, parameters, strings,
  variables, expressions, DMR values, comments, and delimiters.
- Indentation for `if`/`else`, `try`/`catch`/`finally`, `for`, batches, and
  continued multiline parameters, maps, lists, and operation headers.
- Comment/uncomment actions and paired bracket matching.
- Recovery while editing incomplete requests and control-flow blocks.
- Configurable colors under **Editor → Color Scheme → JBoss / WildFly CLI**.

This is an editor plugin. It does not connect to or execute commands on a server.
Operation names and command names are extensible; checking whether a resource,
parameter, or operation exists requires the target server's management model.

## Build and test

Use JDK 21. The Gradle wrapper downloads the pinned Gradle distribution and the
build downloads IntelliJ IDEA Community 2025.2.6 as its SDK.

```sh
./gradlew test buildPlugin
```

The installable archive is `build/distributions/jboss-cli-1.0.0.zip`.
Generated Java parser, lexer, and PSI classes are build outputs under
`build/generated/sources/grammar`; Kotlin contains the handwritten implementation.
Generation runs automatically before compilation.

```sh
./gradlew generateParser generateLexer  # Regenerate from grammar sources
./gradlew runIde                        # Start an isolated development IDE
./gradlew verifyPluginStructure         # Check the packaged plugin
./gradlew verifyPlugin                  # Check binary compatibility with the target IDE
```

The minimum declared IDE version is IntelliJ IDEA 2025.2 (build 252).
JetBrains Plugin Verifier reports the packaged plugin as compatible with
IDEA Community 2025.2.6 (`252.28539.13`) and IDEA 2026.2.2 (`262.10315.125`).
Editor tests run on 2025.2.6; binary verification does not replace interactive
testing of every IDE release.
To include another installed IDE in verification, use
`./gradlew verifyPlugin -PverificationIdePath=/path/to/idea`.

## Install

In IntelliJ IDEA, open **Settings → Plugins → gear menu → Install Plugin from
Disk**, select the ZIP, and restart if prompted. Open a `.cli` file. Use
**Code → Reformat Code** to indent it.

## Formatting and CLI syntax

Formatting preserves spaces inside values and preserves existing line breaks and
backslash continuations. In executable `.cli` scripts, continue multiline requests
with a backslash; braces alone do not continue a physical line:

```text
/subsystem=example:add( \
    options={ \
        handlers=[ \
            {name="first",enabled=true} \
        ] \
    } \
){allow-resource-service-restart=true;blocking-timeout=60}
```

The example subsystem is illustrative. See [examples](examples) for more scripts
and [syntax notes](docs/syntax.md) for the researched grammar and limitations.

## Source layout

| Location | Purpose |
| --- | --- |
| `src/main/grammar/JBossCli.bnf` | Requests, values, headers, commands, control flow, and recovery |
| `src/main/grammar/JBossCli.flex` | Restartable lexer, escapes, comments, nested expressions |
| `src/main/kotlin/dev/jbosscli/psi` | Language element types and PSI file |
| `src/main/kotlin/dev/jbosscli/highlight` | Lexical colors, semantic roles, color settings |
| `src/main/kotlin/dev/jbosscli/format` | Formatting model, indentation blocks, defaults |
| `src/test/kotlin/dev/jbosscli` | IntelliJ parser, formatter, highlighting, and lexer tests |

Parser tests cover CLI and native DMR values, headers and rollout plans,
substitution, control flow, batches, and error recovery. Lexer tests check restart
consistency and nested expressions. Formatter tests check indentation, unchanged
literal content, and idempotence.
