package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlCsvTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlJsonTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlSqlTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlTxtTypeFileWriter;
import cn.oyzh.easyshell.data.postgresql.file.ShellPostgresqlXmlTypeFileWriter;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlColumnUtil;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.dto.DBDataExportConfig;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.DBDataCsvTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataJsonTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataTxtTypeFileReader;
import cn.oyzh.fx.db.data.file.DBDataXmlTypeFileReader;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PostgresqlDataFormatRoundTripTest {

    static {
        DBColumnFieldManager.registerInitializer(DBDialect.POSTGRESQL, ShellPostgresqlColumnUtil::init);
    }

    @Test
    public void roundTripsStructuredAndDelimitedFormats() throws Exception {
        PostgresqlColumns columns = new PostgresqlColumns(List.of(
                column("c_json", "JSON"),
                column("c_jsonpath", "JSONPATH"),
                column("c_xml", "XML"),
                column("c_bytea", "BYTEA")
        ));
        Map<String, Object> values = values();

        assertRoundTrip(columns, values, "json", columns, (file, config) -> {
            try (ShellPostgresqlJsonTypeFileWriter writer = new ShellPostgresqlJsonTypeFileWriter(file, config, columns)) {
                writer.writeHeader();
                writer.writeRecord(values);
                writer.writeTrial();
            }
            try (DBDataJsonTypeFileReader reader = new DBDataJsonTypeFileReader(new File(file), new DBDataImportConfig())) {
                return reader.readRecords(1).get(0);
            }
        });
        assertRoundTrip(columns, values, "csv", columns, (file, config) -> {
            try (ShellPostgresqlCsvTypeFileWriter writer = new ShellPostgresqlCsvTypeFileWriter(file, config, columns)) {
                writer.writeHeader();
                writer.writeRecord(values);
            }
            try (DBDataCsvTypeFileReader reader = new DBDataCsvTypeFileReader(new File(file), new DBDataImportConfig())) {
                return reader.readRecords(1).get(0);
            }
        });
        assertRoundTrip(columns, values, "txt", columns, (file, config) -> {
            try (ShellPostgresqlTxtTypeFileWriter writer = new ShellPostgresqlTxtTypeFileWriter(file, config, columns)) {
                writer.writeHeader();
                writer.writeRecord(values);
            }
            try (DBDataTxtTypeFileReader reader = new DBDataTxtTypeFileReader(new File(file), new DBDataImportConfig())) {
                return reader.readRecords(1).get(0);
            }
        });
        assertRoundTrip(columns, values, "xml", columns, (file, config) -> {
            try (ShellPostgresqlXmlTypeFileWriter writer = new ShellPostgresqlXmlTypeFileWriter(file, config, columns)) {
                writer.writeHeader();
                writer.writeRecord(values);
                writer.writeTrial();
            }
            try (DBDataXmlTypeFileReader reader = new DBDataXmlTypeFileReader(new File(file), new DBDataImportConfig())) {
                return reader.readRecords(1).get(0);
            }
        });
    }

    @Test
    public void sqlExportExcludesGeneratedColumns() throws Exception {
        PostgresqlColumn text = column("c_text", "TEXT");
        PostgresqlColumn upper = column("c_upper", "TEXT");
        upper.setGenerated(true);
        PostgresqlColumns columns = new PostgresqlColumns(List.of(text, upper));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_text", "demo");
        values.put("c_upper", "DEMO");

        File file = File.createTempFile("postgresql-sql-export-", ".sql");
        try {
            DBDataExportConfig config = new DBDataExportConfig();
            try (ShellPostgresqlSqlTypeFileWriter writer = new ShellPostgresqlSqlTypeFileWriter(
                    file.getAbsolutePath(), config, columns)) {
                writer.writeRecord(values);
            }
            String sql = Files.readString(file.toPath());
            assertTrue(sql, sql.contains("\"c_text\""));
            assertFalse(sql, sql.contains("\"c_upper\""));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void roundTripsXmlAttributes() throws Exception {
        PostgresqlColumns columns = new PostgresqlColumns(List.of(
                column("c_jsonpath", "JSONPATH"),
                column("c_xml", "XML")
        ));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_jsonpath", "$.\"row1\".\"name\"");
        values.put("c_xml", "<row attr=\"x\">1 & 2</row>");
        File file = File.createTempFile("postgresql-xml-attr-", ".xml");
        try {
            DBDataExportConfig exportConfig = new DBDataExportConfig();
            exportConfig.setFieldToAttr(true);
            try (ShellPostgresqlXmlTypeFileWriter writer = new ShellPostgresqlXmlTypeFileWriter(
                    file.getAbsolutePath(), exportConfig, columns)) {
                writer.writeHeader();
                writer.writeRecord(values);
                writer.writeTrial();
            }
            DBDataImportConfig importConfig = new DBDataImportConfig();
            importConfig.setAttrToColumn(true);
            try (DBDataXmlTypeFileReader reader = new DBDataXmlTypeFileReader(file, importConfig)) {
                Map<String, Object> imported = reader.readRecords(1).get(0);
                assertEquals(values.get("c_jsonpath"), imported.get("c_jsonpath"));
                assertEquals(values.get("c_xml"), imported.get("c_xml"));
            }
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private void assertRoundTrip(PostgresqlColumns columns, Map<String, Object> values, String extension,
                                 PostgresqlColumns exportColumns, FormatRoundTrip roundTrip) throws Exception {
        File file = File.createTempFile("postgresql-format-", "." + extension);
        try {
            Map<String, Object> imported = roundTrip.run(file.getAbsolutePath(), new DBDataExportConfig());
            assertEquals(values.get("c_jsonpath"), imported.get("c_jsonpath"));
            assertEquals(values.get("c_xml"), imported.get("c_xml"));
            assertEquals(values.get("c_bytea"), imported.get("c_bytea"));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("c_json", "{\"id\":1}");
        values.put("c_jsonpath", "$.\"row1\".\"name\"");
        values.put("c_xml", "<row attr=\"x\">1 & 2</row>");
        values.put("c_bytea", "\\xD3D94468");
        return values;
    }

    private PostgresqlColumn column(String name, String type) {
        PostgresqlColumn column = new PostgresqlColumn(name);
        column.setType(type);
        column.setSchema("demo");
        column.setTableName("dt_demo_misc");
        return column;
    }

    @FunctionalInterface
    private interface FormatRoundTrip {
        Map<String, Object> run(String file, DBDataExportConfig config) throws Exception;
    }
}
