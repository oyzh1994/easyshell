package cn.oyzh.easyshell.dameng;

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
 * LLM tools bound to a Dameng connection.
 */
public class ShellDamengLLMTools implements LLMTools {

    private ShellDamengClient client;

    private final List<DBRawQueryResults> rawResources = new ArrayList<>();

    public void setClient(ShellDamengClient client) {
        this.client = client;
    }

    @Tool("Execute SQL on the bound Dameng connection")
    public String executeSql(
            @P("Dameng schema name") String schema,
            @P("SQL statement") String sql) {
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

    @Tool("Execute SQL on the bound Dameng connection")
    public String executeSql(
            @P("SQL statement") String sql) {
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

    @Tool("Execute SQL and return raw JDBC result sets")
    public List<ResultSet> executeSqlRaw(
            @P("Dameng schema name") String schema,
            @P("SQL statement") String sql) {
        if (this.client == null) {
            return Collections.emptyList();
        }
        if (schema == null || schema.isBlank() || sql == null || sql.isBlank()) {
            return Collections.emptyList();
        }
        try {
            DBRawQueryResults results = this.client.executeSqlRaw(schema, sql);
            this.rawResources.add(results);
            return results.getResultSets();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Tool("Execute SQL and return raw JDBC result sets")
    public List<ResultSet> executeSqlRaw(
            @P("SQL statement") String sql) {
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
        String base = LLMUtil.readLlmText("dameng_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
