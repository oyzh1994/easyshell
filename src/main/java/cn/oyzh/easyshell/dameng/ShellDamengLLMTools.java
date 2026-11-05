package cn.oyzh.easyshell.dameng;

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
 * LLM tools bound to a Dameng connection.
 */
public class ShellDamengLLMTools implements LLMTools {

    private ShellDamengClient client;

    public void setClient(ShellDamengClient client) {
        this.client = client;
    }

    @Tool(name = "executeSql", value = "Execute SQL on the bound Dameng connection")
    public String executeSql(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank()) {
            return "Schema is required.";
        }
        if (sql == null || sql.isBlank()) {
            return "SQL is required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.executeSql(schema, sql));
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "executeSqlDefaultSchema", value = "Execute SQL on the bound Dameng connection")
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
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank() || sql == null || sql.isBlank()) {
            return "Schema and SQL are required.";
        }
        try (DBRawQueryResults results = this.client.executeSqlRaw(schema, sql)) {
            return LLMDBToolsUtil.formatRaw(results);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "executeSqlRawDefaultSchema", value = "Execute SQL and return a safely serialized JSON representation of JDBC result sets")
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

    @Tool(name = "listSchemas", value = "List Dameng schemas on the bound connection")
    public String listSchemas(
            @P(name = "filter", value = "Optional case-insensitive schema name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var schema : this.client.selectSchemas()) {
                names.add(schema.getName());
            }
            return LLMDBToolsUtil.formatList("schema", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listTables", value = "List tables in a Dameng schema")
    public String listTables(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "filter", value = "Optional case-insensitive table name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank()) {
            return "Schema is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var table : this.client.selectTablesSimple(schema)) {
                names.add(table.getName());
            }
            return LLMDBToolsUtil.formatList("table", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listViews", value = "List views in a Dameng schema")
    public String listViews(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "filter", value = "Optional case-insensitive view name filter") String filter,
            @P(name = "maxResults", value = "Maximum results, default 200 and capped at 1000") int maxResults) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank()) {
            return "Schema is required.";
        }
        try {
            List<String> names = new ArrayList<>();
            for (var view : this.client.selectViewsSimple(schema)) {
                names.add(view.getName());
            }
            return LLMDBToolsUtil.formatList("view", names, filter, maxResults);
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "showCreateTable", value = "Get the CREATE TABLE statement for a Dameng table")
    public String showCreateTable(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "table", value = "Table name") String table) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank() || table == null || table.isBlank()) {
            return "Schema and table are required.";
        }
        try {
            String result = this.client.showCreateTable(schema, table);
            return result == null || result.isBlank() ? "Not found: " + schema + "." + table : result;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "showCreateView", value = "Get the CREATE VIEW statement for a Dameng view")
    public String showCreateView(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "view", value = "View name") String view) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank() || view == null || view.isBlank()) {
            return "Schema and view are required.";
        }
        try {
            String result = this.client.showCreateView(schema, view);
            return result == null || result.isBlank() ? "Not found: " + schema + "." + view : result;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Tool(name = "listIndexes", value = "List indexes for a Dameng table")
    public String listIndexes(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "table", value = "Table name") String table) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank() || table == null || table.isBlank()) {
            return "Schema and table are required.";
        }
        try {
            List<String> indexes = new ArrayList<>();
            for (var index : this.client.indexes(schema, table)) {
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

    @Tool(name = "explainSql", value = "Explain a Dameng SQL statement without executing its data changes")
    public String explainSql(
            @P(name = "schema", value = "Dameng schema name") String schema,
            @P(name = "sql", value = "SQL statement to explain") String sql) {
        if (this.client == null) {
            return "Database client is unavailable.";
        }
        if (schema == null || schema.isBlank() || sql == null || sql.isBlank()) {
            return "Schema and SQL are required.";
        }
        try {
            return LLMDBToolsUtil.format(this.client.explainSql(schema, sql));
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("dameng_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
