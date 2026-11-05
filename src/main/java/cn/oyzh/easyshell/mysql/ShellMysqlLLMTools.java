package cn.oyzh.easyshell.mysql;

import cn.oyzh.fx.db.DBRawQueryResults;
import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM tools bound to a MySQL connection.
 */
public class ShellMysqlLLMTools implements LLMTools {

    private ShellMysqlClient client;

    public void setClient(ShellMysqlClient client) {
        this.client = client;
    }

    @Tool(name = "executeSql", value = "Execute SQL on the bound MySQL connection")
    public String executeSql(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        if (sql == null || sql.isBlank()) {
            return "SQL is required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.executeSql(database, sql));
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "executeSqlDefaultDatabase", value = "Execute SQL on the bound MySQL connection")
    public String executeSql(
            @P(name = "sql", value = "SQL statement") String sql) {
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

    @Tool(name = "executeSqlRaw", value = "Execute SQL and return a safely serialized JSON representation of JDBC result sets")
    public String executeSqlRaw(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank() || sql == null || sql.isBlank()) {
            return "Database and SQL are required.";
        }
        try (DBRawQueryResults results = this.client.executeSqlRaw(database, sql)) {
            return LLMDBToolsUtil.formatRaw(results);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "executeSqlRawDefaultDatabase", value = "Execute SQL and return a safely serialized JSON representation of JDBC result sets")
    public String executeSqlRaw(
            @P(name = "sql", value = "SQL statement") String sql) {
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

    @Tool(name = "listDatabases", value = "List MySQL databases on the bound connection")
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

    @Tool(name = "listTables", value = "List tables in a MySQL database")
    public String listTables(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "filter", value = "Optional case-insensitive table name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var table : this.client.selectTablesSimple(database)) {
                names.add(table.getName());
            }
            return LLMDBToolsUtil.formatList("table", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listViews", value = "List views in a MySQL database")
    public String listViews(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "filter", value = "Optional case-insensitive view name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank()) {
            return "Database is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var view : this.client.selectViewsSimple(database)) {
                names.add(view.getName());
            }
            return LLMDBToolsUtil.formatList("view", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "showCreateTable", value = "Get the CREATE TABLE statement for a MySQL table")
    public String showCreateTable(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "table", value = "Table name") String table) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank() || table == null || table.isBlank()) {
            return "Database and table are required.";
        }
        try {
            String result = this.client.showCreateTable(database, table);
            return result == null || result.isBlank() ? "Not found: " + database + "." + table : result;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "showCreateView", value = "Get the CREATE VIEW statement for a MySQL view")
    public String showCreateView(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "view", value = "View name") String view) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank() || view == null || view.isBlank()) {
            return "Database and view are required.";
        }
        try {
            String result = this.client.showCreateView(database, view);
            return result == null || result.isBlank() ? "Not found: " + database + "." + view : result;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listIndexes", value = "List indexes for a MySQL table")
    public String listIndexes(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "table", value = "Table name") String table) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank() || table == null || table.isBlank()) {
            return "Database and table are required.";
        }
        try {
            List<String> indexes = new ArrayList<>();
            for (var index : this.client.selectIndexes(database, table)) {
                StringBuilder columns = new StringBuilder();
                for (var column : index.getColumns()) {
                    if (!columns.isEmpty()) {
                        columns.append(',');
                    }
                    columns.append(column.getColumnName());
                }
                indexes.add(index.getName() + " | unique: " + index.isUnique()
                        + " | type: " + index.getType() + " | columns: " + columns);
            }
            return LLMDBToolsUtil.formatList("index", indexes, null, 500);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "explainSql", value = "Explain a MySQL SQL statement without executing its data changes")
    public String explainSql(
            @P(name = "database", value = "MySQL database name") String database,
            @P(name = "sql", value = "SQL statement to explain") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (database == null || database.isBlank() || sql == null || sql.isBlank()) {
            return "Database and SQL are required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.explainSql(database, sql));
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("mysql_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
