package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.llm.tool.LLMToolScope;
import cn.oyzh.easyshell.llm.tool.LLMTools;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Map;

/**
 * Local command execution tools available independently of the selected session.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class ShellLocalExecutionLLMTools implements LLMTools {

    private final ShellLocalCommandExecutor executor;

    private final LocalPublicIpResolver publicIpResolver;

    public ShellLocalExecutionLLMTools() {
        this(new ShellLocalCommandExecutor());
    }

    public ShellLocalExecutionLLMTools(ShellLocalCommandExecutor executor) {
        this(executor, new LocalPublicIpResolver());
    }

    ShellLocalExecutionLLMTools(ShellLocalCommandExecutor executor,
                                LocalPublicIpResolver publicIpResolver) {
        this.executor = executor;
        this.publicIpResolver = publicIpResolver;
    }

    @Tool(name = "local_get_public_ip",
            value = "Get the current public egress IP of the machine running EasyShell "
                    + "by querying external IP echo services. This never returns a LAN, loopback, "
                    + "link-local, or reserved address.")
    public String getPublicIp() {
        LocalPublicIpResolver.Resolution resolution = this.publicIpResolver.resolve();
        if (resolution.ip() != null) {
            return "publicIp: " + resolution.ip()
                    + "\nsource: " + resolution.source()
                    + "\nnote: The result is the public egress address observed by an external service. "
                    + "VPN, proxy, or carrier NAT can differ from the physical location.";
        }
        String errors = resolution.errors().isEmpty()
                ? "no endpoint configured"
                : String.join("; ", resolution.errors());
        return "Unable to determine the current public IP. " + errors;
    }

    @Tool(name = "local_execute_command_direct",
            value = "Directly execute a command on the local machine running EasyShell")
    public String executeCommand(
            @P(name = "command", value = "The command to execute") String command) {
        return this.execute(command, null, ShellLocalCommandExecutor.DEFAULT_TIMEOUT_SECONDS);
    }

    @Tool(name = "local_execute_command_in_directory_direct",
            value = "Directly execute a command in a specific local working directory")
    public String executeCommandInDirectory(
            @P(name = "workingDirectory", value = "Absolute or relative local working directory") String workingDirectory,
            @P(name = "command", value = "The command to execute") String command,
            @P(name = "timeoutSeconds", value = "Timeout in seconds, maximum 600") long timeoutSeconds) {
        return this.execute(command, workingDirectory, timeoutSeconds);
    }

    private String execute(String command, String workingDirectory, long timeoutSeconds) {
        return this.executor.execute(command, workingDirectory, timeoutSeconds).format();
    }

    @Override
    public LLMToolScope scope() {
        return LLMToolScope.LOCAL_EXECUTION;
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("local_execution_tool.txt");
        String environment = LLMUtil.readLlmText("local_environment.txt");
        for (Map.Entry<String, String> entry : this.executor.environment().entrySet()) {
            environment = environment.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return base + "\n\n" + environment + "\n\n"
                + LLMUtil.languageSystemMessage() + "\n\n"
                + LLMUtil.executionSystemMessage(executionMode);
    }

    @Override
    public void close() {
        this.executor.close();
    }
}
