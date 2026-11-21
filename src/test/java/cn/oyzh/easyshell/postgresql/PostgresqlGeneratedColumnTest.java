package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordProperty;
import cn.oyzh.fx.db.DBRecordData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostgresqlGeneratedColumnTest {

    @Test
    public void generatedColumnIsReadonlyAndExcludedFromRecordData() {
        PostgresqlColumn text = column("c_text", 1);
        PostgresqlColumn upper = column("c_upper", 2);
        upper.setGenerated(true);
        PostgresqlColumns columns = new PostgresqlColumns(List.of(text, upper));
        PostgresqlRecord record = new PostgresqlRecord(columns);
        record.putValue(text, "hello");
        PostgresqlRecordProperty property = record.putValue(upper, "HELLO");

        assertFalse(text.isGenerated());
        assertTrue(upper.isGenerated());
        assertTrue(property.isReadonly());
        assertFalse(upper.supportDefaultValue());

        DBRecordData recordData = record.getRecordData();
        assertTrue(recordData.columns().contains("c_text"));
        assertFalse(recordData.columns().contains("c_upper"));
    }

    @Test
    public void copyPreservesGeneratedColumnFlag() {
        PostgresqlColumn source = column("c_upper", 1);
        source.setGenerated(true);
        PostgresqlColumn target = new PostgresqlColumn();

        target.copy(source);

        assertTrue(target.isGenerated());
    }

    @Test
    public void serializesJsonMapAsJsonText() {
        java.util.Map<String, Object> json = new java.util.LinkedHashMap<>();
        json.put("id", 10);
        json.put("name", "row10");
        json.put("tags", List.of("a", "b"));

        String text = cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil.toJsonText(json);

        assertTrue(text, text.contains("\"id\":10"));
        assertTrue(text, text.contains("\"name\":\"row10\""));
    }

    @Test
    public void convertsByteArrayToBitTextUsingColumnLength() {
        PostgresqlColumn bit = column("c_bit", 1);
        bit.setType("BIT");
        bit.setSize(1);
        PostgresqlColumn varbit = column("c_varbit", 2);
        varbit.setType("BIT VARYING");
        varbit.setSize(4);

        String bitText = cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil.toBitText(
                bit, new byte[]{1});
        String varbitText = cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil.toBitText(
                varbit, new byte[]{10});

        assertTrue(bitText, bitText.equals("1"));
        assertTrue(varbitText, varbitText.equals("1010"));
    }

    @Test
    public void clipsBitOneTextToDeclaredLength() {
        PostgresqlColumn bit = column("c_bit", 1);
        bit.setType("BIT");
        bit.setSize(1);

        String text = cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil.toBitText(
                bit, "00000001");

        assertTrue(text, text.equals("1"));
    }

    @Test
    public void keepsFixedBitLengthInParameterCast() {
        PostgresqlColumn bit = column("c_bit", 1);
        bit.setType("BIT");
        bit.setSize(8);

        assertEquals("?::BIT(8)", ShellPostgresqlClient.valuePlaceholder(bit));
    }

    private static PostgresqlColumn column(String name, int position) {
        PostgresqlColumn column = new PostgresqlColumn();
        column.setName(name);
        column.setType("TEXT");
        column.setPosition(position);
        column.setSchema("demo");
        column.setTableName("dt_demo_text");
        return column;
    }
}
