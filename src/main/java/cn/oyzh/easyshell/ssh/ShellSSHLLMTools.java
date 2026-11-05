package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.ssh.process.ShellProcessInfo;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Comparator;
import java.util.List;

public class ShellSSHLLMTools implements LLMTools {

    private ShellSSHClient client;

    public void setClient(ShellSSHClient client) {
        this.client = client;
    }

    @Tool("执行ssh命令")
    public String execCommand(
            @P(name = "command", value = "命令") String command) {
        return this.client.exec(command);
    }

    @Tool(name = "getConnectionInfo", value = "Get current SSH connection metadata, logged-in user, remote OS, and working directory")
    public String getConnectionInfo() {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            var connect = this.client.getShellConnect();
            StringBuilder result = new StringBuilder();
            result.append("state: ").append(this.client.getState()).append('\n');
            if (connect != null) {
                result.append("name: ").append(this.value(connect.getName())).append('\n');
                result.append("type: ").append(this.value(connect.getType())).append('\n');
                result.append("host: ").append(this.value(connect.getHost())).append('\n');
                result.append("port: ").append(connect.hostPort()).append('\n');
                result.append("user: ").append(this.value(connect.getUser())).append('\n');
                result.append("osType: ").append(this.value(connect.getOsType())).append('\n');
            }
            result.append("whoami: ").append(this.value(this.client.whoami())).append('\n');
            result.append("workingDirectory: ").append(this.value(this.client.workDirProperty().get()));
            return result.toString();
        } catch (Exception ex) {
            return this.failure("Get SSH connection info", ex);
        }
    }

    @Tool(name = "getServerSummary", value = "Get read-only remote server summary: OS, architecture, uptime, timezone, and locale")
    public String getServerSummary() {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            var server = this.client.serverExec();
            StringBuilder result = new StringBuilder();
            result.append("osType: ").append(this.value(server.getOsType())).append('\n');
            result.append("uname: ").append(this.value(server.uname())).append('\n');
            result.append("arch: ").append(this.value(server.arch())).append('\n');
            result.append("uptime: ").append(this.value(server.uptime())).append('\n');
            result.append("timezone: ").append(this.value(server.timezone())).append('\n');
            result.append("locale: ").append(this.value(server.locale()));
            return result.toString();
        } catch (Exception ex) {
            return this.failure("Get server summary", ex);
        }
    }

    @Tool(name = "getCpuInfo", value = "Get read-only remote CPU information")
    public String getCpuInfo() {
        return this.safe(() -> this.client.sshExec().cpu_info(), "Get CPU info");
    }

    @Tool(name = "getMemoryInfo", value = "Get read-only remote memory information")
    public String getMemoryInfo() {
        return this.safe(() -> this.client.sshExec().memory_info(), "Get memory info");
    }

    @Tool(name = "getDiskInfo", value = "Get read-only remote disk usage information")
    public String getDiskInfo() {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            StringBuilder result = new StringBuilder();
            for (var disk : this.client.sshExec().disk_info()) {
                result.append("fileSystem: ").append(this.value(disk.getFileSystem()))
                        .append(" | size: ").append(this.value(disk.getSize()))
                        .append(" | used: ").append(this.value(disk.getUsed()))
                        .append(" | avail: ").append(this.value(disk.getAvail()))
                        .append(" | use: ").append(this.value(disk.getUse()))
                        .append(" | mountedOn: ").append(this.value(disk.getMountedOn()))
                        .append('\n');
            }
            return result.isEmpty() ? "No disk information." : result.toString();
        } catch (Exception ex) {
            return this.failure("Get disk info", ex);
        }
    }

    @Tool(name = "getNetworkInfo", value = "Get read-only remote network interface information")
    public String getNetworkInfo() {
        return this.safe(() -> this.client.sshExec().network_interface_info(), "Get network info");
    }

    @Tool(name = "getGpuInfo", value = "Get read-only remote GPU information")
    public String getGpuInfo() {
        return this.safe(() -> this.client.sshExec().gpu_info(), "Get GPU info");
    }

    @Tool(name = "listProcesses", value = "List remote processes sorted by CPU usage")
    public String listProcesses(
            @P(name = "maxResults", value = "Maximum processes, default 100 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            List<ShellProcessInfo> processes = new java.util.ArrayList<>(this.client.processExec().ps());
            processes.sort(Comparator.comparingDouble(ShellProcessInfo::getCpuUsage).reversed());
            int limit = maxResults <= 0 ? 100 : Math.min(maxResults, 1000);
            int shown = Math.min(processes.size(), limit);
            StringBuilder result = new StringBuilder();
            result.append("count: ").append(processes.size()).append('\n');
            result.append("shown: ").append(shown).append('\n');
            for (int index = 0; index < shown; index++) {
                ShellProcessInfo process = processes.get(index);
                result.append("pid: ").append(process.getPid())
                        .append(" | user: ").append(this.value(process.getUser()))
                        .append(" | cpu: ").append(process.getCpuUsage())
                        .append(" | mem: ").append(process.getMemUsage())
                        .append(" | start: ").append(this.value(process.getStart()))
                        .append(" | command: ").append(this.value(process.getCommand()))
                        .append('\n');
            }
            return result.toString();
        } catch (Exception ex) {
            return this.failure("List processes", ex);
        }
    }

    @Tool(name = "listDockerContainers", value = "List Docker containers on the remote server")
    public String listDockerContainers(
            @P(name = "includeStopped", value = "Whether to include stopped containers") boolean includeStopped) {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            var docker = this.client.dockerExec();
            String result = includeStopped ? docker.docker_ps_a() : docker.docker_ps();
            return result == null || result.isBlank() ? "No containers." : result;
        } catch (Exception ex) {
            return this.failure("List Docker containers", ex);
        }
    }

    @Tool(name = "listDockerImages", value = "List Docker images on the remote server")
    public String listDockerImages() {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            String result = this.client.dockerExec().docker_images();
            return result == null || result.isBlank() ? "No images." : result;
        } catch (Exception ex) {
            return this.failure("List Docker images", ex);
        }
    }

    @Tool(name = "getDockerInfo", value = "Get read-only Docker daemon information")
    public String getDockerInfo() {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            return this.client.dockerExec().docker_info();
        } catch (Exception ex) {
            return this.failure("Get Docker info", ex);
        }
    }

    @Tool(name = "getDockerVersion", value = "Get read-only Docker version information")
    public String getDockerVersion() {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            return this.client.dockerExec().docker_version();
        } catch (Exception ex) {
            return this.failure("Get Docker version", ex);
        }
    }

    private String safe(Command command, String action) {
        if (this.client == null) {
            return "SSH client is unavailable.";
        }
        try {
            return command.run();
        } catch (Exception ex) {
            return this.failure(action, ex);
        }
    }

    private String failure(String action, Exception ex) {
        String message = ex.getMessage();
        return action + " failed: " + (message == null || message.isBlank()
                ? ex.getClass().getSimpleName() : message);
    }

    private String value(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "-" : text;
    }

    @FunctionalInterface
    private interface Command {
        String run() throws Exception;
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("ssh_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
