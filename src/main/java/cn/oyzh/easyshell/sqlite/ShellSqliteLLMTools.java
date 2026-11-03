package cn.oyzh.easyshell.sqlite;

import cn.oyzh.easyshell.llm.LLMDBToolsUtil;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.fx.db.DBRawQueryResults;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 绑定 SQLite 连接的 LLM 工具。
 *
 * @since 2026-11-02
 */
public class ShellSqliteLLMTools implements LLMTools {

    private ShellSqliteClient client;

    private final List<DBRawQueryResults> rawResources = new ArrayList<>();

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

    @Tool(name = "executeSqlRaw", value = "Execute SQL on the bound SQLite database and return raw JDBC result sets")
    public List<ResultSet> executeSqlRaw(@P(name = "sql", value = "SQL statement") String sql) {
        if (this.client == null || sql == null || sql.isBlank()) {
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
        String base = LLMUtil.readLlmText("sqlite_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}
