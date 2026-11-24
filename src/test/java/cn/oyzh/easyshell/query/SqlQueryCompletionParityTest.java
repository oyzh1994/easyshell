package cn.oyzh.easyshell.query;

import cn.oyzh.easyshell.query.mariadb.ShellMariadbQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.mysql.ShellMysqlQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.oracle.ShellOracleQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.postgresql.ShellPostgresqlQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.sqlserver.ShellSqlServerQueryTokenAnalyzer;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.DBQueryPromptItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SqlQueryCompletionParityTest {

    @Test
    public void uppercaseKeywordsAreSuggested() {
        String sql = "SEL";
        this.assertContains(ShellMysqlQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellMysqlQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f), "SELECT");
        this.assertContains(ShellMariadbQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellMariadbQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f), "SELECT");
        this.assertContains(ShellPostgresqlQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellPostgresqlQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f), "SELECT");
        this.assertContains(ShellOracleQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellOracleQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f), "SELECT");
        this.assertContains(ShellSqlServerQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellSqlServerQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f), "SELECT");
        this.assertContains(ShellSqliteQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellSqliteQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f), "SELECT");
    }

    @Test
    public void promptsAreAvailableAfterWhitespace() {
        String sql = "SELECT * FROM ";
        this.assertNotEmpty(ShellMysqlQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellMysqlQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f));
        this.assertNotEmpty(ShellMariadbQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellMariadbQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f));
        this.assertNotEmpty(ShellPostgresqlQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellPostgresqlQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f));
        this.assertNotEmpty(ShellOracleQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellOracleQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f));
        this.assertNotEmpty(ShellSqlServerQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellSqlServerQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f));
        this.assertNotEmpty(ShellSqliteQueryTokenAnalyzer.INSTANCE.initPrompts(
                ShellSqliteQueryTokenAnalyzer.INSTANCE.currentToken(sql, sql.length()), 0.5f));
    }

    private void assertContains(List<? extends DBQueryPromptItem> items, String content) {
        assertTrue(items.stream().anyMatch(item -> content.equals(item.getContent())));
    }

    private void assertNotEmpty(List<? extends DBQueryPromptItem> items) {
        assertFalse(items.isEmpty());
    }
}
