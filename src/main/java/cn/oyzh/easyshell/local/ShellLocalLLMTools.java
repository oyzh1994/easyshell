package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.tool.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Map;

/**
 * 本机命令 LLM 工具。
 *
 * @author oyzh
 * @since 2026-11-01
 */
public class ShellLocalLLMTools implements LLMTools {

    private ShellLocalCommandExecutor executor;

    public void setClient(ShellLocalClient client) {
        this.executor = new ShellLocalCommandExecutor(client == null ? null : client.getCharset());
    }

    public void setExecutor(ShellLocalCommandExecutor executor) {
        this.executor = executor;
    }

    @Tool(name = "get_local_environment", value = "Get read-only information about the local shell environment used by EasyShell")
    public String getLocalEnvironment() {
        if (this.executor == null) {
            return "Local command executor is unavailable.";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : this.executor.environment().entrySet()) {
            result.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        result.append("javaVersion: ").append(Runtime.version()).append('\n');
        result.append("availableProcessors: ").append(Runtime.getRuntime().availableProcessors()).append('\n');
        result.append("defaultCharset: ").append(java.nio.charset.Charset.defaultCharset()).append('\n');
        result.append("maxCommandTimeoutSeconds: ").append(ShellLocalCommandExecutor.MAX_TIMEOUT_SECONDS);
        return result.toString();
    }

    @Tool(name = "get_server_version", value = "Get the local operating system and Java runtime versions")
    public String getServerVersion() {
        return "osName: " + System.getProperty("os.name")
                + "\nosVersion: " + System.getProperty("os.version")
                + "\nosArch: " + System.getProperty("os.arch")
                + "\njavaVersion: " + Runtime.version();
    }

    @Tool(name = "execute_local_command", value = "Execute a command on the local machine")
    public String executeCommand(
            @P(name = "command", value = "The command to execute") String command) {
        return this.execute(command, null, ShellLocalCommandExecutor.DEFAULT_TIMEOUT_SECONDS);
    }

    @Tool(name = "execute_local_command_in_directory",
            value = "Execute a command on the local machine in a specific working directory")
    public String executeCommandInDirectory(
            @P(name = "workingDirectory", value = "Absolute or relative working directory") String workingDirectory,
            @P(name = "command", value = "The command to execute") String command,
            @P(name = "timeoutSeconds", value = "Timeout in seconds, maximum 600") long timeoutSeconds) {
        return this.execute(command, workingDirectory, timeoutSeconds);
    }

    private String execute(String command, String workingDirectory, long timeoutSeconds) {
        if (this.executor == null) {
            return "Local command executor is unavailable.";
        }
        return this.executor.execute(command, workingDirectory, timeoutSeconds).format();
    }

    @Override
    public void close() {
        if (this.executor != null) {
            this.executor.close();
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("local_tool.txt");
        String environment = LLMUtil.readLlmText("local_environment.txt");
        if (this.executor != null) {
            for (Map.Entry<String, String> entry : this.executor.environment().entrySet()) {
                environment = environment.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + environment + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
