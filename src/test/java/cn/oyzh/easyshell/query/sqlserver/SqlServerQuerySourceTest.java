package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class SqlServerQuerySourceTest {

    @Test
    public void fillsSourceTableForSimpleSelect() {
        SqlServerExecuteResult result = resultWithIdentityColumn(
                "SELECT * FROM [demo2].[t_log]");

        assertEquals("demo2", result.schema());
        assertEquals("t_log", result.tableName());
        assertEquals(true, result.isUpdatable());
    }

    @Test
    public void fillsSourceTableForPlainColumnSelect() {
        SqlServerExecuteResult result = resultWithIdentityColumn(
                "SELECT TOP 10 [id], [log] FROM [demo2].[t_log] ORDER BY [id]");

        assertEquals("demo2", result.schema());
        assertEquals("t_log", result.tableName());
    }

    @Test
    public void ignoresCommentsStringsAndTableHints() {
        SqlServerExecuteResult result = resultWithIdentityColumn("""
                SELECT t.*
                FROM [demo2].[t_log] AS t WITH (NOLOCK)
                WHERE [log] = 'FROM another_table'
                """);

        assertEquals("demo2", result.schema());
        assertEquals("t_log", result.tableName());
    }

    @Test
    public void leavesJoinResultReadonlyWhenTableCannotBeDetermined() {
        SqlServerExecuteResult result = resultWithIdentityColumn(
                "SELECT t1.* FROM [demo2].[t_log] t1 JOIN [demo2].[t_log] t2 ON t1.[id] = t2.[id]");

        assertNull(result.tableName());
        assertFalse(result.isUpdatable());
    }

    @Test
    public void leavesMultipleSourceResultReadonly() {
        SqlServerExecuteResult result = resultWithIdentityColumn(
                "SELECT a.[id] FROM [demo2].[t_log] a, [demo2].[t_log] b");

        assertNull(result.tableName());
        assertFalse(result.isUpdatable());
    }

    @Test
    public void leavesComputedResultReadonly() {
        SqlServerExecuteResult result = resultWithIdentityColumn(
                "SELECT [id] + 1 AS [id] FROM [demo2].[t_log]");

        assertNull(result.tableName());
        assertFalse(result.isUpdatable());
    }

    @Test
    public void leavesAliasedResultReadonly() {
        SqlServerExecuteResult result = resultWithIdentityColumn(
                "SELECT [id] AS [other_id] FROM [demo2].[t_log]");

        assertNull(result.tableName());
        assertFalse(result.isUpdatable());
    }

    private SqlServerExecuteResult resultWithIdentityColumn(String sql) {
        SqlServerColumn id = new SqlServerColumn("id");
        id.setAutoIncrement(true);
        SqlServerColumns columns = new SqlServerColumns();
        columns.add(id);

        SqlServerExecuteResult result = new SqlServerExecuteResult();
        result.setContent(sql);
        result.setColumns(columns);
        result.fillSource();
        return result;
    }
}
