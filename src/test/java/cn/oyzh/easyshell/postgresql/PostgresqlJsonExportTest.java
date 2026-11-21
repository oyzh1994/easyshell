package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlJsonTypeFileWriter;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlColumnUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataJsonTypeFileReader;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PostgresqlJsonExportTest {

    static {
        DBColumnFieldManager.registerInitializer(DBDialect.POSTGRESQL, ShellPostgresqlColumnUtil::init);
    }

    @Test
    public void escapesJsonpathAndByteaStrings() throws Exception {
        PostgresqlColumn jsonpath = column("c_jsonpath", "JSONPATH");
        PostgresqlColumn bytea = column("c_bytea", "BYTEA");
        PostgresqlColumns columns = new PostgresqlColumns(List.of(jsonpath, bytea));
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("c_jsonpath", "$.\"row10\".\"name\"");
        record.put("c_bytea", "\\xD3D94468");

        File file = File.createTempFile("postgresql-json-export-", ".json");
        try {
            DBDataExportConfig config = new DBDataExportConfig();
            try (ShellPostgresqlJsonTypeFileWriter writer = new ShellPostgresqlJsonTypeFileWriter(
                    file.getAbsolutePath(), config, columns)) {
                writer.writeHeader();
                writer.writeRecord(record);
                writer.writeTrial();
            }
            JSONArray rows = JSON.parseArray(Files.readString(file.toPath()));
            assertEquals("$.\"row10\".\"name\"", rows.getJSONObject(0).getString("c_jsonpath"));
            assertEquals("\\xD3D94468", rows.getJSONObject(0).getString("c_bytea"));

            try (DBDataJsonTypeFileReader reader = new DBDataJsonTypeFileReader(file, new DBDataImportConfig())) {
                Map<String, Object> imported = reader.readRecords(1).get(0);
                assertEquals("$.\"row10\".\"name\"", imported.get("c_jsonpath"));
                assertEquals("\\xD3D94468", imported.get("c_bytea"));
            }
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private static PostgresqlColumn column(String name, String type) {
        PostgresqlColumn column = new PostgresqlColumn(name);
        column.setType(type);
        column.setSchema("demo");
        column.setTableName("dt_demo_misc");
        return column;
    }
}
