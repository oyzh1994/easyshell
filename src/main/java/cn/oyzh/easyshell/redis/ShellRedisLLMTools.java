package cn.oyzh.easyshell.redis;

import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.terminal.redis2.RedisCommandDescriptor;
import cn.oyzh.easyshell.terminal.redis2.RedisCommandExecutor;
import cn.oyzh.easyshell.terminal.redis2.RedisCommandRegistry;
import cn.oyzh.easyshell.terminal.redis2.RedisCommandTokenizer;
import cn.oyzh.easyshell.terminal.redis2.RedisResponseFormatter;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;
import java.util.Map;

/**
 * LLM tools bound to a Redis connection.
 */
public class ShellRedisLLMTools implements LLMTools {

    private int dbIndex;

    private ShellRedisClient client;

    private RedisCommandRegistry registry;

    private ShellRedisCommandSession commandSession;

    private final RedisCommandExecutor commandExecutor = new RedisCommandExecutor();

    public void setClient(ShellRedisClient client) {
        this.client = client;
    }

    @Tool(name = "executeCommand", value = "Execute a Redis command on the bound connection")
    public String executeCommand(
            @P(name = "command", value = "Redis command") String command) {
        return this.executeCommand(command, this.dbIndex);
    }

    @Tool(name = "executeCommandInDatabase", value = "Execute a Redis command on a selected database")
    public String executeCommand(
            @P(name = "command", value = "Redis command") String command,
            @P(name = "dbIndex", value = "Redis database index") int dbIndex) {
        if (this.client == null) {
            return "Redis client is unavailable.";
        }
        if (command == null || command.isBlank()) {
            return "Command is required.";
        }
        if (dbIndex < 0) {
            return "Database index must be non-negative.";
        }
        try {
            List<String> args = RedisCommandTokenizer.tokenize(command);
            if (args.isEmpty()) {
                return "Command is required.";
            }
            if (this.commandSession != null && this.dbIndex != dbIndex) {
                this.closeCommandSession();
            }
            this.dbIndex = dbIndex;
            RedisCommandDescriptor descriptor = this.registry().find(args);
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
            return RedisResponseFormatter.format(response, "\n");
        } catch (Exception ex) {
            this.closeCommandSession();
            return "Execution failed: " + ex.getMessage();
        }
    }

    private RedisCommandRegistry registry() {
        if (this.registry == null) {
            try {
                this.registry = RedisCommandRegistry.load(this.client);
            } catch (Exception ex) {
                this.registry = RedisCommandRegistry.from(Map.of());
            }
        }
        return this.registry;
    }

    private boolean isWriteCommand(RedisCommandDescriptor descriptor) {
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
        String base = LLMUtil.readLlmText("redis_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
