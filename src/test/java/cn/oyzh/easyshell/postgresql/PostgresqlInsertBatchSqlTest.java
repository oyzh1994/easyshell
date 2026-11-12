package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlUtil;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PostgresqlInsertBatchSqlTest {

    @Test
    public void parsesQualifiedInsertTarget() {
        assertArrayEquals(new String[]{"demo4", "t2"},
                ShellPostgresqlUtil.parseInsertTarget(
                        "INSERT INTO \"demo4\".\"t2\" (\"id\", \"name\") VALUES (5, 'a3')"));
    }

    @Test
    public void addsOverridingSystemValueBeforeValues() {
        assertEquals(
                "INSERT INTO \"demo4\".\"t2\" (\"id\", \"name\") OVERRIDING SYSTEM VALUE VALUES (5, 'a3')",
                ShellPostgresqlUtil.addOverridingSystemValue(
                        "INSERT INTO \"demo4\".\"t2\" (\"id\", \"name\") VALUES (5, 'a3')"));
    }

    @Test
    public void keepsExistingOverrideClause() {
        String sql = "INSERT INTO t2 (id) OVERRIDING SYSTEM VALUE VALUES (5)";
        assertEquals(sql, ShellPostgresqlUtil.addOverridingSystemValue(sql));
    }

    @Test
    public void keepsDefaultValuesInsert() {
        String sql = "INSERT INTO t2 DEFAULT VALUES";
        assertEquals(sql, ShellPostgresqlUtil.addOverridingSystemValue(sql));
    }
}
