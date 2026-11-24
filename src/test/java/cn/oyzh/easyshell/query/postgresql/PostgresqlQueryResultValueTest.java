package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlColumnUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLXML;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PostgresqlQueryResultValueTest {

    static {
        DBColumnFieldManager.registerInitializer(DBDialect.POSTGRESQL, ShellPostgresqlColumnUtil::init);
    }

    @Test
    public void keepsByteaValueAsBinary() throws Exception {
        PostgresqlColumn column = new PostgresqlColumn("c_bytea");
        column.setType("BYTEA");
        assertTrue(column.supportBinary());
        byte[] bytes = {0x01, 0x23, 0x45, 0x67};

        Object parsed = new TestQueryResult().parse(column, bytes);

        assertSame(bytes, parsed);

        PostgresqlRecord record = new PostgresqlRecord(
                new PostgresqlColumns(List.of(column)), true);
        record.putValue(column, parsed);
        assertSame(bytes, record.getProperty(column.getName()).get());
    }

    @Test
    public void keepsBitValueAsBinaryBits() throws Exception {
        PostgresqlColumn column = new PostgresqlColumn("c_bit");
        column.setType("BIT");
        assertTrue(column.supportBit());

        Object parsed = new TestQueryResult().parse(column, true);

        assertArrayEquals(new byte[]{1}, (byte[]) parsed);
    }

    @Test
    public void readsXmlValueAsText() throws Exception {
        PostgresqlColumn column = new PostgresqlColumn("c_xml");
        column.setType("XML");
        String xml = "<row><id>1</id><val>10</val></row>";
        AtomicBoolean freed = new AtomicBoolean();
        SQLXML sqlXml = (SQLXML) Proxy.newProxyInstance(
                SQLXML.class.getClassLoader(), new Class<?>[]{SQLXML.class},
                (proxy, method, args) -> {
                    if ("getString".equals(method.getName())) {
                        return xml;
                    }
                    if ("free".equals(method.getName())) {
                        freed.set(true);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        Object parsed = new TestQueryResult().parse(column, sqlXml);

        assertEquals(xml, parsed);
        assertTrue(freed.get());
    }

    private static final class TestQueryResult extends PostgresqlQueryResult {

        private Object parse(PostgresqlColumn column, Object value) throws Exception {
            return this.parseValue(column, value);
        }

        @Override
        public void parseResult(ResultSet resultSet, Connection connection,
                                boolean readonly) {
        }
    }
}
