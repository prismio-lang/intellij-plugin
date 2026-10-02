# Security policy

## Supported versions

| Version | Security fixes |
|---------|----------------|
| 0.1.x   | Yes |

Fixes go into the latest release. Please update to it rather than staying on
an older one.

## Reporting a problem

Email **security@prismio.org**. Please don't open a public issue: that shows
the problem to everyone before there is a fix.

It helps if your report includes:

- what the problem is and what an attacker could do with it
- the steps to reproduce it, ideally with a small project that shows it
- the plugin version, IDE and version, and operating system
- how we can reach you with questions

## What this policy covers

This repository, the Prismio plugin for JetBrains IDEs.

The plugin runs programs on your machine. It starts the Prismio compiler to
check the file you are editing, and it runs the commands your run
configurations and `build.ums` name. The problems we most want to hear about
are the ones where that goes wrong, for example:

- opening a project makes the plugin run a program the project itself supplied
- text in a source file or `build.ums` ends up in a command line in a way it shouldn't
- the plugin reads or writes files outside the project and the system's temp directory

Report problems in other projects to them instead:

- the compiler, runtime, standard library or build tooling: the
  [Prismio security policy](https://github.com/prismio-lang/prismio/security/policy)
  (also security@prismio.org)
- the IDE itself: [JetBrains security](https://www.jetbrains.com/legal/terms/jetbrains-security-policy/)
- a third-party library: its maintainers

## What happens next

| Step | When |
|------|------|
| We confirm we got your report | Within 48 hours |
| First assessment | Within 5 business days |
| Fix for a critical problem | Within 14 days |
| Public disclosure | After the fix is released and users have had time to update |

We'll credit you in the release notes unless you'd rather stay anonymous.
