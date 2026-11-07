package cn.oyzh.easyshell.valkey;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.terminal.valkey2.ValkeyCommandDescriptor;
import cn.oyzh.easyshell.terminal.valkey2.ValkeyCommandExecutor;
import cn.oyzh.easyshell.terminal.valkey2.ValkeyCommandRegistry;
import cn.oyzh.easyshell.terminal.valkey2.ValkeyCommandTokenizer;
import cn.oyzh.easyshell.terminal.valkey2.ValkeyResponseFormatter;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLM tools bound to a Valkey connection.
 */
public class ShellValkeyLLMTools implements LLMTools {

    private int dbIndex;

    private ShellValkeyClient client;

    private ValkeyCommandRegistry registry;

    private ShellValkeyCommandSession commandSession;

    private final ValkeyCommandExecutor commandExecutor = new ValkeyCommandExecutor();

    public void setClient(ShellValkeyClient client) {
        this.client = client;
    }

    @Tool(name = "executeCommand", value = "Execute a Valkey command on the bound connection")
    public String executeCommand(
            @P(name = "command", value = "Valkey command") String command) {
        return this.executeCommand(command, this.dbIndex);
    }

    @Tool(name = "executeCommandInDatabase", value = "Execute a Valkey command on a selected database")
    public String executeCommand(
            @P(name = "command", value = "Valkey command") String command,
            @P(name = "dbIndex", value = "Valkey database index") int dbIndex) {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        if (command == null || command.isBlank()) {
            return "Command is required.";
        }
        if (dbIndex < 0) {
            return "Database index must be non-negative.";
        }
        try {
            List<String> args = ValkeyCommandTokenizer.tokenize(command);
            if (args.isEmpty()) {
                return "Command is required.";
            }
            if (this.commandSession != null && this.dbIndex != dbIndex) {
                this.closeCommandSession();
            }
            this.dbIndex = dbIndex;
            ValkeyCommandDescriptor descriptor = this.registry().find(args);
            if (this.client.isReadonly() && this.isWriteCommand(descriptor)) {
                throw new ShellReadonlyOperationException();
            }
            if (this.commandSession == null) {
                this.commandSession = this.client.openCommandSession(this.dbIndex);
            }
            Object response = this.commandExecutor.execute(args, descriptor, this.commandSession);
            if (args.getFirst().equalsIgnoreCase("select") && args.size() == 2) {
                this.dbIndex = Integer.parseInt(args.get(1));
            }
            return ValkeyResponseFormatter.format(response, "\n");
        } catch (Exception ex) {
            this.closeCommandSession();
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "getServerInfo", value = "Get Valkey server information for one INFO section, such as server, clients, memory, stats, or keyspace")
    public String getServerInfo(
            @P(name = "section", value = "Optional INFO section; empty returns the default section") String section) {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        try {
            String info = this.client.info(section == null || section.isBlank() ? null : section.trim());
            return info == null || info.isBlank() ? "OK" : this.truncate(info);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "getServerVersion", value = "Get the Valkey server version")
    public String getServerVersion() {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        try {
            return "serverVersion: " + this.client.getServerVersion();
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listDatabases", value = "List configured Valkey database indexes without selecting them")
    public String listDatabases() {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        try {
            int count = this.client.databases();
            List<String> indexes = new ArrayList<>(Math.max(count, 0));
            for (int index = 0; index < count; index++) {
                indexes.add(String.valueOf(index));
            }
            return LLMDBToolsUtil.formatList("dbIndex", indexes, null, 1000);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "getDatabaseInfo", value = "Get read-only statistics for one Valkey database")
    public String getDatabaseInfo(
            @P(name = "dbIndex", value = "Valkey database index") int dbIndex) {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        if (dbIndex < 0) {
            return "Database index must be non-negative.";
        }
        try {
            return "dbIndex: " + dbIndex + "\nkeyCount: " + this.client.dbSize(dbIndex);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "getKeyInfo", value = "Get read-only metadata for one Valkey key: type, TTL, memory usage, encoding, reference count, and idle time")
    public String getKeyInfo(
            @P(name = "dbIndex", value = "Valkey database index") int dbIndex,
            @P(name = "key", value = "Valkey key") String key) {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        if (dbIndex < 0) {
            return "Database index must be non-negative.";
        }
        if (key == null || key.isBlank()) {
            return "Key is required.";
        }
        try {
            StringBuilder result = new StringBuilder();
            result.append("dbIndex: ").append(dbIndex).append('\n');
            result.append("key: ").append(key).append('\n');
            result.append("type: ").append(this.client.type(dbIndex, key)).append('\n');
            result.append("ttlSeconds: ").append(this.client.ttl(dbIndex, key)).append('\n');
            result.append("pttlMilliseconds: ").append(this.client.pttl(dbIndex, key)).append('\n');
            result.append("memoryUsageBytes: ").append(this.value(this.client.memoryUsage(dbIndex, key))).append('\n');
            result.append("encoding: ").append(this.value(this.client.objectEncoding(dbIndex, key))).append('\n');
            result.append("refCount: ").append(this.value(this.client.objectRefcount(dbIndex, key))).append('\n');
            result.append("idleSeconds: ").append(this.value(this.client.objectIdletime(dbIndex, key)));
            return result.toString();
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listCommands", value = "List Valkey commands supported by the connected server")
    public String listCommands(
            @P(name = "filter", value = "Optional case-insensitive command name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Valkey client is unavailable.";
        }
        try {
            return LLMDBToolsUtil.formatList("command", this.client.commandList(), filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    private String value(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String truncate(String value) {
        int maxChars = 64 * 1024;
        return value.length() <= maxChars ? value : value.substring(0, maxChars) + "\n... output truncated";
    }

    private ValkeyCommandRegistry registry() {
        if (this.registry == null) {
            try {
                this.registry = ValkeyCommandRegistry.load(this.client);
            } catch (Exception ex) {
                this.registry = ValkeyCommandRegistry.from(Map.of());
            }
        }
        return this.registry;
    }

    private boolean isWriteCommand(ValkeyCommandDescriptor descriptor) {
        return descriptor != null
                && descriptor.flags().stream().anyMatch(flag -> flag.equalsIgnoreCase("write"));
    }

    private void closeCommandSession() {
        if (this.commandSession != null) {
            try {
                this.commandSession.close();
            } catch (Exception ignored) {
            }
            this.commandSession = null;
        }
    }

    @Override
    public void close() {
        this.closeCommandSession();
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("valkey_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
