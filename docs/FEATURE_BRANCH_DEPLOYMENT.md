# Manual branch deployment using existing repository secrets

Manual branch runs and automatic `main` runs deploy to the same existing EC2 server,
application directory, database, Redis and storage configuration. A manual feature-branch
run replaces the running application. This is not an isolated staging environment.

## Run a branch

1. Commit and push the branch with the updated workflow.
2. Open Actions → Deploy To EC2 → Run workflow.
3. Select the branch under **Use workflow from**, for example `feature/dashboard-changes`.
4. Start a new run. There is no staging/production input.
5. Verify application health and business behaviour on the existing application URL.
6. After testing, merge into `main`; a push to `main` deploys automatically.

Feature-branch pushes do not deploy automatically. Tags and non-dispatch/non-main-push
runs are rejected. Re-running an old failed run uses its old workflow revision; start a
new run from the updated branch instead. The workflow must be present on the default
branch for GitHub's Run workflow UI to be available.

## Existing secrets

Both paths use the same unprefixed repository secrets:

- `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, `EC2_APP_DIR`
- `DOCKER_HUB_USERNAME`, `DOCKER_HUB_TOKEN`
- `AWS_RDS_ENDPOINT`, `AWS_RDS_USERNAME`, `AWS_RDS_PASSWORD`
- `AWS_REDIS_ENDPOINT`
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`

No `STAGING_*` secrets or GitHub environments are required. This workflow reads secrets,
not the GitHub Variables tab. Secret values must not be printed in logs. Only trusted
repository branches should be deployed because their code runs with deployment access.

## Release consistency

All images use a commit SHA/run ID/run attempt tag; deployment uses that exact tag.
All manual and automatic runs share one concurrency group for the single EC2 destination.
An active run is not cancelled. GitHub may replace an older pending run with a newer one.

Host-wide Docker pruning remains disabled to retain rollback images. Monitor disk usage.
To roll back, restore the known-good image tag and matching Compose configuration; this
does not undo database changes. Legacy manual scripts must use the release tag rather
than assume `latest` was updated.

## Validation and deployment limits

Backend tests run before image publication. The default QA-connected application-context
test is excluded and opt-in local database tests are not enabled on the GitHub runner.
Deployment starts containers in sequence but does not establish application health.
Migrations must be applied separately when needed; do not rerun already-applied scripts.
