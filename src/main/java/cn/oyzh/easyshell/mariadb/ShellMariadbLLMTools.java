package cn.oyzh.easyshell.mariadb;

import cn.oyzh.fx.db.DBRawQueryResults;
import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LLM tools bound to a MariaDB connection.
 */
public class ShellMariadbLLMTools implements LLMTools {

    private ShellMariadbClient client;

    private final List<DBRawQueryResults> rawResources = new ArrayList<>();

    public void setClient(ShellMariadbClient client) {
        this.client = client;
    }

    @Tool(name = "executeSql", value = "Execute SQL on the bound MariaDB connection")
    public String executeSql(
            @P(name = "database", value = "MariaDB database name") String database,
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

    @Tool(name = "executeSqlDefaultDatabase", value = "Execute SQL on the bound MariaDB connection")
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

    @Tool(name = "executeSqlRaw", value = "Execute SQL and return raw JDBC result sets")
    public List<ResultSet> executeSqlRaw(
            @P(name = "database", value = "MariaDB database name") String database,
            @P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return Collections.emptyList();
        }
        if (database == null || database.isBlank() || sql == null || sql.isBlank()) {
            return Collections.emptyList();
        }
        try {
            DBRawQueryResults results = this.client.executeSqlRaw(database, sql);
            this.rawResources.add(results);
            return results.getResultSets();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Tool(name = "executeSqlRawDefaultDatabase", value = "Execute SQL and return raw JDBC result sets")
    public List<ResultSet> executeSqlRaw(
            @P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null) {
            return Collections.emptyList();
        }
        if (sql == null || sql.isBlank()) {
            return Collections.emptyList();
        }
        try {
            DBRawQueryResults results = this.client.executeSqlRaw(sql);
            this.rawResources.add(results);
            return results.getResultSets();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public void close() {
        this.rawResources.forEach(DBRawQueryResults::close);
        this.rawResources.clear();
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("mariadb_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
