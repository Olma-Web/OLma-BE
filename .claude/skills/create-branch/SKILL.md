---
name: create-branch
description: Create a branch from develop following naming conventions
user-invocable: true
argument-hint: [optional issue number or branch hint]
---

Create a new branch from develop following naming conventions strictly.

## Steps

1. Ask the user for the following if not already clear from $ARGUMENTS or context:
   - **Type**: feat, fix, chore, docs, refactor
   - **Issue number**: GitHub issue number (e.g. 13)
   - **Slug**: short description derived from issue title (lowercase, hyphen-separated, max 30 chars)
2. Propose branch name: `<type>/<issue-number>/<slug>` (e.g. `feat/13/add-alert-threshold-api`)
3. Confirm with the user before creating
4. Run `git checkout develop && git pull origin develop`
5. Run `git checkout -b <branch-name>`
6. Print the branch name when done

## Naming conventions
- Format: `<type>/<issue-number>/<slug>`
- Types: feat, fix, chore, docs, refactor
- Slug: lowercase, hyphens only, max 30 chars, derived from issue title
- Examples: `feat/13/add-alert-api`, `fix/14/sensor-null-error`

If $ARGUMENTS is provided, use it as a hint for the issue number or branch name.
