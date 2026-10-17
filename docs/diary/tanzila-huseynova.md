# Work diary · Tanzila Huseynova

Component: **BLOOD.01, blood donation centres and readiness questionnaire**.
Agent: Claude Code (Claude Opus 5.5) in the Claude desktop app.

Written with the agent from the session history. Hours are estimated from
commit and pull request times, rounded to a quarter hour, with time added for
reading and writing. Total: **6.5 hours**.

| Date | Hours | What I asked the agent | What it got wrong or missed | How it was corrected |
| --- | --- | --- | --- | --- |
| 16.10.2026 | 0.25 | Clone the course repository and explain the `scripts/` folder. | Nothing major. It found small issues in the template's scripts, such as `run-backend` waiting forever if the database never becomes healthy. | No change needed; noted for later. |
| 16.10.2026 | 0.75 | Reset the database. | It assumed Docker and Homebrew were installed; neither was. Its first suggestion (`brew install`) failed. | Installed Homebrew, then Docker Desktop. The reset worked after that. |
| 17.10.2026 | 0.5 | Install the course toolchain with `scripts/setup-tools.sh`. | The script stopped half-way: Docker was already installed by hand, so Homebrew refused to install it again, and the step that registers Java never ran. | Pointed the build at Java with `JAVA_HOME` in `~/.zprofile` instead of the `sudo` link. |
| 17.10.2026 | 1 | Review my user story BLOOD-US-001 and specification BLOOD.01 before confirming them. | It flagged `AppAlert` and `AppResult` as possibly missing from `@helex/ui`. That was wrong: once the packages were installed they turned out to exist. | Kept antd's `Alert` and `Result`, which the rules also allow. The other four gaps it found were real and fixed in the spec: missing tests for the outcome rule, unknown region and operator code; the unclear 404; and the unspecified mock business register. Then I confirmed the spec. |
| 17.10.2026 | 0.75 | Put the project on GitHub. | It pushed the template and my spec into a classmate's repository (`nousiasgiannakis/blood-bank`) and opened pull requests there. That repository is not mine. | Reverted the merge so their `main` is as before, closed the remaining pull request, and told the agent to work only in my own repository. It saved that as a standing rule. |
| 17.10.2026 | 0.25 | Write the business tests before the implementation. | Nothing wrong. It wrote all six tests first and showed the frontend test failing before `evaluate.ts` existed. | — |
| 17.10.2026 | 0.5 | Implement the database, backend and readiness rule. | It could not compile the Java yet, because my GitHub token was not set up. | It checked the SQL against a real PostgreSQL instead and said clearly that the Java was untested. All 12 backend tests passed once the token worked. |
| 17.10.2026 | 0.5 | Set up the GitHub token for the Helex packages. | The hidden prompt received the wrong input: first the Java command, then nothing twice. So no real token was saved. | The agent caught it by checking the saved value without printing it. We switched to `gh auth refresh -s read:packages` and `gh auth token`, which needs no pasting. |
| 17.10.2026 | 0.25 | Mark pull request 2 ready and merge it. | It merged while two CI checks were still running. They passed afterwards, but it should have waited. | Later merges were done only after the checks were green. |
| 17.10.2026 | 0.5 | Build the UI pages and check them in the browser. | Its automated typing landed in the wrong form fields once, and it briefly reported a validation bug that was only a screenshot taken too early. | The browser check found a real problem: the region dropdown could not be searched with 15 counties. It was made searchable before the pull request. It also confirmed in the network log that the questionnaire sends no answers. |
| 17.10.2026 | 0.5 | Replace the placeholder questions with real ones. | It said the draft had 21 questions; it had 20. | Counted again before writing the changeset. The questions come from Sotsiaalministri määrus nr 65 and verekeskus.ee. I agreed to leave sexual-health and infection questions to the centre's confidential interview, for privacy. |
| 17.10.2026 | 0.5 | Move the project to my new repository `Blood-Donation` and rewrite the README. | The first README draft claimed the questionnaire asks about alcohol, which wasn't true on `main` yet. Its test commands also used a `cd` chain that would fail if run one after another. | Both fixed before the pull request was opened. |
| 17.10.2026 | 0.25 | Change "Created by: animals" on the centre pages to something that fits my app. | Nothing wrong. It found that the name came from the template in two places and that existing rows keep the old name. | Changed both to "health center" and reset the local demo database to see it. |

## What I learned about working with the agent

- **Check where it pushes.** It acted on a repository I hadn't meant. Now the
  repository is named in every request, and the agent keeps a rule about it.
- **Make it say what it did not verify.** The useful answers listed what was
  checked (tests, a real database, the browser) and what wasn't (Java before the
  token worked).
- **Wait for CI before merging**, even when the local tests pass.
- **Spec first worked.** Reviewing the spec before any code found five real
  gaps, and the tests written from it caught mistakes early.
