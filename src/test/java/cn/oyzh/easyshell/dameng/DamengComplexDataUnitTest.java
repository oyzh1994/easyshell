package cn.oyzh.easyshell.dameng;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.util.dameng.ShellDamengColumnUtil;
import cn.oyzh.easyshell.util.dameng.ShellDamengDataUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 达梦复杂字段 SQL 生成测试。
 */
public class DamengComplexDataUnitTest {

    @Before
    public void setUp() {
        ShellDamengColumnUtil.init();
    }

    @Test
    public void insertSqlSupportsBinaryAndSkipsGeneratedColumns() {
        DamengColumn id = column("ID", "BIGINT");
        DamengColumn binary = column("C_LONGVARBINARY", "LONGVARBINARY");
        DamengColumn xml = column("C_XML", "XMLTYPE");
        DamengColumn generated = column("C_VIRTUAL", "VARCHAR");
        generated.setGeneratedExpression("CONCAT(\"C_XML\", '-v')");

        DamengColumns columns = new DamengColumns(List.of(id, binary, xml, generated));
        DamengRecord record = new DamengRecord(columns);
        record.putValue("ID", 1L);
        record.putValue("C_LONGVARBINARY", new byte[]{1, 2, 3});
        record.putValue("C_XML", "<root/>");
        record.putValue("C_VIRTUAL", "ignored");

        String sql = ShellDamengDataUtil.toInsertSql(columns, record, true);
        assertTrue(sql, sql.contains("0x010203"));
        assertTrue(sql, sql.contains("'<root/>'"));
        assertFalse(sql, sql.contains("C_VIRTUAL"));
    }

    private static DamengColumn column(String name, String type) {
        DamengColumn column = new DamengColumn(name);
        column.setType(type);
        column.setSchema("SYSDBA");
        column.setTableName("COMPLEX_TYPES");
        return column;
    }
}
