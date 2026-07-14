---
name: create-issue
description: Create a GitHub issue following conventions
user-invocable: true
argument-hint: [optional issue title hint]
---

Create a GitHub issue following conventions strictly.

## Steps

1. Run `gh repo view --json nameWithOwner` to confirm the current repo
2. Run `gh label list` to see available labels
3. Ask the user for the following if not already clear from $ARGUMENTS or context:
   - **Title**: short, imperative, no period (e.g. "Add sensor threshold alert API")
   - **Type**: feat, fix, chore, docs, refactor
   - **Description**: what needs to be done and why (can be inferred from context)
   - **Labels**: choose from available labels (e.g. enhancement, bug, documentation)
4. Draft the issue body using this template:
   ```
   ## Summary
   <1-2 sentence description of what and why>

   ## Tasks
   - [ ] task 1
   - [ ] task 2

   ## Notes
   <optional: constraints, related issues, references>
   ```
5. Show the draft to the user and confirm before creating
6. Create the issue with `gh issue create` using a HEREDOC for the body
7. Print the issue URL and issue number when done

## Naming conventions
- Issue title format: `[TYPE] Short description` (e.g. `[FEAT] Add alert threshold API`)
- Use imperative mood ("Add", "Fix", "Remove" not "Added", "Fixed")

If $ARGUMENTS is provided, use it as a hint for the issue title or description.
