package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.store.jdbc.ColumnDefinition;
import cn.oyzh.store.jdbc.JdbcConst;
import cn.oyzh.store.jdbc.JdbcDialect;
import cn.oyzh.store.jdbc.JdbcManager;
import cn.oyzh.store.jdbc.TableDefinition;
import cn.oyzh.store.jdbc.sqlite.SqliteUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 使用真实dev1数据验证ShellConnect表结构演进与ShellConnectStore能力。
 *
 * 所有操作都在dev1数据库的临时副本上进行，原始数据库只读。
 *
 * @author oyzh
 * @since 2026-10-23
 */
public class ShellConnectSchemaEvolutionTest {

    private static final String TABLE = "t_connect";

    private static final int EXPECTED_ROWS = 157;

    private static final Path DEV1_DATABASE = Path.of(
            System.getProperty("user.home"), ".easyshell_dev1", "db");

    private Path workDirectory;

    private Path database;

    private String dev1Sha256;

    @Before
    public void setUp() throws Exception {
        Assume.assumeTrue("dev1 database not found: " + DEV1_DATABASE,
                Files.isRegularFile(DEV1_DATABASE));
        this.dev1Sha256 = this.sha256(DEV1_DATABASE);
        this.workDirectory = Files.createTempDirectory("easyshell-shell-connect-schema-");
        this.database = this.workDirectory.resolve("db");
        Files.copy(DEV1_DATABASE, this.database);
        this.configureDatabase();
        assertEquals("SQLite copy is not healthy", "ok", this.integrityCheck());
        assertEquals(EXPECTED_ROWS, this.rowCount());
    }

    @After
    public void tearDown() throws Exception {
        JdbcManager.destroy();
        if (this.dev1Sha256 != null) {
            assertEquals("dev1 database was modified", this.dev1Sha256, this.sha256(DEV1_DATABASE));
        }
        if (this.workDirectory != null) {
            try (Stream<Path> paths = Files.walk(this.workDirectory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(this::deleteQuietly);
            }
        }
    }

    @Test
    public void addsMissingColumnAndPreservesExistingRows() throws Exception {
        List<String> columns = this.columnNames();
        List<String> baseline = this.snapshot(this.without(columns, "REMARK"));
        this.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                + " DROP COLUMN " + SqliteUtil.wrap("REMARK"));
        assertFalse(this.columnExists("REMARK"));

        ShellConnectStore store = new ShellConnectStore();

        assertEquals(EXPECTED_ROWS, store.load().size());
        assertEquals("TEXT", this.columnType("REMARK"));
        this.assertShellConnectSchema();
        assertEquals(baseline, this.snapshot(this.without(columns, "REMARK")));
        assertEquals(EXPECTED_ROWS, this.rowCount());
    }

    @Test
    public void dropsObsoleteColumnAndPreservesExistingRows() throws Exception {
        List<String> columns = this.columnNames();
        List<String> baseline = this.snapshot(columns);
        this.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                + " ADD COLUMN " + SqliteUtil.wrap("LEGACY_EXTRA") + " TEXT");
        this.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                + " ADD COLUMN " + SqliteUtil.wrap("LEGACY_SECOND") + " INTEGER");
        this.execute("UPDATE " + SqliteUtil.wrap(TABLE)
                + " SET " + SqliteUtil.wrap("LEGACY_EXTRA") + "='obsolete-value'");
        assertTrue(this.columnExists("LEGACY_EXTRA"));
        assertTrue(this.columnExists("LEGACY_SECOND"));

        ShellConnectStore store = new ShellConnectStore();

        assertEquals(EXPECTED_ROWS, store.load().size());
        assertFalse(this.columnExists("LEGACY_EXTRA"));
        assertFalse(this.columnExists("LEGACY_SECOND"));
        this.assertShellConnectSchema();
        assertEquals(baseline, this.snapshot(columns));
        assertEquals(EXPECTED_ROWS, this.rowCount());
    }

    @Test
    public void restoresChangedNumericColumnTypeAndValues() throws Exception {
        List<String> columns = this.columnNames();
        List<String> baseline = this.snapshot(this.without(columns, "CONNECTTIMEOUT"));
        this.rebuildWithColumnType("CONNECTTIMEOUT", "TEXT");
        assertEquals("TEXT", this.columnType("CONNECTTIMEOUT"));
        Map<String, Integer> expected = this.readIntegerValues("CONNECTTIMEOUT");
        assertFalse(expected.isEmpty());

        ShellConnectStore store = new ShellConnectStore();

        assertEquals(EXPECTED_ROWS, store.load().size());
        assertEquals("INTEGER", this.columnType("CONNECTTIMEOUT"));
        this.assertShellConnectSchema();
        assertEquals(baseline, this.snapshot(this.without(columns, "CONNECTTIMEOUT")));
        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            ShellConnect connect = store.selectOne(entry.getKey());
            assertNotNull(connect);
            assertEquals(entry.getValue(), connect.getConnectTimeOut());
        }

        ShellConnectStore restarted = new ShellConnectStore();
        assertEquals(EXPECTED_ROWS, restarted.load().size());
        this.assertShellConnectSchema();
    }

    @Test
    public void restoresChangedTextColumnTypeAndValues() throws Exception {
        List<String> columns = this.columnNames();
        List<String> baseline = this.snapshot(this.without(columns, "REMARK"));
        this.rebuildWithColumnType("REMARK", "INTEGER");
        assertEquals("INTEGER", this.columnType("REMARK"));

        List<String> ids = this.firstIds(3);
        Map<String, String> expected = new LinkedHashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            String value = "schema-remark-" + index + "-中文";
            this.updateText(ids.get(index), "REMARK", value);
            expected.put(ids.get(index), value);
        }

        ShellConnectStore store = new ShellConnectStore();

        assertEquals(EXPECTED_ROWS, store.load().size());
        assertEquals("TEXT", this.columnType("REMARK"));
        this.assertShellConnectSchema();
        assertEquals(baseline, this.snapshot(this.without(columns, "REMARK")));
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            ShellConnect connect = store.selectOne(entry.getKey());
            assertNotNull(connect);
            assertEquals(entry.getValue(), connect.getRemark());
        }
    }

    @Test
    public void repairsCombinedSchemaDriftInOneStartup() throws Exception {
        List<String> columns = this.columnNames();
        List<String> unchanged = this.without(this.without(columns, "REMARK"), "CONNECTTIMEOUT");
        List<String> baseline = this.snapshot(unchanged);

        this.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                + " DROP COLUMN " + SqliteUtil.wrap("REMARK"));
        this.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                + " ADD COLUMN " + SqliteUtil.wrap("LEGACY_EXTRA") + " TEXT");
        this.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                + " ADD COLUMN " + SqliteUtil.wrap("LEGACY_SECOND") + " INTEGER");
        this.rebuildWithColumnType("CONNECTTIMEOUT", "TEXT");
        Map<String, Integer> expected = this.readIntegerValues("CONNECTTIMEOUT");
        assertFalse(expected.isEmpty());

        ShellConnectStore store = new ShellConnectStore();

        assertEquals(EXPECTED_ROWS, store.load().size());
        assertEquals("TEXT", this.columnType("REMARK"));
        assertEquals("INTEGER", this.columnType("CONNECTTIMEOUT"));
        assertFalse(this.columnExists("LEGACY_EXTRA"));
        assertFalse(this.columnExists("LEGACY_SECOND"));
        this.assertShellConnectSchema();
        assertEquals(baseline, this.snapshot(unchanged));
        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            ShellConnect connect = store.selectOne(entry.getKey());
            assertNotNull(connect);
            assertEquals(entry.getValue(), connect.getConnectTimeOut());
        }

        ShellConnectStore restarted = new ShellConnectStore();
        assertEquals(EXPECTED_ROWS, restarted.load().size());
        this.assertShellConnectSchema();
    }

    @Test
    public void shellConnectStoreCrudWorksOnRealDev1Copy() throws Exception {
        ShellConnectStore store = new ShellConnectStore();
        List<ShellConnect> connects = store.load();
        assertEquals(EXPECTED_ROWS, connects.size());

        ShellConnect existing = connects.get(0);
        String existingId = existing.getId();
        String oldName = existing.getName();
        existing.setName(oldName + "-schema-test");
        assertTrue(store.update(existing));
        ShellConnect updated = store.selectOne(existingId);
        assertNotNull(updated);
        assertEquals(existing.getName(), updated.getName());

        ShellConnect added = new ShellConnect();
        added.setId("schema-evolution-" + UUID.randomUUID());
        added.setName("schema-evolution");
        added.setHost("127.0.0.1");
        added.setType("local");
        assertTrue(store.insert(added));
        assertTrue(store.exist(added.getId()));
        assertEquals(EXPECTED_ROWS + 1, store.load().size());

        assertTrue(store.delete(added));
        assertFalse(store.exist(added.getId()));
        assertEquals(EXPECTED_ROWS, store.load().size());
        this.assertShellConnectSchema();
    }

    private void configureDatabase() {
        JdbcManager.destroy();
        JdbcConst.dbFile(this.database.toString());
        JdbcConst.dbDialect(JdbcDialect.SQLITE);
        JdbcManager.dialect = JdbcDialect.SQLITE;
    }

    private Connection openConnection() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + this.database);
    }

    private void execute(String sql) throws Exception {
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private String integrityCheck() throws Exception {
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA integrity_check")) {
            return resultSet.next() ? resultSet.getString(1) : "";
        }
    }

    private long rowCount() throws Exception {
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM " + SqliteUtil.wrap(TABLE))) {
            return resultSet.next() ? resultSet.getLong(1) : -1L;
        }
    }

    private List<ColumnInfo> columnInfo() throws Exception {
        List<ColumnInfo> columns = new ArrayList<>();
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(" + SqliteUtil.wrap(TABLE) + ")")) {
            while (resultSet.next()) {
                columns.add(new ColumnInfo(
                        resultSet.getInt("cid"),
                        resultSet.getString("name"),
                        resultSet.getString("type"),
                        resultSet.getInt("notnull") != 0,
                        resultSet.getInt("pk")));
            }
        }
        return columns;
    }

    private List<String> columnNames() throws Exception {
        List<String> names = new ArrayList<>();
        for (ColumnInfo column : this.columnInfo()) {
            names.add(column.name);
        }
        return names;
    }

    private boolean columnExists(String columnName) throws Exception {
        for (ColumnInfo column : this.columnInfo()) {
            if (column.name.equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }

    private String columnType(String columnName) throws Exception {
        for (ColumnInfo column : this.columnInfo()) {
            if (column.name.equalsIgnoreCase(columnName)) {
                return column.type.toUpperCase(Locale.ROOT);
            }
        }
        return "";
    }

    private long countTablesLike(String pattern) throws Exception {
        try (Connection connection = this.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND lower(name) LIKE ?")) {
            statement.setString(1, pattern.toLowerCase(Locale.ROOT));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : -1L;
            }
        }
    }

    private void assertShellConnectSchema() throws Exception {
        List<ColumnInfo> actual = this.columnInfo();
        TableDefinition model = TableDefinition.ofClass(ShellConnect.class);
        assertEquals("column count", model.getColumns().size(), actual.size());
        for (ColumnDefinition expected : model.getColumns()) {
            ColumnInfo found = null;
            for (ColumnInfo column : actual) {
                if (column.name.equalsIgnoreCase(expected.getColumnName())) {
                    found = column;
                    break;
                }
            }
            assertNotNull("missing column: " + expected.getColumnName(), found);
            assertTrue("type mismatch: " + expected.getColumnName() + ", expected:"
                            + expected.getColumnType() + ", actual:" + found.type,
                    SqliteUtil.checkSqlType(expected.getColumnType(), found.type));
        }
        assertEquals("ID", this.primaryKeyColumn());
        assertEquals(0, this.countTablesLike("t_connect_backup_%"));
        assertEquals(0, this.countTablesLike("t_connect_schema_legacy"));
        assertEquals("ok", this.integrityCheck());
        assertEquals(EXPECTED_ROWS, this.rowCount());
    }

    private String primaryKeyColumn() throws Exception {
        List<String> primaryKeys = new ArrayList<>();
        for (ColumnInfo column : this.columnInfo()) {
            if (column.pk > 0) {
                primaryKeys.add(column.name.toUpperCase(Locale.ROOT));
            }
        }
        assertEquals(1, primaryKeys.size());
        return primaryKeys.get(0);
    }

    private List<String> firstIds(int limit) throws Exception {
        List<String> ids = new ArrayList<>();
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT " + SqliteUtil.wrap("ID") + " FROM " + SqliteUtil.wrap(TABLE)
                             + " ORDER BY " + SqliteUtil.wrap("ID") + " LIMIT " + limit)) {
            while (resultSet.next()) {
                ids.add(resultSet.getString(1));
            }
        }
        return ids;
    }

    private Map<String, Integer> readIntegerValues(String column) throws Exception {
        Map<String, Integer> values = new LinkedHashMap<>();
        String sql = "SELECT " + SqliteUtil.wrap("ID") + "," + SqliteUtil.wrap(column)
                + " FROM " + SqliteUtil.wrap(TABLE)
                + " ORDER BY " + SqliteUtil.wrap("ID");
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Object value = resultSet.getObject(2);
                if (value != null) {
                    values.put(resultSet.getString(1),
                            value instanceof Number number ? number.intValue()
                                    : Integer.valueOf(value.toString()));
                }
            }
        }
        return values;
    }

    private List<String> snapshot(List<String> columns) throws Exception {
        List<String> rows = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ");
        for (String column : columns) {
            sql.append(SqliteUtil.wrap(column)).append(",");
        }
        sql.deleteCharAt(sql.length() - 1)
                .append(" FROM ").append(SqliteUtil.wrap(TABLE))
                .append(" ORDER BY ").append(SqliteUtil.wrap("ID"));
        try (Connection connection = this.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql.toString())) {
            int columnCount = columns.size();
            while (resultSet.next()) {
                StringBuilder row = new StringBuilder();
                for (int index = 1; index <= columnCount; index++) {
                    Object value = resultSet.getObject(index);
                    if (value == null) {
                        row.append("<NULL>");
                    } else {
                        row.append(value.getClass().getSimpleName()).append(':').append(value);
                    }
                    row.append('\u001f');
                }
                rows.add(row.toString());
            }
        }
        return rows;
    }

    private void rebuildWithColumnType(String columnName, String type) throws Exception {
        List<ColumnInfo> columns = this.columnInfo();
        String backup = "t_connect_schema_legacy";
        StringBuilder create = new StringBuilder("CREATE TABLE ")
                .append(SqliteUtil.wrap(TABLE)).append(" (");
        List<String> primaryKeys = new ArrayList<>();
        for (ColumnInfo column : columns) {
            create.append(SqliteUtil.wrap(column.name)).append(' ')
                    .append(column.name.equalsIgnoreCase(columnName) ? type : column.type);
            if (column.notNull || column.pk > 0) {
                create.append(" NOT NULL");
            }
            create.append(',');
            if (column.pk > 0) {
                primaryKeys.add(column.name);
            }
        }
        if (!primaryKeys.isEmpty()) {
            create.append("PRIMARY KEY (");
            for (String primaryKey : primaryKeys) {
                create.append(SqliteUtil.wrap(primaryKey)).append(',');
            }
            create.deleteCharAt(create.length() - 1).append("),");
        }
        create.deleteCharAt(create.length() - 1).append(')');

        StringBuilder columnList = new StringBuilder();
        for (ColumnInfo column : columns) {
            columnList.append(SqliteUtil.wrap(column.name)).append(',');
        }
        columnList.deleteCharAt(columnList.length() - 1);

        try (Connection connection = this.openConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS " + SqliteUtil.wrap(backup));
                statement.execute("ALTER TABLE " + SqliteUtil.wrap(TABLE)
                        + " RENAME TO " + SqliteUtil.wrap(backup));
                statement.execute(create.toString());
                statement.execute("INSERT INTO " + SqliteUtil.wrap(TABLE)
                        + " (" + columnList + ") SELECT " + columnList
                        + " FROM " + SqliteUtil.wrap(backup));
                statement.execute("DROP TABLE " + SqliteUtil.wrap(backup));
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void updateText(String id, String column, String value) throws Exception {
        String sql = "UPDATE " + SqliteUtil.wrap(TABLE) + " SET " + SqliteUtil.wrap(column)
                + "=? WHERE " + SqliteUtil.wrap("ID") + "=?";
        try (Connection connection = this.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setString(2, id);
            statement.executeUpdate();
        }
    }

    private List<String> without(List<String> columns, String excluded) {
        List<String> result = new ArrayList<>();
        for (String column : columns) {
            if (!column.equalsIgnoreCase(excluded)) {
                result.add(column);
            }
        }
        return result;
    }

    private String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        StringBuilder result = new StringBuilder();
        for (byte data : digest.digest(Files.readAllBytes(file))) {
            result.append(String.format("%02x", data));
        }
        return result.toString();
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private record ColumnInfo(int cid, String name, String type, boolean notNull, int pk) {
    }
}
