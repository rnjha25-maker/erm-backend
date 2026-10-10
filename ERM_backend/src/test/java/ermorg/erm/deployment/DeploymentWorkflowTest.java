package ermorg.erm.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

/** Executes the actual branch guard, without contacting GitHub, Docker or EC2. */
class DeploymentWorkflowTest {
    @TempDir Path temporary;

    @SuppressWarnings("unchecked")
    private Map<String, Object> workflow() throws Exception {
        return new Yaml().load(Files.readString(Path.of("../.github/workflows/deploy-ec2.yml")));
    }

    @SuppressWarnings("unchecked")
    @Test void manualStagingAndMainDeployUseTheExactBuiltRelease() throws Exception {
        var workflow = workflow();
        // SnakeYAML's YAML 1.1 parser treats the unquoted GitHub key "on" as true.
        var triggers = (Map<String, Object>) workflow.getOrDefault("on", workflow.get(true));
        assertThat(triggers).containsKeys("push", "workflow_dispatch");
        var jobs = (Map<String, Map<String, Object>>) workflow.get("jobs");
        assertThat(jobs.get("deploy").get("needs")).isEqualTo(List.of("validate", "build-and-push"));
        var buildSteps = (List<Map<String, Object>>) jobs.get("build-and-push").get("steps");
        var images = buildSteps.stream().filter(step -> String.valueOf(step.get("uses")).startsWith("docker/build-push-action@")).toList();
        assertThat(images).hasSize(9);
        for (var image : images) {
            assertThat(((Map<String, Object>) image.get("with")).get("tags").toString())
                .endsWith(":${{ needs.validate.outputs.image_tag }}").doesNotContain(":latest");
        }
        String yaml = Files.readString(Path.of("../.github/workflows/deploy-ec2.yml"));
        assertThat(yaml).contains("IMAGE_TAG=${{ needs.validate.outputs.image_tag }}", "cancel-in-progress: false");
    }

    @SuppressWarnings("unchecked")
    @Test void productionRejectsFeatureBranchesAndTagsButStagingAcceptsBranch() throws Exception {
        var jobs = (Map<String, Map<String, Object>>) workflow().get("jobs");
        var steps = (List<Map<String, Object>>) jobs.get("validate").get("steps");
        Path script = temporary.resolve("guard.sh");
        Files.writeString(script, steps.get(0).get("run").toString());
        assertThat(runGuard(script, "staging", "refs/heads/dashboard-fixes")).isZero();
        assertThat(runGuard(script, "production", "refs/heads/main")).isZero();
        assertThat(runGuard(script, "production", "refs/heads/dashboard-fixes")).isNotZero();
        assertThat(runGuard(script, "staging", "refs/tags/main")).isNotZero();
    }

    private int runGuard(Path script, String target, String ref) throws Exception {
        String bash = System.getProperty("os.name").startsWith("Windows")
                ? "C:/Program Files/Git/bin/bash.exe" : "bash";
        var builder = new ProcessBuilder(bash, script.toString().replace('\\', '/'));
        var env = builder.environment();
        env.put("TARGET", target);
        env.put("GITHUB_REF", ref);
        env.put("GITHUB_SHA", "abc123");
        env.put("GITHUB_RUN_ID", "123");
        env.put("GITHUB_RUN_ATTEMPT", "1");
        Path output = temporary.resolve("outputs.txt");
        env.put("GITHUB_OUTPUT", output.toString().replace('\\', '/'));
        var process = builder.redirectErrorStream(true).start();
        String diagnostic = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        int exit = process.waitFor();
        assertThat(exit).withFailMessage("Bash could not execute guard: %s", diagnostic).isIn(0, 1);
        if (exit == 0) {
            assertThat(Files.readString(output)).contains("image_tag=sha-abc123-123-1", "target=" + target);
        }
        return exit;
    }
}
