package cn.oyzh.easyshell.mongo;

import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.util.ArrayList;
import java.util.List;

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

    @Tool(name = "listDatabases", value = "List MongoDB databases on the bound connection")
    public String listDatabases(
            @P(name = "filter", value = "Optional case-insensitive database name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        try {
            return LLMDBToolsUtil.formatList("database", this.client.listDatabaseNames(), filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listCollections", value = "List collections in a MongoDB database")
    public String listCollections(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "filter", value = "Optional case-insensitive collection name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var collection : this.client.listCollections(database)) {
                names.add(collection.getName());
            }
            return LLMDBToolsUtil.formatList("collection", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listBuckets", value = "List GridFS buckets in a MongoDB database")
    public String listBuckets(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "filter", value = "Optional case-insensitive bucket name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var bucket : this.client.listBuckets(database)) {
                names.add(bucket.getName());
            }
            return LLMDBToolsUtil.formatList("bucket", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listFunctions", value = "List stored JavaScript functions in a MongoDB database")
    public String listFunctions(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "filter", value = "Optional case-insensitive function name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var function : this.client.listFunctions(database)) {
                names.add(function.getName());
            }
            return LLMDBToolsUtil.formatList("function", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listUsers", value = "List MongoDB users and roles without exposing passwords")
    public String listUsers(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            List<String> users = new ArrayList<>();
            for (var user : this.client.listUsers(database)) {
                StringBuilder roles = new StringBuilder();
                if (user.getRoles() != null) {
                    for (var role : user.getRoles()) {
                        if (!roles.isEmpty()) {
                            roles.append(',');
                        }
                        roles.append(role.getDb()).append('.').append(role.getRole());
                    }
                }
                users.add(user.getUser() + " | roles: " + roles);
            }
            return LLMDBToolsUtil.formatList("user", users, null, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "getServerVersion", value = "Get the MongoDB server version")
    public String getServerVersion() {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        try {
            return "serverVersion: " + this.client.selectVersion();
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "pingDatabase", value = "Check whether a MongoDB database responds to ping")
    public String pingDatabase(
            @P(name = "database", value = "MongoDB database name") String database) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            return "ping: " + this.client.ping(database);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "countCollection", value = "Count documents in a MongoDB collection using a read-only count command")
    public String countCollection(
            @P(name = "database", value = "MongoDB database name") String database,
            @P(name = "collection", value = "Collection name") String collection) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank() || collection == null || collection.isBlank()) {
            return "Database and collection are required.";
        }
        try {
            Document result = this.client.runCommand(database, new Document("count", collection));
            return result == null ? "OK" : result.toJson(JSON_SETTINGS);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
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
