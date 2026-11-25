package cn.oyzh.easyshell.oracle;

import cn.oyzh.easyshell.data.oracle.file.ShellOracleSqlTypeFileWriter;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import org.junit.Test;

import javax.sql.rowset.serial.SerialClob;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OracleSqlLargeValueExportTest {

    @Test
    public void writesClobAsPlsqlVariable() throws Exception {
        Path file = Files.createTempFile("oracle-large-clob-", ".sql");
        OracleColumn column = new OracleColumn("C_CLOB");
        column.setTableName("T_LARGE");
        column.setPosition(1);
        column.setType("CLOB");
        OracleColumns columns = new OracleColumns(List.of(column));
        DBDataExportConfig config = new DBDataExportConfig();
        Map<String, Object> record = new HashMap<>();
        record.put("C_CLOB", new SerialClob("x".repeat(5000).toCharArray()));
        try (ShellOracleSqlTypeFileWriter writer = new ShellOracleSqlTypeFileWriter(
                file.toString(), config, columns)) {
            writer.setTable("T_LARGE");
            writer.writeRecord(record);
        }
        String sql = Files.readString(file);
        assertTrue(sql.contains("DECLARE"));
        assertTrue(sql.contains("TO_CLOB("));
        assertFalse(sql.contains("'" + "x".repeat(4001)));
    }

    @Test
    public void buildsDumpInsertWithPlsqlVariable() {
        OracleColumn id = new OracleColumn("ID");
        id.setTableName("T_LARGE");
        id.setPosition(1);
        id.setType("NUMBER(19)");
        OracleColumn clob = new OracleColumn("C_CLOB");
        clob.setTableName("T_LARGE");
        clob.setPosition(2);
        clob.setType("CLOB");
        OracleColumns columns = new OracleColumns(List.of(id, clob));
        OracleRecord record = new OracleRecord(columns);
        record.putValue("ID", 1);
        record.putValue("C_CLOB", "C".repeat(4000));

        String sql = ShellOracleDataUtil.toInsertSql(columns, record, true);

        assertTrue(sql.contains("DECLARE"));
        assertTrue(sql.contains("TO_CLOB("));
        assertFalse(sql.contains("'" + "C".repeat(4000) + "'"));
    }
}
