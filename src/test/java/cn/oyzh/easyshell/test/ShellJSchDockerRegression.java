package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.docker.ShellDockerCommit;
import cn.oyzh.easyshell.ssh.docker.ShellDockerContainer;
import cn.oyzh.easyshell.ssh.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh.docker.ShellDockerImage;
import cn.oyzh.easyshell.ssh.docker.ShellDockerImageHistory;
import cn.oyzh.easyshell.ssh.docker.ShellDockerParser;
import cn.oyzh.easyshell.ssh.docker.ShellDockerPort;
import cn.oyzh.easyshell.ssh.docker.ShellDockerResource;
import cn.oyzh.easyshell.ssh.docker.ShellDockerRmi;
import cn.oyzh.easyshell.ssh.docker.ShellDockerRun;
import cn.oyzh.easyshell.ssh.docker.ShellDockerSave;
import cn.oyzh.easyshell.ssh.docker.ShellDockerTag;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class ShellJSchDockerRegression {

    private static final String LOCAL_IMAGE = "easyshell-jsch-test:local";

    private static final String EXIT_MARKER = "__JSCH_DOCKER_EXIT__";

    private ShellJSchDockerRegression() {
    }

    public static void main(String[] args) {
        try {
            run(args);
            System.out.println("JSch Docker regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {
        String host = value("test.host", args, 0);
        String user = value("test.user", args, 1);
        String password = value("test.password", args, 2);
        String image = System.getProperty("test.docker.image", System.getenv().getOrDefault("EASYSHELL_TEST_DOCKER_IMAGE", LOCAL_IMAGE));

        ShellConnect connect = new ShellConnect();
        connect.setHost(host.contains(":") ? host : host + ":22");
        connect.setUser(user);
        connect.setPassword(password);
        connect.setAuthMethod("password");
        connect.setCharset("UTF-8");
        connect.setConnectTimeOut(15000);

        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.setVerifyFailureCallback(current -> current);
            client.start(15000);
            ShellDockerExec docker = client.dockerExec();
            DockerFixture fixture = new DockerFixture(image);
            boolean imageCreated = false;
            Throwable failure = null;
            try {
                requireDockerBasics(docker);
                imageCreated = ensureImage(client, docker, image, fixture);
                requireImageVisible(docker, image);
                exerciseContainer(client, docker, image, fixture);
                requireParserResilience();
            } catch (Throwable ex) {
                failure = ex;
                throw ex;
            } finally {
                try {
                    cleanup(client, docker, fixture, imageCreated);
                } catch (Throwable cleanupFailure) {
                    if (failure == null) {
                        throw cleanupFailure;
                    }
                    failure.addSuppressed(cleanupFailure);
                }
            }
        }
    }

    private static void requireDockerBasics(ShellDockerExec docker) {
        String version = docker.docker_version();
        requireNotBlank(version, "docker version output is blank");
        requireContains(version, "Client:", "docker version missing client information");
        requireContains(version, "Server:", "docker version missing server information");

        String info = docker.docker_info();
        requireNotBlank(info, "docker info output is blank");
        String lowerInfo = info.toLowerCase(Locale.ROOT);
        if (!lowerInfo.contains("server version") && !lowerInfo.contains("containers")) {
            throw new IllegalStateException("unexpected docker info output");
        }

        String compose = docker.docker_compose_version();
        requireNotBlank(compose, "docker-compose version output is blank");
        String lowerCompose = compose.toLowerCase(Locale.ROOT);
        if (lowerCompose.contains("command not found") || lowerCompose.contains("not recognized")) {
            System.out.println("Docker Compose standalone command is unavailable; method returned the expected not-found response");
        } else if (!lowerCompose.contains("compose")) {
            throw new IllegalStateException("unexpected docker-compose version output");
        }
        System.out.println("Docker basics verified");
    }

    private static boolean ensureImage(ShellSSHClient client, ShellDockerExec docker, String image, DockerFixture fixture) {
        if (imageExists(docker, image)) {
            System.out.println("Docker test image already available");
            return false;
        }
        if (LOCAL_IMAGE.equals(image)) {
            buildLocalImage(client, image, fixture.suffix);
            System.out.println("Docker local test image created");
        } else {
            execChecked(client, "docker pull " + shellQuote(image), 180000);
            System.out.println("Docker test image pulled");
        }
        if (!imageExists(docker, image)) {
            throw new IllegalStateException("docker test image is not available after preparation");
        }
        return true;
    }

    private static void buildLocalImage(ShellSSHClient client, String image, String suffix) {
        String root = "/tmp/easyshell-jsch-image-" + suffix;
        String tar = root + ".tar";
        String command = "set -e; root=" + shellQuote(root) + "; tar=" + shellQuote(tar)
                + "; trap 'rm -rf \"$root\" \"$tar\"' EXIT"
                + "; mkdir -p \"$root/bin\""
                + "; cp /usr/bin/busybox \"$root/bin/busybox\""
                + "; for command in sh sleep echo cat ip hostname; do ln -s busybox \"$root/bin/$command\"; done"
                + "; tar -C \"$root\" -cf \"$tar\" ."
                + "; docker import --change 'CMD [\"/bin/sh\"]'"
                + " --change 'ENV PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'"
                + " \"$tar\" " + shellQuote(image);
        execChecked(client, command, 60000);
    }

    private static void requireImageVisible(ShellDockerExec docker, String image) {
        List<ShellDockerImage> images = ShellDockerParser.images(docker.docker_images());
        require(!images.isEmpty(), "docker images parser returned no images");
        require(images.stream().anyMatch(current -> image.equals(current.getImageName())),
                "docker images parser did not return the prepared image");
    }

    private static void exerciseContainer(ShellSSHClient client, ShellDockerExec docker, String image, DockerFixture fixture) {
        requireRunCommandOptions(docker, image);
        createRunningContainer(docker, image, fixture);
        requireRunningContainer(docker, fixture);
        requireContainerMetadata(docker, fixture);
        requireDockerUpdate(docker, image, fixture);
        requirePauseRestartAndRename(docker, fixture);
        requireLogsAndExec(client, docker, fixture);
        requireStopStartAndKill(docker, fixture);
        requireImageLifecycle(client, docker, fixture);
        requireRemoval(docker, image, fixture);
        System.out.println("Docker container and image lifecycle verified");
    }

    private static void requireRunCommandOptions(ShellDockerExec docker, String image) {
        ShellDockerRun preview = new ShellDockerRun();
        preview.setImageName(image);
        preview.setRm(true);
        preview.setPrivileged(true);
        preview.setRestart("unless-stopped");

        ShellDockerRun.DockerPort udp = new ShellDockerRun.DockerPort();
        udp.setOuterPort(12345);
        udp.setInnerPort(53);
        udp.setType("udp");
        preview.setPorts(List.of(udp));

        String command = docker.docker_run_cmd(preview);
        requireContains(command, "--rm", "docker run --rm option is missing");
        requireContains(command, "--privileged", "docker run --privileged option is missing");
        requireContains(command, "--restart=" + shellQuote("unless-stopped"), "docker run restart option is missing");
        requireContains(command, "-p 12345:53/udp", "docker run udp port option is missing");
        require(command.contains(shellQuote(image)), "docker run preview image is not quoted");
    }

    private static void createRunningContainer(ShellDockerExec docker, String image, DockerFixture fixture) {
        ShellDockerRun run = new ShellDockerRun();
        run.setContainerName(fixture.containerName);
        run.setImageName(image);
        run.setD(true);
        run.setI(true);
        run.setT(true);
        run.setRm(false);
        run.setRestart("no");
        run.setParams("--hostname " + fixture.hostname);

        ShellDockerRun.DockerEnv env = new ShellDockerRun.DockerEnv();
        env.setName("JSCH_DOCKER_ENV");
        env.setValue(fixture.envValue);
        run.setEnvs(List.of(env));

        ShellDockerRun.DockerLabel label = new ShellDockerRun.DockerLabel();
        label.setName("jsch.test");
        label.setValue(fixture.labelValue);
        run.setLabels(List.of(label));

        ShellDockerRun.DockerVolume volume = new ShellDockerRun.DockerVolume();
        volume.setOuterVolume(fixture.volumeName);
        volume.setInnerVolume("/data");
        run.setVolumes(List.of(volume));

        ShellDockerRun.DockerPort port = new ShellDockerRun.DockerPort();
        port.setOuterPort(0);
        port.setInnerPort(8080);
        port.setType("tcp");
        run.setPorts(List.of(port));

        String command = docker.docker_run_cmd(run);
        int parameterIndex = command.indexOf("--hostname " + fixture.hostname);
        int imageIndex = command.lastIndexOf(image);
        require(parameterIndex >= 0 && parameterIndex < imageIndex, "docker run parameters were not emitted before the image");
        requireContains(command, "-p 0:8080", "docker run port mapping is missing");
        requireContains(command, "-e " + shellQuote("JSCH_DOCKER_ENV=" + fixture.envValue), "docker run environment is missing or unquoted");
        requireContains(command, "--label " + shellQuote("jsch.test=" + fixture.labelValue), "docker run label is missing or unquoted");
        requireContains(command, "-v " + shellQuote(fixture.volumeName + ":/data"), "docker run volume is missing or unquoted");
        requireContains(command, shellQuote(image), "docker run image is not quoted");
        require(command.trim().endsWith(shellQuote(image)), "docker run image is not the final argument");

        String output = docker.docker_run(run);
        requireNotBlank(output, "docker run returned no container id");
        awaitStatus(docker, fixture.containerName, "running", 15000);
        fixture.containerId = inspect(docker, fixture.containerName, "{{.Id}}");
        require(fixture.containerId.matches("[0-9a-f]{12,64}"), "docker run did not create a valid container");

        String json = docker.docker_inspect(fixture.containerName);
        requireNotBlank(json, "docker inspect returned no container data");
        requireContains(json, fixture.hostname, "container hostname is missing from inspect output");
        requireContains(json, "JSCH_DOCKER_ENV=" + fixture.envValue, "container environment is missing from inspect output");
        requireContains(json, "jsch.test", "container label is missing from inspect output");
        requireContains(json, fixture.labelValue, "container label value is missing from inspect output");
        requireContains(json, fixture.volumeName, "container volume is missing from inspect output");
        requireContains(json, "/data", "container mount target is missing from inspect output");
    }

    private static void requireRunningContainer(ShellDockerExec docker, DockerFixture fixture) {
        List<ShellDockerContainer> running = ShellDockerParser.ps(docker.docker_ps());
        ShellDockerContainer current = findContainer(running, fixture.containerName);
        require(current != null, "running docker ps parser did not return the test container");
        require(current.isRunning(), "test container should be running");
        requireContains(current.getImage(), "easyshell-jsch-test", "docker ps parser returned an unexpected image");
        requireContains(current.getContainerId(), fixture.containerId.substring(0, 12), "docker ps parser returned an unexpected id");

        List<ShellDockerContainer> all = ShellDockerParser.ps(docker.docker_ps_a());
        require(findContainer(all, fixture.containerName) != null, "docker ps -a parser did not return the test container");

        List<ShellDockerPort> ports = ShellDockerParser.port(docker.docker_port(fixture.containerName));
        require(!ports.isEmpty(), "docker port parser returned no mappings");
        require(ports.stream().anyMatch(port -> port.getInnerPort().contains("8080/tcp")
                && port.getOuterPort().contains(":")), "docker port parser did not return the expected mapping");
    }

    private static void requireContainerMetadata(ShellDockerExec docker, DockerFixture fixture) {
        ShellDockerResource resource = ShellDockerParser.resource(docker.docker_resource(fixture.containerName));
        require(resource != null, "docker resource parser returned null");
        require(resource.getMemory() == 0, "new container should not have a memory limit");
        require(resource.getCpuShares() == 0, "new container should not have cpu shares");
    }

    private static void requireDockerUpdate(ShellDockerExec docker, String image, DockerFixture fixture) {
        ShellDockerResource update = new ShellDockerResource();
        update.setMemory(64);
        update.setMemorySwap(128);
        update.setCpuShares(512);
        update.setNanoCpus(500_000_000L);
        String output = docker.docker_update(update, fixture.containerName);
        requireContains(output, fixture.containerName, "docker update did not return the container name");

        ShellDockerResource actual = ShellDockerParser.resource(docker.docker_resource(fixture.containerName));
        require(actual != null, "docker resource parser returned null after update");
        require(actual.getMemory() == 64L * 1024 * 1024, "docker memory limit was not applied");
        require(actual.getMemorySwap() == 128L * 1024 * 1024, "docker memory swap limit was not applied");
        require(actual.getCpuShares() == 512, "docker cpu shares were not applied");
        require(actual.getNanoCpus() == 500_000_000L, "docker cpu limit was not converted to nano cpus");
        require(actual.getCpuPeriod() == 0, "nano cpus should take precedence over cpu period");
        require(actual.getCpuQuota() == 0, "nano cpus should take precedence over cpu quota");

        ShellDockerResource partial = new ShellDockerResource();
        partial.setCpuShares(256);
        docker.docker_update(partial, fixture.containerName);
        actual = ShellDockerParser.resource(docker.docker_resource(fixture.containerName));
        require(actual != null && actual.getCpuShares() == 256, "partial docker update with zero defaults failed");
        require(actual.getMemory() == 64L * 1024 * 1024, "partial update unexpectedly cleared the memory limit");

        ShellDockerRun resourceRun = new ShellDockerRun();
        resourceRun.setContainerName(fixture.resourceContainerName);
        resourceRun.setImageName(image);
        resourceRun.setD(true);
        resourceRun.setI(true);
        resourceRun.setT(true);
        docker.docker_run(resourceRun);
        awaitStatus(docker, fixture.resourceContainerName, "running", 15000);

        ShellDockerResource periodUpdate = new ShellDockerResource();
        periodUpdate.setCpuShares(128);
        periodUpdate.setCpuPeriod(100000);
        periodUpdate.setCpuQuota(50000);
        docker.docker_update(periodUpdate, fixture.resourceContainerName);
        actual = ShellDockerParser.resource(docker.docker_resource(fixture.resourceContainerName));
        require(actual != null && actual.getCpuShares() == 128, "cpu shares were not applied without nano cpus");
        require(actual.getCpuPeriod() == 100000, "docker cpu period was not applied");
        require(actual.getCpuQuota() == 50000, "docker cpu quota was not applied");
        require(actual.getNanoCpus() == 0, "period/quota update unexpectedly set nano cpus");

        docker.docker_rm_f(fixture.resourceContainerName);
        require(!containerExists(docker, fixture.resourceContainerName), "resource test container was not removed");
    }

    private static void requirePauseRestartAndRename(ShellDockerExec docker, DockerFixture fixture) {
        docker.docker_pause(fixture.containerName);
        require("paused".equals(inspect(docker, fixture.containerName, "{{.State.Status}}")), "docker pause did not pause the container");
        ShellDockerContainer paused = findContainer(ShellDockerParser.ps(docker.docker_ps()), fixture.containerName);
        require(paused != null && paused.isPaused() && paused.isRunning(), "paused container parser state is incorrect");

        docker.docker_unpause(fixture.containerName);
        require("running".equals(inspect(docker, fixture.containerName, "{{.State.Status}}")), "docker unpause did not resume the container");

        docker.docker_rename(fixture.containerName, fixture.renamedName);
        require("running".equals(inspect(docker, fixture.renamedName, "{{.State.Status}}")), "docker rename did not rename the container");
        fixture.activeContainerName = fixture.renamedName;

        String before = inspect(docker, fixture.renamedName, "{{.State.StartedAt}}");
        sleep(1100);
        docker.docker_restart(fixture.renamedName);
        awaitStatus(docker, fixture.renamedName, "running", 15000);
        String after = inspect(docker, fixture.renamedName, "{{.State.StartedAt}}");
        require(!before.equals(after), "docker restart did not change the start time");
    }

    private static void requireLogsAndExec(ShellSSHClient client, ShellDockerExec docker, DockerFixture fixture) {
        String execOutput = execChecked(client, "docker exec " + shellQuote(fixture.renamedName)
                + " echo " + shellQuote(fixture.execToken), 30000);
        requireContains(execOutput, fixture.execToken, "docker exec output is missing");

        execChecked(client, "docker exec " + shellQuote(fixture.renamedName)
                + " sh -c " + shellQuote("echo " + fixture.logToken + " > /proc/1/fd/1"), 30000);
        sleep(300);
        String logs = docker.docker_logs(fixture.renamedName);
        requireContains(logs, fixture.logToken, "docker logs did not return the emitted log line");
    }

    private static void requireStopStartAndKill(ShellDockerExec docker, DockerFixture fixture) {
        docker.docker_stop(fixture.renamedName);
        require("exited".equals(inspect(docker, fixture.renamedName, "{{.State.Status}}")), "docker stop did not stop the container");
        require(findContainer(ShellDockerParser.ps(docker.docker_ps()), fixture.renamedName) == null,
                "stopped container should not be returned by docker ps");
        ShellDockerContainer exited = findContainer(ShellDockerParser.ps(docker.docker_ps_exited()), fixture.renamedName);
        require(exited != null && exited.isExited(), "docker ps exited parser did not return the stopped container");

        docker.docker_start(fixture.renamedName);
        awaitStatus(docker, fixture.renamedName, "running", 15000);

        docker.docker_kill(fixture.renamedName);
        awaitStatus(docker, fixture.renamedName, "exited", 15000);
    }

    private static void requireImageLifecycle(ShellSSHClient client, ShellDockerExec docker, DockerFixture fixture) {
        ShellDockerCommit commit = new ShellDockerCommit();
        commit.setContainerId(fixture.containerId);
        commit.setRepository(fixture.commitRepository);
        commit.setTag("test");
        commit.setComment(fixture.commitComment);
        String commitCommand = docker.docker_commit_cmd(commit);
        requireContains(commitCommand, "-m " + shellQuote(fixture.commitComment), "docker commit comment was not quoted");

        String commitOutput = docker.docker_commit(commit);
        requireNotBlank(commitOutput, "docker commit returned no image id");
        require(imageExists(docker, fixture.commitImage), "docker commit image is not visible");

        List<ShellDockerImage> images = ShellDockerParser.images(docker.docker_images());
        require(images.stream().anyMatch(current -> fixture.commitImage.equals(current.getImageName())),
                "docker images parser did not return the committed image");

        List<ShellDockerImageHistory> history = ShellDockerParser.history(docker.docker_history(fixture.commitImage));
        require(!history.isEmpty(), "docker history parser returned no records");
        require(history.stream().anyMatch(current -> current.getComment().contains(fixture.commitComment)),
                "docker history parser did not return the commit comment");

        ShellDockerTag tag = new ShellDockerTag();
        tag.setImageName(fixture.commitImage);
        tag.setNewImageName(fixture.tagImage);
        docker.docker_tag(tag);
        require(imageExists(docker, fixture.tagImage), "docker tag did not create the new image reference");

        ShellDockerSave save = new ShellDockerSave();
        save.setImageName(fixture.commitImage);
        save.setFilePath(fixture.savePath);
        String saveCommand = docker.docker_save_cmd(save);
        requireContains(saveCommand, "docker save -o " + shellQuote(fixture.savePath), "docker save command is malformed");
        docker.docker_save(save);
        String fileState = execChecked(client, "test -s " + shellQuote(fixture.savePath) + " && echo present || echo missing", 30000);
        require("present".equals(fileState.trim()), "docker save did not create a non-empty archive");

        ShellDockerRmi rmi = new ShellDockerRmi();
        rmi.setImageName(fixture.tagImage);
        rmi.setForce(true);
        docker.docker_rmi(rmi);
        require(!imageExists(docker, fixture.tagImage), "docker rmi parameter object did not remove the tag image");

        docker.docker_rmi_f(fixture.commitImage);
        require(!imageExists(docker, fixture.commitImage), "docker rmi -f did not remove the committed image");
    }

    private static void requireRemoval(ShellDockerExec docker, String image, DockerFixture fixture) {
        String rmOutput = docker.docker_rm(fixture.renamedName);
        requireContains(rmOutput, fixture.renamedName, "docker rm did not return the removed container name");
        require(!containerExists(docker, fixture.renamedName), "docker rm did not remove the stopped container");

        ShellDockerRun secondRun = new ShellDockerRun();
        secondRun.setContainerName(fixture.secondContainerName);
        secondRun.setImageName(image);
        secondRun.setD(true);
        secondRun.setI(true);
        secondRun.setT(true);
        docker.docker_run(secondRun);
        awaitStatus(docker, fixture.secondContainerName, "running", 15000);
        String forceRmOutput = docker.docker_rm_f(fixture.secondContainerName);
        requireContains(forceRmOutput, fixture.secondContainerName, "docker rm -f did not return the removed container name");
        require(!containerExists(docker, fixture.secondContainerName), "docker rm -f did not remove the running container");
    }

    private static void requireParserResilience() {
        require(ShellDockerParser.ps("Error response from daemon: unavailable").isEmpty(),
                "docker ps parser should ignore non-tabular error output");
        require(ShellDockerParser.images("Error response from daemon: unavailable").isEmpty(),
                "docker images parser should ignore non-tabular error output");
        require(ShellDockerParser.port("Error response from daemon: unavailable").isEmpty(),
                "docker port parser should ignore invalid output");

        ShellDockerResource resource = ShellDockerParser.resource("invalid\t2\t3\t4\t5\t6");
        require(resource != null && resource.getMemory() == 0 && resource.getMemorySwap() == 2,
                "docker resource parser should preserve valid fields around invalid values");

        List<ShellDockerImageHistory> legacy = ShellDockerParser.history("abc\\r\tcreated\\r\tcommand\\r\t1MB\\r\tcomment");
        require(legacy.size() == 1 && "comment".equals(legacy.getFirst().getComment()),
                "docker history parser should remain compatible with the legacy separator");
        System.out.println("Docker parser resilience verified");
    }

    private static void cleanup(ShellSSHClient client, ShellDockerExec docker, DockerFixture fixture, boolean imageCreated) {
        for (String name : new String[]{fixture.containerName, fixture.renamedName, fixture.activeContainerName,
                fixture.secondContainerName, fixture.resourceContainerName}) {
            if (name != null) {
                client.exec("docker rm -f " + shellQuote(name) + " >/dev/null 2>&1");
            }
        }
        for (String image : new String[]{fixture.commitImage, fixture.tagImage}) {
            if (image != null) {
                client.exec("docker rmi -f " + shellQuote(image) + " >/dev/null 2>&1");
            }
        }
        client.exec("docker volume rm " + shellQuote(fixture.volumeName) + " >/dev/null 2>&1");
        client.exec("rm -f " + shellQuote(fixture.savePath));
        if (imageCreated) {
            client.exec("docker rmi -f " + shellQuote(fixture.image) + " >/dev/null 2>&1");
        }
        if (containerExists(docker, fixture.containerName) || containerExists(docker, fixture.renamedName)
                || containerExists(docker, fixture.secondContainerName) || containerExists(docker, fixture.resourceContainerName)) {
            throw new IllegalStateException("docker test container cleanup failed");
        }
    }

    private static void awaitStatus(ShellDockerExec docker, String name, String expected, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;
        String status = null;
        while (System.currentTimeMillis() < deadline) {
            status = inspect(docker, name, "{{.State.Status}}");
            if (expected.equals(status)) {
                return;
            }
            sleep(200);
        }
        throw new IllegalStateException("docker container " + name + " did not reach " + expected + ", actual: " + status);
    }

    private static String inspect(ShellDockerExec docker, String id, String format) {
        String output = docker.docker_inspect(id, format);
        requireNotBlank(output, "docker inspect returned no output");
        return output.trim();
    }

    private static boolean imageExists(ShellDockerExec docker, String image) {
        String output = docker.docker_inspect(image, "{{.Id}}");
        return output != null && output.trim().startsWith("sha256:");
    }

    private static boolean containerExists(ShellDockerExec docker, String name) {
        String output = docker.docker_inspect(name, "{{.Id}}");
        return output != null && output.trim().matches("[0-9a-f]{12,64}");
    }

    private static ShellDockerContainer findContainer(List<ShellDockerContainer> containers, String name) {
        return containers.stream()
                .filter(container -> name.equals(container.getNames()))
                .findFirst()
                .orElse(null);
    }

    private static String execChecked(ShellSSHClient client, String command, int timeout) {
        String wrapped = command + "; __jsch_docker_rc=$?; printf '\\n" + EXIT_MARKER + ":%s\\n' \"$__jsch_docker_rc\"";
        String output = client.exec(wrapped, timeout);
        if (output == null) {
            throw new IllegalStateException("remote command returned null");
        }
        int markerIndex = output.lastIndexOf(EXIT_MARKER + ":");
        if (markerIndex < 0) {
            throw new IllegalStateException("remote command did not return an exit status: " + output);
        }
        String exitCode = output.substring(markerIndex + EXIT_MARKER.length() + 1).trim();
        String result = output.substring(0, markerIndex).trim();
        if (!"0".equals(exitCode)) {
            throw new IllegalStateException("remote command failed with exit code " + exitCode + ": " + result);
        }
        return result;
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static String value(String property, String[] args, int index) {
        String result = System.getProperty(property);
        if (result == null && args.length > index) {
            result = args[index];
        }
        if (result == null) {
            String name = property.substring("test.".length()).toUpperCase(Locale.ROOT).replace('.', '_');
            result = System.getenv("EASYSHELL_TEST_" + name);
        }
        if (result == null || result.isBlank()) {
            throw new IllegalArgumentException("missing integration test value: " + property);
        }
        return result;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void requireNotBlank(String value, String message) {
        require(value != null && !value.isBlank(), message);
    }

    private static void requireContains(String value, String expected, String message) {
        require(value != null && value.contains(expected), message);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("docker regression interrupted", ex);
        }
    }

    private static String suffix() {
        return Long.toString(System.currentTimeMillis(), 36)
                + Integer.toString(ThreadLocalRandom.current().nextInt(0x1000), 36);
    }

    private static final class DockerFixture {

        private final String suffix = suffix();

        private final String image;

        private final String containerName = "easyshell-jsch-" + this.suffix;

        private final String renamedName = this.containerName + "-renamed";

        private final String secondContainerName = this.containerName + "-rmf";

        private final String resourceContainerName = this.containerName + "-resource";

        private final String volumeName = this.containerName + "-volume";

        private final String hostname = "jsch-host-" + this.suffix;

        private final String envValue = "env value " + this.suffix;

        private final String labelValue = "label value " + this.suffix;

        private final String execToken = "jsch-exec-" + this.suffix;

        private final String logToken = "jsch-log-" + this.suffix;

        private final String commitRepository = "easyshell-jsch-commit-" + this.suffix;

        private final String commitComment = "jsch docker commit comment " + this.suffix;

        private final String commitImage = this.commitRepository + ":test";

        private final String tagImage = "easyshell-jsch-tag-" + this.suffix + ":test";

        private final String savePath = "/tmp/easyshell jsch docker " + this.suffix + ".tar";

        private String containerId;

        private String activeContainerName;

        private DockerFixture(String image) {
            this.image = image;
        }
    }
}
