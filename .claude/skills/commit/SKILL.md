---
name: commit
description: Create a commit following Lumie commit conventions
user-invocable: true
argument-hint: [optional message hint]
---

Create a git commit following Lumie conventions strictly.

## Pre-commit gate (mandatory)

**Step 0 — reviewer dispatch:** Invoke `/pre-commit-review` first. It detects changed files and runs the matching reviewer agents (`backend-reviewer`, `frontend-reviewer`, `ui-reviewer`, `worker-reviewer`, `infra-validator`, `migration-reviewer`, `claude-md-linter`) plus `lumie-risk-scorer` in parallel. If any agent returns a blocker, STOP — do not proceed to the sweep or commit.

**Step 1 — HARD_RULES sweep on staged diff** (delegates to SSOT):
```bash
bash .claude/lint/sweep.sh all --staged
```
- Patterns live in `.claude/lint/patterns/{backend,frontend,worker}.yaml`.
- Exit 2 = HARD_RULES violation — STOP, do not commit.
- Investigate why PreToolUse hook didn't catch (bypassed?) before resolving.

## Steps

1. Run `git status` and `git diff --cached` (if nothing staged, run `git diff`).
2. Run pre-commit gates above.
3. Run `git log --oneline -1` to check the last commit.
4. Analyze the changes and determine:
   - TYPE: FEAT, FIX, CHORE, REFACTOR, PERF, REVERT, or DOCS
   - scope: affected module/service name
5. **Amend decision**: If the staged/unstaged changes are closely related to the last commit (same scope, continuation of same work), propose `--amend` to the user. If in a monorepo, warn about force push triggering unrelated pipelines and prefer a new commit instead.
6. Draft commit message:
   - Subject: `TYPE(scope): description` — max 50 chars, imperative mood, no period
   - Body: required, use `-` bullet points, 72 chars/line
   - If amending, update the message to cover both original and new changes
7. NEVER include AI/Claude mentions or Co-Authored-By lines.
8. Stage relevant files (specific files, not `git add -A`).
9. Commit using heredoc format (add `--amend` flag if amend was agreed).

If $ARGUMENTS is provided, use it as a hint for the commit message.

## Documentation sync (after successful commit)

- ❌ DISABLED — audit cleanup window. SKIP entire section.
- ✅ Report only: `code commit: <hash>, docs: skipped (audit cleanup window)`.
- 🔁 Restore: revert this section block from git history once Tier 0~1 audit cleanup is complete.

<details>
<summary>Original doc-sync workflow (reference only — not active during cleanup)</summary>

Evaluate doc-impact after the code commit succeeds:

**SKIP silently** (no LLM work, no second commit) if the commit is:
- Internal refactor, typo fix, formatting, or private rename with same I/O
- Algorithm change with identical public interface
- Test-only change

**MUST sync docs** if ANY of the following changed:
- ❌ Public API: controller URL, HTTP method, DTO field shape, response code
- ❌ Module boundary: new module, module split, port interface change → also update `intro.md` Mermaid diagram
- ❌ Domain term rename (anything in `DOMAIN_GLOSSARY.md`)
- ❌ External dependency: RabbitMQ exchange name, env var, ingress path
- ❌ New architecture decision
- ❌ Schema/migration change (Flyway file added/modified) → sync `data-model/` (overview, relevant schema, migration-guide)
- ❌ New service added or removed → update `intro.md` Mermaid diagram + relevant section overview
- ❌ `.claude/` change (agent, skill, hook, knowledge, rules) → sync `dev/documentation.md`; if material to platform overview sync `intro.md`
- ❌ Dev tooling change (Tilt, Docker Compose, Dockerfile, Makefile) → sync `dev/tilt.md` or `dev/workspace.md`
- ❌ Production incident resolved or post-mortem written → sync `troubleshooting/<slug>.md` (create if new)

**If sync required:**

1. Read `lumie-document/.claude/knowledge/DOC_GEN_RULES.md` for doc rules and output format. Read `lumie-document/.claude/knowledge/DOC_GEN_SIDEBAR_MAP.md` for sidebar/path/module mapping.
2. Identify which `lumie-document/docusaurus/docs/<section>/*.md` files are affected.
3. Edit those files directly — Korean per DOC_GEN_RULES, preserve existing structure and `sidebar_position`, update only changed sections.
4. Commit lumie-document with message: `DOCS(<section>): sync from <code-repo>@<short-sha>`
5. ❌ Do NOT auto-push lumie-document — push is user-explicit (GitOps deploy safety).
6. Report to user: `code commit: <hash>, docs commit: <hash>` or `code commit: <hash>, docs: skipped (not doc-relevant)`.

</details>

## References

- HARD_RULES_CROSS: `.claude/knowledge/HARD_RULES_CROSS.md`
- Doc generation rules: `lumie-document/.claude/knowledge/DOC_GEN_RULES.md`
- Doc sidebar/module mapping: `lumie-document/.claude/knowledge/DOC_GEN_SIDEBAR_MAP.md`
