package cn.oyzh.easyshell.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.terminal.zk2.ZKCommandDescriptor;
import cn.oyzh.easyshell.terminal.zk2.ZKCommandExecutor;
import cn.oyzh.easyshell.terminal.zk2.ZKCommandRegistry;
import cn.oyzh.easyshell.terminal.zk2.ZKCommandTokenizer;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * LLM tools bound to a ZooKeeper connection.
 */
public class ShellZKLLMTools implements LLMTools {

    private ShellZKClient client;

    private final ZKCommandExecutor commandExecutor = new ZKCommandExecutor();

    public void setClient(ShellZKClient client) {
        this.client = client;
    }

    @Tool("Execute a ZooKeeper command on the bound connection")
    public String executeCommand(
            @P(name = "command", value = "ZooKeeper command") String command) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (command == null || command.isBlank()) {
            return "Command is required.";
        }
        try {
            String input = command.strip();
            if (input.endsWith(";")) {
                input = input.substring(0, input.length() - 1).stripTrailing();
            }
            List<String> args = ZKCommandTokenizer.tokenize(input);
            if (args.isEmpty()) {
                return "Command is required.";
            }
            if (args.getFirst().equalsIgnoreCase("rmr")) {
                args.set(0, "deleteall");
            }
            ZKCommandDescriptor descriptor = ZKCommandRegistry.find(args)
                    .orElseThrow(() -> new IllegalArgumentException("Command not found: " + args.getFirst()));
            ZooKeeper zooKeeper = null;
            if (descriptor.type() == ZKCommandDescriptor.Type.CLI
                    && !args.getFirst().equalsIgnoreCase("version")) {
                zooKeeper = this.client.getZooKeeper();
                if (zooKeeper == null) {
                    return "Execution failed: ZooKeeper client is not connected.";
                }
            }
            ShellConnect connect = this.client.getShellConnect();
            String host = connect == null ? "" : connect.hostIp();
            int port = connect == null ? 0 : connect.hostPort();
            String result = this.commandExecutor.execute(descriptor, args, zooKeeper, host, port);
            return result == null || result.isBlank() ? "OK" : result;
        } catch (Exception ex) {
            String message = ex.getMessage();
            return "Execution failed: " + (message == null || message.isBlank()
                    ? ex.getClass().getSimpleName() : message);
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("zk_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
