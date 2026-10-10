# Manual branch deployment

The EC2 workflow supports testing a branch before merging it into `main`.
No deployment is triggered by editing this workflow locally.

## One-time GitHub setup

1. Make the workflow available on the repository's default branch so that GitHub
   displays **Run workflow**. The selected feature branch must also contain this
   updated workflow. Publish the branch before selecting it.
2. Create GitHub environments named `staging` and `production`. Restrict production
   deployment branches to `main`; configure required reviewers as appropriate.
3. Configure a separate staging EC2 host. The Compose file uses fixed container names
   and ports, so a different directory on the production host is not sufficient.
4. Configure these staging secrets (repository secrets or the staging environment):
   - `STAGING_EC2_HOST`, `STAGING_EC2_USER`, `STAGING_EC2_SSH_KEY`, `STAGING_EC2_APP_DIR`
   - `STAGING_AWS_RDS_ENDPOINT`, `STAGING_AWS_RDS_USERNAME`, `STAGING_AWS_RDS_PASSWORD`
   - `STAGING_AWS_REDIS_ENDPOINT`
   - `STAGING_AWS_ACCESS_KEY_ID`, `STAGING_AWS_SECRET_ACCESS_KEY`, `STAGING_AWS_REGION`
5. Retain the existing unprefixed production secrets and shared
   `DOCKER_HUB_USERNAME` / `DOCKER_HUB_TOKEN` secrets. Staging secrets do not fall back
   to production secrets. Keep staging database/storage resources separate and apply
   the required schema migrations to the actual staging database before deployment.

The workflow validates required deployment settings before copying files to EC2.
The host must have Docker/Compose installed, access to the selected registry images,
and network connectivity to its configured database, Redis and storage resources.
Existing application storage/bucket configuration must also be reviewed for staging;
different AWS credentials alone do not prove that buckets are isolated.

## Test a feature branch

1. Open **Actions → Deploy To EC2 → Run workflow**.
2. Under **Use workflow from**, select `dashboard-fixes` (or another feature branch).
3. Select environment **staging**, which is the manual-run default.
4. Run the workflow and check the test, build and deployment jobs.
5. Verify application health and business flows against the staging URL. The deployment
   script starts containers in sequence; container startup is not an application-health
   or business acceptance test.
6. After testing/review, merge into `main`. A push to `main` retains the existing automatic
   production deployment behaviour. Production can also be run manually from `main`.

Feature branches cannot target production through the workflow's branch guard. GitHub
environment branch policies are the enforcement boundary against edits to the workflow
itself; only trusted repository branches should receive deployment secrets.

## Release consistency and rollback

Every image is tagged with commit SHA, workflow run ID and run attempt. Deployment uses
the same tag for all services; feature builds never overwrite `latest`. Concurrent runs
for the same target are serialized without interrupting an active deployment. GitHub may
replace an older pending run with a newer pending run; this is not a FIFO queue.

Automatic host-wide Docker pruning was removed to preserve previous images for rollback.
Monitor disk usage and manage retention separately. To roll back, restore a known-good
image tag in the target's `.env` and redeploy the matching Compose version. This does not
roll back database migrations. Manual scripts that rely on `latest` must instead use
the release tag from the successful workflow run.

## Test scope

The workflow runs backend tests before publishing images. The default application-context
test is excluded because its default configuration points at a shared QA database.
Opt-in database tests are not enabled in GitHub's runner by this change; run them against
the disposable local MySQL fixture separately. Never enable them against shared data.

No migrations, merges or deployments are performed automatically by the new manual-run
option beyond the selected build/deploy workflow itself.
