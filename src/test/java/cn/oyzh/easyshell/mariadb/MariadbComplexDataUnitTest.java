package cn.oyzh.easyshell.mariadb;

import cn.oyzh.easyshell.data.mariadb.ShellMariadbDataImportHelper;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbColumnUtil;
import cn.oyzh.easyshell.util.db.ShellDBSetValueUtil;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * MariaDB 复杂字段参数化回归测试。
 */
public class MariadbComplexDataUnitTest {

    @Before
    public void setUp() {
        ShellMariadbColumnUtil.init();
    }

    @Test
    public void stringPutValueKeepsColumnMetadata() {
        MariadbColumn point = column("c_point", "POINT", "complex_types");
        MariadbRecord record = new MariadbRecord(new MariadbColumns(List.of(point)));
        record.putValue("c_point", "POINT(1 2)");

        assertTrue(record.getRecordData().isTypeGeometry("c_point"));
    }

    @Test
    public void parsesGeneratedColumnStorage() {
        MariadbColumn virtual = column("c_virtual", "VARCHAR(100)", "complex_types");
        virtual.parseExtra("VIRTUAL GENERATED");
        MariadbColumn stored = column("c_stored", "VARCHAR(100)", "complex_types");
        stored.parseExtra("STORED GENERATED");
        MariadbColumn defaultGenerated = column("c_created", "TIMESTAMP", "complex_types");
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
        MariadbColumn id = column("id", "BIGINT", "complex_types");
        MariadbColumn bit = column("c_bit", "BIT(13)", "complex_types");
        MariadbColumn bool = column("c_bool", "TINYINT(1)", "complex_types");
        MariadbColumn json = column("c_json", "JSON", "complex_types");
        MariadbColumn point = column("c_point", "POINT", "complex_types");
        MariadbColumn generated = column("c_virtual", "VARCHAR(100)", "complex_types");
        generated.parseExtra("VIRTUAL GENERATED");

        MariadbColumns columns = new MariadbColumns(List.of(id, bit, bool, json, point, generated));
        MariadbRecord record = new MariadbRecord(columns);
        record.putValue("id", "1");
        record.putValue("c_bit", "b'0001101000101011'");
        record.putValue("c_bool", "true");
        record.putValue("c_json", "{\"name\":\"张三\",\"quote\":\"a\\\"b\"}");
        record.putValue("c_point", "ST_GeomFromText('POINT(1 2)')");
        record.putValue("c_virtual", "ignored");

        DBDataImportConfig config = new DBDataImportConfig();
        assertFalse(record.getRecordData().columns().contains("c_virtual"));
        assertTrue(record.getProperty("c_virtual").isReadonly());
        String sql = ShellMariadbDataImportHelper.toInsertSql(columns, List.of(record), config).get(0);
        assertTrue(sql, sql.contains("b'1101000101011'"));
        assertTrue(sql, !sql.contains("'true'"));
        assertTrue(sql, sql.contains("ST_GeomFromText('POINT(1 2)')"));
        assertTrue(sql, sql.contains("\\\\"));
        assertFalse(sql, sql.contains("c_virtual"));
    }

    @Test
    public void distinguishesEnumAndSetSelectionValues() throws Exception {
        MariadbColumn enumColumn = column("c_enum", "ENUM('red','green','blue')", "complex_types");
        MariadbColumn setColumn = column("c_set", "SET('red','green','blue')", "complex_types");

        assertTrue(enumColumn.isEnumType());
        assertFalse(enumColumn.isSetType());
        assertFalse(setColumn.isEnumType());
        assertTrue(setColumn.isSetType());
        assertEquals(List.of("red", "blue"), ShellDBSetValueUtil.parseValues("red,blue"));
        assertEquals("red,green,blue",
                ShellDBSetValueUtil.joinValues(List.of("red", "green", "blue")));

        String nodeUtil = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/util/mariadb/ShellMariadbNodeUtil.java"));
        assertTrue(nodeUtil.contains("column.isSetType()"));
        assertTrue(nodeUtil.contains("new ShellDBSetTextFiled(column.getValueList())"));
    }

    @Test
    public void setPopupUsesCheckBoxes() throws Exception {
        String fxml = Files.readString(Path.of(
                "src/main/resources/popups/db/shellDBSetPopup.fxml"));
        String controller = Files.readString(Path.of(
                "src/main/java/cn/oyzh/easyshell/popups/db/ShellDBSetPopupController.java"));
        assertTrue(fxml.contains("fx:id=\"listView\""));
        assertTrue(controller.contains("FXListView<FXCheckBox>"));
        assertTrue(controller.contains("checkBox.isSelected()"));
    }

    private static MariadbColumn column(String name, String type, String tableName) {
        MariadbColumn column = new MariadbColumn(name);
        column.setType(type);
        column.setTableName(tableName);
        return column;
    }
}
