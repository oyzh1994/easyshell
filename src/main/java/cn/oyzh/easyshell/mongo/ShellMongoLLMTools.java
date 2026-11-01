package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

/**
 * LLM tools bound to a MongoDB connection.
 */
public class ShellMongoLLMTools implements LLMTools {

    private static final JsonWriterSettings JSON_SETTINGS = JsonWriterSettings.builder()
            .indent(true)
            .build();

    private ShellMongoClient client;

    public void setClient(ShellMongoClient client) {
        this.client = client;
    }

    @Tool(name = "runCommand", value = "Run a standard MongoDB database command encoded as a strict JSON object")
    public String runCommand(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "command", value = "MongoDB command as a strict JSON object") String command) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        if (command == null || command.isBlank()) {
            return "Command is required.";
        }
        try {
            Document result = this.client.runCommand(database, Document.parse(command.strip()));
            if (result == null) {
                return "OK";
            }
            Object ok = result.get("ok");
            String json = result.toJson(JSON_SETTINGS);
            if (ok instanceof Number number && number.doubleValue() != 1.0) {
                return "Execution failed: " + json;
            }
            return json;
        } catch (Exception ex) {
            String message = ex.getMessage();
            return "Execution failed: " + (message == null || message.isBlank()
                    ? ex.getClass().getSimpleName() : message);
        }
    }

    @Tool("Execute a MongoDB shell script on the bound connection")
    public String executeScript(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "script", value = "MongoDB shell script") String script) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        if (script == null || script.isBlank()) {
            return "Script is required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.executeScript(database, script));
        } catch (Exception ex) {
            String message = ex.getMessage();
            return "Execution failed: " + (message == null || message.isBlank()
                    ? ex.getClass().getSimpleName() : message);
        }
    }

    @Tool(name = "mongoExamples", value = "Return MongoDB runCommand and shell syntax examples")
    public String example() {
        return LLMUtil.readLlmText("mongo_example.txt");
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("mongo_tool.txt");
        String examples = LLMUtil.readLlmText("mongo_example.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + examples + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
