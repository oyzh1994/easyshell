package cn.oyzh.easyshell.data;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbDataUtil;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlDataUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import dm.jdbc.driver.DmdbBFile;
import dm.jdbc.driver.DmdbBlob;
import dm.jdbc.driver.DmdbIntervalYM;
import dm.jdbc.driver.DmdbRowId;
import org.junit.Test;
import org.mariadb.jdbc.MariaDbBlob;
import org.mariadb.jdbc.MariaDbClob;
import org.mariadb.jdbc.type.Point;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGInterval;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DatabaseValueStandardizationTest {

    static {
        DBColumnFieldManager.registerInitializer(DBDialect.MYSQL, ShellMysqlColumnUtil::init);
        DBColumnFieldManager.registerInitializer(DBDialect.MARIADB, ShellMariadbColumnUtil::init);
    }

    @Test
    public void standardizesMysqlDriverValues() {
        byte[] bytes = {1, 2, 3};
        assertArrayEquals(bytes, (byte[]) ShellMysqlDataUtil.valueStandardization(
                new com.mysql.cj.jdbc.Blob(bytes, null)));
        assertEquals("mysql clob", ShellMysqlDataUtil.valueStandardization(
                new com.mysql.cj.jdbc.Clob("mysql clob", null)));

        MysqlColumn bitColumn = new MysqlColumn();
        bitColumn.setType("BIT");
        assertArrayEquals(new byte[]{1},
                (byte[]) ShellMysqlDataUtil.valueStandardization(bitColumn, true));
    }

    @Test
    public void standardizesMariadbDriverValues() {
        byte[] bytes = "mariadb blob".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(bytes, (byte[]) ShellMariadbDataUtil.valueStandardization(new MariaDbBlob(bytes)));
        assertEquals("mariadb clob", ShellMariadbDataUtil.valueStandardization(new MariaDbClob(
                "mariadb clob".getBytes(StandardCharsets.UTF_8))));

        Point point = new Point(1, 2);
        assertEquals(point.toString(), ShellMariadbDataUtil.valueStandardization(point));

        MariadbColumn bitColumn = new MariadbColumn();
        bitColumn.setType("BIT");
        assertArrayEquals(new byte[]{0},
                (byte[]) ShellMariadbDataUtil.valueStandardization(bitColumn, false));
    }

    @Test
    public void standardizesDamengDriverValues() throws Exception {
        byte[] bytes = {7, 8, 9};
        assertArrayEquals(bytes, (byte[]) ShellDamengDataUtil.valueStandardization(new DmdbBlob(bytes, null)));

        DmdbIntervalYM interval = new DmdbIntervalYM("INTERVAL '2-5' YEAR TO MONTH");
        assertEquals(interval.toString(), ShellDamengDataUtil.valueStandardization(interval));

        DmdbRowId rowId = DmdbRowId.valueOf(123L);
        assertEquals(rowId.toString(), ShellDamengDataUtil.valueStandardization(rowId));

        DmdbBFile bFile = new DmdbBFile("DATA", "demo.dat");
        assertEquals(bFile.getFullName(), ShellDamengDataUtil.valueStandardization(bFile));
    }

    @Test
    public void standardizesPostgresqlDriverValues() throws Exception {
        PGpoint point = new PGpoint(1, 2);
        assertEquals(point.getValue(), ShellPostgresqlDataUtil.valueStandardization(point));

        PGInterval interval = new PGInterval("1 day 02:03:04");
        assertEquals(interval.getValue(), ShellPostgresqlDataUtil.valueStandardization(interval));

        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), ShellPostgresqlDataUtil.valueStandardization(uuid));
    }

    @Test
    public void standardizesSqliteJdbcValues() {
        assertNull(ShellSqliteDataUtil.valueStandardization(null));

        byte[] bytes = {4, 5, 6};
        assertArrayEquals(bytes, (byte[]) ShellSqliteDataUtil.valueStandardization(bytes));
        assertEquals("'quoted'", ShellSqliteDataUtil.valueStandardization("'quoted'"));
        assertEquals("sqlite clob", ShellSqliteDataUtil.valueStandardization(new MariaDbClob(
                "sqlite clob".getBytes(StandardCharsets.UTF_8))));
    }
}
