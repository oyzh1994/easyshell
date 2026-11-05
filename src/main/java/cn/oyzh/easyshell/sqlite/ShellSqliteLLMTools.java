package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.fx.db.DBRawQueryResults;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * 绑定 SQLite 连接的 LLM 工具。
 *
 * @since 2026-11-02
 */
public class ShellSqliteLLMTools implements LLMTools {

    private ShellSqliteClient client;

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    @Tool(name = "executeSql", value = "Execute SQL on the bound SQLite database file")
    public String executeSql(@P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (sql == null || sql.isBlank()) {
            return "SQL is required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.executeSql(sql));
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "executeSqlRaw", value = "Execute SQL on the bound SQLite database and return a safely serialized JSON representation of JDBC result sets")
    public String executeSqlRaw(@P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (sql == null || sql.isBlank()) {
            return "SQL is required.";
        }
        try (DBRawQueryResults results = this.client.executeSqlRaw(sql)) {
            return LLMDBToolsUtil.formatRaw(results);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listDatabases", value = "List attached SQLite databases on the bound connection")
    public String listDatabases(
            @P(name = "filter", value = "Optional case-insensitive database name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        try {
            return LLMDBToolsUtil.formatList("database", this.client.databaseNames(), filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listTables", value = "List tables in a SQLite database")
    public String listTables(
            @P(name = "database", value = "SQLite database name, usually main") String database,
            @P(name = "filter", value = "Optional case-insensitive table name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        String dbName = database == null || database.isBlank() ? "main" : database.trim();
        try {
            List<String> names = new ArrayList<>();
            for (var table : this.client.selectTablesSimple(dbName)) {
                names.add(table.getName());
            }
            return LLMDBToolsUtil.formatList("table", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listViews", value = "List views in a SQLite database")
    public String listViews(
            @P(name = "database", value = "SQLite database name, usually main") String database,
            @P(name = "filter", value = "Optional case-insensitive view name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        String dbName = database == null || database.isBlank() ? "main" : database.trim();
        try {
            List<String> names = new ArrayList<>();
            for (var view : this.client.selectViewsSimple(dbName)) {
                names.add(view.getName());
            }
            return LLMDBToolsUtil.formatList("view", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "showCreateTable", value = "Get the CREATE TABLE statement for a SQLite table")
    public String showCreateTable(
            @P(name = "database", value = "SQLite database name, usually main") String database,
            @P(name = "table", value = "Table name") String table) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (table == null || table.isBlank()) {
            return "Table is required.";
        }
        String dbName = database == null || database.isBlank() ? "main" : database.trim();
        try {
            String result = this.client.showCreateTable(dbName, table);
            return result == null || result.isBlank() ? "Not found: " + table : result;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "showCreateView", value = "Get the CREATE VIEW statement for a SQLite view")
    public String showCreateView(
            @P(name = "database", value = "SQLite database name, usually main") String database,
            @P(name = "view", value = "View name") String view) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (view == null || view.isBlank()) {
            return "View is required.";
        }
        String dbName = database == null || database.isBlank() ? "main" : database.trim();
        try {
            String result = this.client.showCreateView(dbName, view);
            return result == null || result.isBlank() ? "Not found: " + view : result;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listIndexes", value = "List indexes for a SQLite table")
    public String listIndexes(
            @P(name = "database", value = "SQLite database name, usually main") String database,
            @P(name = "table", value = "Table name") String table) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (table == null || table.isBlank()) {
            return "Table is required.";
        }
        String dbName = database == null || database.isBlank() ? "main" : database.trim();
        try {
            List<String> indexes = new ArrayList<>();
            for (var index : this.client.selectIndexes(dbName, table)) {
                indexes.add(index.getName() + " | table: " + index.getTableName()
                        + " | definition: " + index.getCreateDefinition());
            }
            return LLMDBToolsUtil.formatList("index", indexes, null, 500);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "explainSql", value = "Explain a SQLite SQL statement without executing its data changes")
    public String explainSql(@P(name = "sql", value = "SQL statement to explain") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (sql == null || sql.isBlank()) {
            return "SQL is required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.explainSql(sql));
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("sqlite_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
