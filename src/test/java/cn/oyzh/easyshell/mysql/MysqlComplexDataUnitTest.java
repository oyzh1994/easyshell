package cn.oyzh.easyshell.mysql;

import cn.oyzh.easyshell.data.mysql.ShellMysqlDataImportHelper;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * MySQL 复杂字段参数化回归测试。
 */
public class MysqlComplexDataUnitTest {

    @Before
    public void setUp() {
        ShellMysqlColumnUtil.init();
    }

    @Test
    public void stringPutValueKeepsColumnMetadata() {
        MysqlColumn point = column("c_point", "POINT", "complex_types");
        MysqlRecord record = new MysqlRecord(new MysqlColumns(List.of(point)));
        record.putValue("c_point", "POINT(1 2)");

        assertTrue(record.getRecordData().isTypeGeometry("c_point"));
    }

    @Test
    public void parsesGeneratedColumnStorage() {
        MysqlColumn virtual = column("c_virtual", "VARCHAR(100)", "complex_types");
        virtual.parseExtra("VIRTUAL GENERATED");
        MysqlColumn stored = column("c_stored", "VARCHAR(100)", "complex_types");
        stored.parseExtra("STORED GENERATED");
        MysqlColumn defaultGenerated = column("c_created", "TIMESTAMP", "complex_types");
        defaultGenerated.parseExtra("DEFAULT_GENERATED on update CURRENT_TIMESTAMP");

        assertTrue(virtual.isGenerated());
        assertTrue(virtual.isVirtual());
        assertTrue(stored.isGenerated());
        assertFalse(stored.isVirtual());
        assertFalse(defaultGenerated.isGenerated());
        assertFalse(virtual.supportDefaultValue());
    }

    @Test
    public void importSqlRestoresSpecialLiteralsAndSkipsGeneratedColumns() throws Exception {
        MysqlColumn id = column("id", "BIGINT", "complex_types");
        MysqlColumn bit = column("c_bit", "BIT(13)", "complex_types");
        MysqlColumn bool = column("c_bool", "TINYINT(1)", "complex_types");
        MysqlColumn json = column("c_json", "JSON", "complex_types");
        MysqlColumn point = column("c_point", "POINT", "complex_types");
        MysqlColumn generated = column("c_virtual", "VARCHAR(100)", "complex_types");
        generated.parseExtra("VIRTUAL GENERATED");

        MysqlColumns columns = new MysqlColumns(List.of(id, bit, bool, json, point, generated));
        MysqlRecord record = new MysqlRecord(columns);
        record.putValue("id", "1");
        record.putValue("c_bit", "b'0001101000101011'");
        record.putValue("c_bool", "true");
        record.putValue("c_json", "{\"name\":\"张三\",\"quote\":\"a\\\"b\"}");
        record.putValue("c_point", "ST_GeomFromText('POINT(1 2)')");
        record.putValue("c_virtual", "ignored");

        DBDataImportConfig config = new DBDataImportConfig();
        assertFalse(record.getRecordData().columns().contains("c_virtual"));
        assertTrue(record.getProperty("c_virtual").isReadonly());
        String sql = ShellMysqlDataImportHelper.toInsertSql(columns, List.of(record), config).get(0);
        assertTrue(sql, sql.contains("b'1101000101011'"));
        assertTrue(sql, !sql.contains("'true'"));
        assertTrue(sql, sql.contains("ST_GeomFromText('POINT(1 2)')"));
        assertTrue(sql, sql.contains("\\\\"));
        assertFalse(sql, sql.contains("c_virtual"));
    }

    private static MysqlColumn column(String name, String type, String tableName) {
        MysqlColumn column = new MysqlColumn(name);
        column.setType(type);
        column.setTableName(tableName);
        return column;
    }
}
