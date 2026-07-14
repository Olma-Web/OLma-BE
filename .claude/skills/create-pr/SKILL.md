---
name: create-pr
description: Create a GitHub PR using commit messages as the basis for the description
user-invocable: true
argument-hint: [optional PR title hint]
---

Create a GitHub Pull Request using commit messages to draft the title and body.

## Steps

1. Run `git branch --show-current` to confirm the current branch
2. Extract the issue number from the branch name (format: `<type>/<issue-number>/<slug>`)
3. Run `gh issue view <issue-number> --json title,labels` to fetch the issue title and labels
4. Use the issue title as the PR title as-is; collect the label names from the labels field
5. Run `git log develop..HEAD --format="%s%n%b"` to collect commit subjects and bodies
6. Draft the PR body using commit bodies:
   ```
   Closes #<issue-number>

   ## Summary
   <bullet points derived from commit bodies>

   ## Test plan
   - [ ] 관련 단위 테스트 통과 확인
   - [ ] API 응답 확인 (Swagger or Postman)
   ```
7. Show the draft to the user and confirm before creating
8. Run `git push origin <current-branch>` to push if not already pushed
9. Create the PR with `gh pr create --base develop --assignee @me` using a HEREDOC for the body, and add `--label "<name>"` for each label fetched from the issue
10. Print the PR URL when done

## Conventions
- Base branch is always `develop`
- PR title must match the linked GitHub issue title exactly
- Never include AI/Claude mentions

If $ARGUMENTS is provided, use it as a hint only when the issue number cannot be extracted from the branch name.
