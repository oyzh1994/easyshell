package cn.oyzh.easyshell.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
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
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

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

    @Tool(name = "getNodeData", value = "Read data from one ZooKeeper node")
    public String getNodeData(
            @P(name = "path", value = "ZooKeeper node path, for example /app/config") String path) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (path == null || path.isBlank()) {
            return "Node path is required.";
        }
        try {
            String data = this.client.getDataString(path.trim());
            return data == null ? "" : data;
        } catch (Exception ex) {
            return "Execution failed: " + (ex.getMessage() == null || ex.getMessage().isBlank()
                    ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    @Tool(name = "listChildren", value = "List direct children of one ZooKeeper node")
    public String listChildren(
            @P(name = "path", value = "ZooKeeper node path") String path,
            @P(name = "filter", value = "Optional case-insensitive child name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (path == null || path.isBlank()) {
            return "Node path is required.";
        }
        try {
            return LLMDBToolsUtil.formatList("child", this.client.getChildren(path.trim()), filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + (ex.getMessage() == null || ex.getMessage().isBlank()
                    ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    @Tool(name = "checkNodeExists", value = "Check whether a ZooKeeper node exists")
    public String checkNodeExists(
            @P(name = "path", value = "ZooKeeper node path") String path) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (path == null || path.isBlank()) {
            return "Node path is required.";
        }
        try {
            return "path: " + path.trim() + "\nexists: " + this.client.exists(path.trim());
        } catch (Exception ex) {
            return "Execution failed: " + (ex.getMessage() == null || ex.getMessage().isBlank()
                    ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    @Tool(name = "getNodeStat", value = "Get ZooKeeper node metadata such as version, timestamps, data length, and child count")
    public String getNodeStat(
            @P(name = "path", value = "ZooKeeper node path") String path) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (path == null || path.isBlank()) {
            return "Node path is required.";
        }
        try {
            Stat stat = this.client.checkExists(path.trim());
            if (stat == null) {
                return "Not found: " + path.trim();
            }
            return "path: " + path.trim()
                    + "\nczxid: " + stat.getCzxid()
                    + "\nmzxid: " + stat.getMzxid()
                    + "\npzxid: " + stat.getPzxid()
                    + "\nctime: " + stat.getCtime()
                    + "\nmtime: " + stat.getMtime()
                    + "\nversion: " + stat.getVersion()
                    + "\ncversion: " + stat.getCversion()
                    + "\naversion: " + stat.getAversion()
                    + "\nephemeralOwner: " + stat.getEphemeralOwner()
                    + "\ndataLength: " + stat.getDataLength()
                    + "\nnumChildren: " + stat.getNumChildren();
        } catch (Exception ex) {
            return "Execution failed: " + (ex.getMessage() == null || ex.getMessage().isBlank()
                    ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    @Tool(name = "getNodeAcl", value = "Get the ACL entries of one ZooKeeper node")
    public String getNodeAcl(
            @P(name = "path", value = "ZooKeeper node path") String path) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (path == null || path.isBlank()) {
            return "Node path is required.";
        }
        try {
            List<ACL> aclList = this.client.getACL(path.trim());
            if (aclList == null || aclList.isEmpty()) {
                return "No ACL entries.";
            }
            StringBuilder result = new StringBuilder("path: ").append(path.trim()).append('\n');
            for (ACL acl : aclList) {
                result.append("scheme: ").append(acl.getId().getScheme())
                        .append(" | id: ").append(acl.getId().getId())
                        .append(" | perms: ").append(acl.getPerms())
                        .append('\n');
            }
            return result.toString();
        } catch (Exception ex) {
            return "Execution failed: " + (ex.getMessage() == null || ex.getMessage().isBlank()
                    ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    @Tool(name = "getAllChildrenCount", value = "Count all descendants below one ZooKeeper node")
    public String getAllChildrenCount(
            @P(name = "path", value = "ZooKeeper node path") String path) {
        if (this.client == null) {
            return "ZooKeeper client is unavailable.";
        }
        if (path == null || path.isBlank()) {
            return "Node path is required.";
        }
        try {
            return "path: " + path.trim() + "\ndescendantCount: " + this.client.getAllChildrenNumber(path.trim());
        } catch (Exception ex) {
            return "Execution failed: " + (ex.getMessage() == null || ex.getMessage().isBlank()
                    ? ex.getClass().getSimpleName() : ex.getMessage());
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
