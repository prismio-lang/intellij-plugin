# Security Policy

## Supported Versions

The following versions of the **Prismio Language Support** plugin currently receive security updates:

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ Active |

Older versions that are no longer listed above will **not** receive security patches. We strongly encourage all users to stay on the latest release.

---

## Reporting a Vulnerability

Security issues affecting the Prismio Language Support plugin should be reported responsibly and privately.

> [!CAUTION]
> **Do NOT open public GitHub issues for security vulnerabilities.** Doing so exposes the issue before a fix is available, putting all users at risk.

Instead, report vulnerabilities confidentially via email:

- **security@prismio.org**

Include the following information where possible:

- A clear description of the issue
- Steps required to reproduce it
- Affected component(s) and platform(s) (plugin version, IDE name and version, OS)
- Relevant proof-of-concept code, logs, screenshots, or test cases
- Potential impact assessment
- Contact information for follow-up communication

---

## Scope

This policy applies to the **prismio-intellij** plugin repository.

**In scope:**
- The Prismio Language Support IntelliJ plugin

**Out of scope — report to the appropriate project instead:**
- [Prismio compiler, runtime, LLVM bridge, package & build tooling](https://github.com/prismio-lang/prismio/security/policy) → `security@prismio.org`
- JetBrains Platform / IntelliJ IDEA itself → [JetBrains Security](https://www.jetbrains.com/legal/terms/jetbrains-security-policy/)
- Third-party dependencies → their respective maintainers

---

## Response Timeline

| Stage | Timeline |
|-------|----------|
| Acknowledgement | ≤ 48 hours |
| Initial assessment | ≤ 5 business days |
| Patch release | Critical issues within 14 days |
| Public disclosure | After patch is released and users have had time to update |

We will credit you in the release notes for responsible disclosures, unless you prefer to remain anonymous.
