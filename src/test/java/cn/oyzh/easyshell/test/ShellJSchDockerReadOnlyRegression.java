package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.docker.ShellDockerContainer;
import cn.oyzh.easyshell.ssh.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh.docker.ShellDockerImage;
import cn.oyzh.easyshell.ssh.docker.ShellDockerParser;
import cn.oyzh.easyshell.ssh.docker.ShellDockerResource;
import cn.oyzh.easyshell.util.ShellUtil;

import java.util.List;
import java.util.Locale;

/**
 * Windows/macOS 远端 Docker 只读回归。仅调用 info、version、ps、images、
 * inspect、resource、port、history 等无状态命令，不创建、删除或修改资源。
 */
public final class ShellJSchDockerReadOnlyRegression {

    private static final int MAX_INSPECT_ITEMS = 3;

    private ShellJSchDockerReadOnlyRegression() {
    }

    public static void main(String[] args) {
        try {
            boolean executed = run();
            if (executed) {
                System.out.println("JSch Docker read-only regression passed");
            } else {
                System.out.println("JSch Docker read-only regression skipped: docker unavailable");
            }
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean run() throws Exception {
        int hostIndex = Integer.getInteger("test.docker.readonly.host.index", 2);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Docker read-only target: " + host);

        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(20000);
            System.out.println("remote platform: windows=" + client.isWindows()
                    + ", macos=" + client.isMacos() + ", linux=" + client.isLinux());
            ShellDockerExec docker = client.dockerExec();
            require(docker != null, "docker executor is null");
            String dockerCommand = client.isWindows() ? "where docker.exe" : "which docker";
            String dockerPath = client.exec(dockerCommand);
            boolean dockerUnavailable = ShellUtil.isCommandNotFound(dockerPath)
                    || (client.isWindows() && ShellUtil.isWindowsCommandNotFound(dockerPath, "docker"));
            System.out.println("docker command lookup: " + (dockerUnavailable ? "unavailable" : "available"));
            if (dockerUnavailable) {
                return false;
            }

            String version = requireHealthy(docker.docker_version(), "docker version");
            requireContains(version, "Client:", "docker version missing client section");
            requireContains(version, "Server:", "docker version missing server section");

            String info = requireHealthy(docker.docker_info(), "docker info");
            String lowerInfo = info.toLowerCase(Locale.ROOT);
            require(lowerInfo.contains("server version") || lowerInfo.contains("containers"),
                    "docker info output is not recognizable");

            String compose = docker.docker_compose_version();
            boolean composeAvailable = compose != null && !compose.isBlank() && !isFailureOutput(compose);
            String composePlugin = client.exec("docker compose version");
            boolean composePluginAvailable = composePlugin != null && !composePlugin.isBlank()
                    && !isFailureOutput(composePlugin);
            require(!composePluginAvailable || composeAvailable,
                    "docker_compose_version did not fall back to the docker compose plugin");

            String psOutput = healthyOrEmpty(docker.docker_ps(), "docker ps");
            String psAllOutput = healthyOrEmpty(docker.docker_ps_a(), "docker ps -a");
            String psExitedOutput = healthyOrEmpty(docker.docker_ps_exited(), "docker ps exited");
            String imagesOutput = healthyOrEmpty(docker.docker_images(), "docker images");

            List<ShellDockerContainer> running = ShellDockerParser.ps(psOutput);
            List<ShellDockerContainer> all = ShellDockerParser.ps(psAllOutput);
            List<ShellDockerContainer> exited = ShellDockerParser.ps(psExitedOutput);
            List<ShellDockerImage> images = ShellDockerParser.images(imagesOutput);
            requireUtf8Clean(psOutput, "docker ps");
            requireUtf8Clean(psAllOutput, "docker ps -a");
            requireUtf8Clean(imagesOutput, "docker images");
            for (ShellDockerContainer container : all) {
                require(!container.getNames().contains("\r"), "docker ps names contains carriage return");
                require(!container.getCommand().contains("\uFFFD"), "docker ps command contains replacement character");
            }
            if (client.isWindows()) {
                String missing = client.execUtf8("where __easyshell_missing_docker_test_command__");
                require(ShellUtil.isWindowsCommandNotFound(missing, "__easyshell_missing_docker_test_command__"),
                        "execUtf8 did not fall back to the Windows remote charset");
            }

            int inspectedContainers = 0;
            int parsedPorts = 0;
            int parsedResources = 0;
            for (ShellDockerContainer container : all) {
                if (inspectedContainers >= MAX_INSPECT_ITEMS) {
                    break;
                }
                String containerId = container.getContainerId();
                require(containerId != null && !containerId.isBlank(), "docker ps returned a blank container id");
                String inspect = requireHealthy(docker.docker_inspect(containerId), "docker inspect");
                require(!inspect.isBlank(), "docker inspect output is blank");
                String resourceOutput = docker.docker_resource(containerId);
                ShellDockerResource resource = ShellDockerParser.resource(resourceOutput);
                require(resource != null, "docker resource parse returned null");
                parsedPorts += ShellDockerParser.port(docker.docker_port(containerId)).size();
                parsedResources++;
                inspectedContainers++;
            }

            int inspectedImages = 0;
            int parsedHistory = 0;
            for (ShellDockerImage image : images) {
                if (inspectedImages >= MAX_INSPECT_ITEMS) {
                    break;
                }
                String imageId = image.getImageId();
                require(imageId != null && !imageId.isBlank(), "docker images returned a blank image id");
                String historyOutput = healthyOrEmpty(docker.docker_history(imageId), "docker history");
                parsedHistory += ShellDockerParser.history(historyOutput).size();
                inspectedImages++;
            }

            requireParsersResilient();
            System.out.println("docker read-only verified: compose=" + composeAvailable
                    + ", composePlugin=" + composePluginAvailable
                    + ", running=" + running.size() + ", all=" + all.size() + ", exited=" + exited.size()
                    + ", images=" + images.size() + ", inspectedContainers=" + inspectedContainers
                    + ", resources=" + parsedResources + ", ports=" + parsedPorts
                    + ", inspectedImages=" + inspectedImages + ", history=" + parsedHistory);
            return true;
        }
    }

    private static String healthyOrEmpty(String output, String command) {
        if (output == null || output.isBlank()) {
            return "";
        }
        require(!isFailureOutput(output), command + " failed: " + firstLine(output));
        return output;
    }

    private static String requireHealthy(String output, String command) {
        require(output != null && !output.isBlank(), command + " output is blank");
        require(!isFailureOutput(output), command + " failed: " + firstLine(output));
        return output;
    }

    private static boolean isFailureOutput(String output) {
        String value = output.toLowerCase(Locale.ROOT);
        return value.contains("command not found")
                || value.contains("not recognized as an internal or external command")
                || value.contains("the term 'docker' is not recognized")
                || value.contains("cannot connect to the docker daemon")
                || value.contains("error during connect")
                || value.contains("failed to connect to the docker")
                || value.contains("is the docker daemon running")
                || value.contains("open //./pipe/docker_engine")
                || value.contains("the system cannot find the file specified");
    }

    private static void requireParsersResilient() {
        List<ShellDockerContainer> containers = ShellDockerParser.ps("Error response from daemon: unavailable");
        require(containers.isEmpty(), "docker ps parser accepted daemon error as a container");
        List<ShellDockerImage> images = ShellDockerParser.images("error during connect");
        require(images.isEmpty(), "docker images parser accepted daemon error as an image");
        ShellDockerResource resource = ShellDockerParser.resource("invalid\t2\t3\t4\t5\t6");
        require(resource != null && resource.getMemory() == 0, "docker resource parser did not tolerate invalid memory");
    }

    private static void requireUtf8Clean(String output, String command) {
        if (output == null) {
            return;
        }
        require(!output.contains("\uFFFD"), command + " contains a UTF-8 replacement character");
    }

    private static String firstLine(String value) {
        int lineEnd = value.indexOf('\n');
        return lineEnd < 0 ? value : value.substring(0, lineEnd);
    }

    private static void requireContains(String value, String expected, String message) {
        require(value.contains(expected), message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
