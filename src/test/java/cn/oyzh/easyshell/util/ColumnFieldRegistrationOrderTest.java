package cn.oyzh.easyshell.util;

import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoColumnUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlColumnUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteColumnUtil;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerColumnUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ColumnFieldRegistrationOrderTest {

    @Test
    public void commonColumnTypesAreRegisteredFirst() {
        assertCommonTypes(DBDialect.MYSQL, ShellMysqlColumnUtil::init, List.of(
                "INT", "BIGINT", "VARCHAR", "CHAR", "DECIMAL", "BIT",
                "DATETIME", "DATE", "TIMESTAMP", "TINYINT", "SMALLINT", "TEXT"
        ));
        assertCommonTypes(DBDialect.MARIADB, ShellMariadbColumnUtil::init, List.of(
                "INT", "BIGINT", "VARCHAR", "CHAR", "DECIMAL", "BIT",
                "DATETIME", "DATE", "TIMESTAMP", "TINYINT", "SMALLINT", "TEXT"
        ));
        assertCommonTypes(DBDialect.DAMENG, ShellDamengColumnUtil::init, List.of(
                "INT", "BIGINT", "VARCHAR", "VARCHAR2", "DECIMAL", "NUMBER",
                "DATE", "TIMESTAMP", "DATETIME", "CHAR", "NVARCHAR2", "CLOB", "BLOB"
        ));
        assertCommonTypes(DBDialect.ORACLE, ShellOracleColumnUtil::init, List.of(
                "NUMBER", "INT", "BIGINT", "VARCHAR2", "VARCHAR", "DECIMAL",
                "DATE", "TIMESTAMP", "CLOB", "BLOB", "CHAR", "NVARCHAR2"
        ));
        assertCommonTypes(DBDialect.POSTGRESQL, ShellPostgresqlColumnUtil::init, List.of(
                "INT", "BIGINT", "VARCHAR", "TEXT", "NUMERIC", "BOOLEAN",
                "TIMESTAMP", "DATE", "UUID", "JSONB", "JSON", "BYTEA"
        ));
        assertCommonTypes(DBDialect.SQLSERVER, ShellSqlServerColumnUtil::init, List.of(
                "INT", "BIGINT", "NVARCHAR", "VARCHAR", "DECIMAL", "BIT",
                "DATETIME2", "DATE", "DATETIME", "UNIQUEIDENTIFIER"
        ));
        assertCommonTypes(DBDialect.SQLITE, ShellSqliteColumnUtil::init, List.of(
                "INTEGER", "TEXT", "REAL", "BLOB", "NUMERIC", "BOOLEAN",
                "DATETIME", "DATE", "TIME", "JSON"
        ));
        assertCommonTypes(DBDialect.MONGODB, ShellMongoColumnUtil::init, List.of(
                "STRING", "INT", "LONG", "DOUBLE", "BOOLEAN", "DATE"
        ));
    }

    private void assertCommonTypes(DBDialect dialect, Runnable initializer, List<String> expected) {
        initializer.run();
        List<String> names = DBColumnFieldManager.fieldNames(dialect);
        assertEquals(dialect + " common field order", expected, names.subList(0, expected.size()));
        initializer.run();
        assertEquals(dialect + " field registration is not idempotent",
                names.size(), DBColumnFieldManager.fieldNames(dialect).size());
    }
}
