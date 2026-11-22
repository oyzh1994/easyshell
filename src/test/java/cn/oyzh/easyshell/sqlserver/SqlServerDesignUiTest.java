package cn.oyzh.easyshell.sqlserver;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.sqlserver.table.SqlServerColumnKindComboBox;
import cn.oyzh.easyshell.fx.sqlserver.table.ShellSqlServerForeignKeyPolicyComboBox;
import cn.oyzh.easyshell.fx.sqlserver.table.ShellSqlServerIndexMethodComboBox;
import cn.oyzh.easyshell.fx.sqlserver.table.ShellSqlServerTriggerPolicyComboBox;
import cn.oyzh.easyshell.fx.sqlserver.routine.ShellSqlServerParamModeComboBox;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.generator.table.SqlServerTableAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.table.SqlServerTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.function.SqlServerFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.function.SqlServerFunctionAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.procedure.SqlServerProcedureAlertSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.sequence.SqlServerSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.generator.view.SqlServerViewAlertSqlGenerator;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerInsertRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerAlertTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerCreateTableParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.sqlserver.function.SqlServerCreateFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerAlertFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerAlertProcedureParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerCreateSequenceParam;
import cn.oyzh.easyshell.sqlserver.sequence.SqlServerSequence;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerAlertViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.tabs.sqlserver.table.ShellSqlServerTableDesignTab;
import cn.oyzh.easyshell.tabs.sqlserver.table.ShellSqlServerTableDesignTabController;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeView;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerColumnUtil;
import cn.oyzh.easyshell.data.sqlserver.file.ShellSqlServerDataImportHelper;
import cn.oyzh.easyshell.controller.connect.sqlserver.ShellAddSqlServerConnectController;
import cn.oyzh.easyshell.controller.connect.sqlserver.ShellUpdateSqlServerConnectController;
import cn.oyzh.easyshell.tabs.sqlserver.table.ShellSqlServerTableColumnExtraController;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.PortTextField;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import javafx.scene.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SQL Server表设计UI装载测试。
 */
public class SqlServerDesignUiTest {

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue(startup.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void loadsGeneratedColumnDesignControls() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(
                        "/tabs/sqlserver/table/shellSqlServerTableColumnExtra.fxml");
                assertNotNull(result.getNode());
                ShellSqlServerTableColumnExtraController controller =
                        (ShellSqlServerTableColumnExtraController) result.getController();
                assertTrue(getField(controller, "columnKind") instanceof SqlServerColumnKindComboBox);
                SqlServerColumnKindComboBox kind = (SqlServerColumnKindComboBox)
                        getField(controller, "columnKind");
                assertTrue(kind.getItems().contains(SqlServerColumn.KIND_NORMAL));
                assertTrue(kind.getItems().contains(SqlServerColumn.KIND_GENERATED));
                assertTrue(getField(controller, "generatedExpression") instanceof ClearableTextField);
                assertTrue(getField(controller, "generatedStorage") instanceof FXComboBox<?>);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void loadsTableDesignTab() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl(
                        "/tabs/sqlserver/table/shellSqlServerTableDesignTab.fxml");
                assertNotNull(result.getNode());
                assertNotNull(result.getController());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void usesOnlySqlServerIdentityForAutoIncrement() throws Exception {
        ShellSqlServerColumnUtil.init();
        String fxml = Files.readString(Path.of(
                "src/main/resources/tabs/sqlserver/table/shellSqlServerTableColumnExtra.fxml"));
        assertFalse(fxml.contains("AoutoIncrementTypeComboBox"));
        assertFalse(fxml.contains("GENERATED BY DEFAULT"));
        assertFalse(fxml.contains("GENERATED ALWAYS"));
        assertFalse(fxml.contains("BIGSERIAL"));
        assertFalse(fxml.contains("SMALLSERIAL"));
        assertTrue(fxml.contains("IDENTITY"));

        SqlServerTable table = new SqlServerTable();
        table.setSchema("EASYSHELL_SRC");
        table.setName("IDENTITY_TEST");
        SqlServerColumn id = column("id", "int", 1, true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");
        SqlServerCreateTableParam param = new SqlServerCreateTableParam();
        param.setTable(table);
        param.setColumns(new SqlServerColumns(List.of(id)));
        String sql = new SqlServerTableCreateSqlGenerator().generateSingle(param);
        assertTrue(sql.contains("IDENTITY(1,1)"));
        assertFalse(sql.contains("SERIAL"));
        assertFalse(sql.contains("GENERATED BY DEFAULT"));
        assertFalse(sql.contains("GENERATED ALWAYS"));
        assertFalse(sql.contains("OVERRIDING SYSTEM VALUE"));
    }

    @Test
    public void keepsOneTrailingSemicolonOnCreatedAndChangedTriggers() {
        String body = ShellSqlServerHelper.normalizeTriggerBody(
                "BEGIN INSERT INTO [LOG] ([VALUE]) VALUES ('a'); END;;",
                "AFTER INSERT");
        assertEquals("BEGIN INSERT INTO [LOG] ([VALUE]) VALUES ('a'); END;", body);

        SqlServerTable table = new SqlServerTable();
        table.setSchema("dbo");
        table.setName("TRIGGER_TEST");
        SqlServerTrigger trigger = new SqlServerTrigger();
        trigger.setName("TRG_TRIGGER_TEST");
        trigger.setPolicy("AFTER INSERT");
        trigger.setDefinition("BEGIN INSERT INTO [LOG] ([VALUE]) VALUES ('a'); END;;");

        SqlServerCreateTableParam createParam = new SqlServerCreateTableParam();
        createParam.setTable(table);
        createParam.setColumns(new SqlServerColumns(List.of()));
        createParam.setTriggers(DBObjects.of(List.of(trigger)));
        String createSql = new SqlServerTableCreateSqlGenerator().generateSingle(createParam);
        assertTrue(createSql.contains("END;"));
        assertFalse(createSql.contains("END;;"));

        trigger.setChanged(true);
        SqlServerAlertTableParam alertParam = new SqlServerAlertTableParam();
        alertParam.setTable(table);
        alertParam.setTriggers(DBObjects.of(List.of(trigger)));
        String alertSql = new SqlServerTableAlertSqlGenerator().generateSingle(alertParam);
        assertTrue(alertSql.contains("END;"));
        assertFalse(alertSql.contains("END;;"));
    }

    @Test
    public void removesViewHeaderBeforeRegeneratingDefinition() {
        assertEquals("SELECT * FROM t_log",
                ShellSqlServerHelper.normalizeViewDefinition(
                        "CREATE VIEW [demo2].[v1] AS\nSELECT * FROM t_log;"));
        assertEquals("SELECT * FROM t_log",
                ShellSqlServerHelper.normalizeViewDefinition(
                        "CREATE OR ALTER VIEW [demo2].[v1] AS SELECT * FROM t_log"));
        assertEquals("SELECT * FROM t_log",
                ShellSqlServerHelper.normalizeViewDefinition(
                        "ALTER VIEW [demo2].[v1] AS SELECT * FROM t_log"));

        SqlServerView view = new SqlServerView();
        view.setName("v1");
        view.setSchema("demo2");
        view.setDefinition("CREATE VIEW [demo2].[v1] AS SELECT * FROM t_log;");
        SqlServerAlertViewParam param = new SqlServerAlertViewParam();
        param.setSchema("demo2");
        param.setView(view);
        String sql = SqlServerViewAlertSqlGenerator.generateSqlSingle(param);
        assertEquals(1, sql.toUpperCase().split("CREATE OR ALTER VIEW", -1).length - 1);
        assertFalse(sql.substring(sql.toUpperCase().indexOf(" AS ") + 4)
                .stripLeading().toUpperCase().startsWith("CREATE"));
    }

    @Test
    public void functionParametersAreAlwaysInputOnly() {
        SqlServerRoutineParam first = new SqlServerRoutineParam();
        first.setName("first");
        first.setType("INT");
        first.setMode("IN");
        SqlServerRoutineParam second = new SqlServerRoutineParam();
        second.setName("second");
        second.setType("INT");
        second.setMode("OUT");
        SqlServerRoutineParam third = new SqlServerRoutineParam();
        third.setName("third");
        third.setType("INT");
        third.setMode("INOUT");
        second.setFunctionParam(true);
        third.setFunctionParam(true);
        assertEquals("IN", second.getMode());
        assertEquals("IN", third.getMode());

        SqlServerFunction function = new SqlServerFunction();
        function.setName("func1");
        function.setSchema("dbo");
        function.setParams(List.of(first, second, third));
        SqlServerRoutineParam returnParam = new SqlServerRoutineParam();
        returnParam.setType("INT");
        function.setReturnParam(returnParam);
        function.setDefinition("RETURN @first + @second + @third;");
        SqlServerCreateFunctionParam param = new SqlServerCreateFunctionParam();
        param.setSchema("dbo");
        param.setFunction(function);
        String sql = SqlServerFunctionCreateSqlGenerator.generateSqlSingle(param);
        assertFalse(sql.toUpperCase().contains(" OUTPUT"));
        assertTrue(sql.contains("RETURNS INT"));
        assertFalse(sql.toUpperCase().contains("RETURNS RECORD"));

        function.setDefinition("""
                CREATE FUNCTION [dbo].[func1] (@first INT, @second INT, @third INT)
                RETURNS INT
                WITH EXECUTE AS CALLER
                AS
                BEGIN
                    RETURN @first + @second + @third;
                END;
                """);
        SqlServerAlertFunctionParam alertParam = new SqlServerAlertFunctionParam();
        alertParam.setSchema("dbo");
        alertParam.setFunction(function);
        String alertSql = SqlServerFunctionAlertSqlGenerator.generateSqlSingle(alertParam);
        assertEquals(1, alertSql.toUpperCase().split("CREATE OR ALTER FUNCTION", -1).length - 1);
        assertFalse(alertSql.toUpperCase().contains("CREATE   FUNCTION"));
        assertFalse(alertSql.toUpperCase().contains("AS\nBEGIN\nBEGIN"));

        function.setDefinition("""
                BEGIN
                BEGIN
                    -- Routine body goes here...

                    RETURN 0;
                END;
                END;
                """);
        alertSql = SqlServerFunctionAlertSqlGenerator.generateSqlSingle(alertParam);
        assertEquals(1, alertSql.toUpperCase().split("\\bBEGIN\\b", -1).length - 1);
        assertEquals(1, alertSql.toUpperCase().split("\\bEND\\b", -1).length - 1);

        ShellSqlServerParamModeComboBox functionModes = new ShellSqlServerParamModeComboBox(true);
        assertTrue(functionModes.getItems().contains("IN"));
        assertFalse(functionModes.getItems().contains("OUT"));
        assertFalse(functionModes.getItems().contains("INOUT"));
        ShellSqlServerParamModeComboBox procedureModes = new ShellSqlServerParamModeComboBox(false);
        assertTrue(procedureModes.getItems().contains("IN"));
        assertTrue(procedureModes.getItems().contains("OUT"));
        assertFalse(procedureModes.getItems().contains("INOUT"));
    }

    @Test
    public void procedureEditKeepsCreateOrAlterAndPreservesOutputParameters() {
        SqlServerRoutineParam first = new SqlServerRoutineParam();
        first.setName("a");
        first.setType("INT");
        first.setMode("IN");
        SqlServerRoutineParam second = new SqlServerRoutineParam();
        second.setName("b");
        second.setType("INT");
        second.setMode("OUT");
        SqlServerRoutineParam third = new SqlServerRoutineParam();
        third.setName("c");
        third.setType("INT");
        third.setMode("OUT");
        SqlServerProcedure procedure = new SqlServerProcedure();
        procedure.setName("px1");
        procedure.setSchema("demo2");
        procedure.setSecurityType("SELF");
        procedure.setParams(List.of(first, second, third));
        procedure.setDefinition("""
                CREATE PROCEDURE [demo2].[px1]
                 @a INT,@b INT OUTPUT,@c INT OUTPUT
                WITH EXECUTE AS SELF
                AS
                BEGIN
                BEGIN
                    RETURN;
                END;
                END;
                """);
        SqlServerAlertProcedureParam param = new SqlServerAlertProcedureParam();
        param.setSchema("demo2");
        param.setProcedure(procedure);
        String sql = SqlServerProcedureAlertSqlGenerator.generateSqlSingle(param);
        assertEquals(1, sql.toUpperCase().split("CREATE OR ALTER PROCEDURE", -1).length - 1);
        assertFalse(sql.toUpperCase().contains("CREATE   PROCEDURE"));
        assertTrue(sql.toUpperCase().contains("WITH EXECUTE AS SELF"));
        assertEquals(2, sql.toUpperCase().split(" OUTPUT", -1).length - 1);
        assertEquals(1, sql.toUpperCase().split("\\bBEGIN\\b", -1).length - 1);
        assertEquals(1, sql.toUpperCase().split("\\bEND\\b", -1).length - 1);
    }

    @Test
    public void newSequenceUsesPositiveDefaults() {
        String fxml;
        try {
            fxml = Files.readString(Path.of(
                    "src/main/resources/tabs/sqlserver/sequence/shellSqlServerSequenceDesignTab.fxml"));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
        assertFalse(fxml.contains("fx:id=\"order\""));
        assertFalse(fxml.contains("%base.order"));
        SqlServerSequence sequence = new SqlServerSequence();
        sequence.setName("SEQ_DEFAULT");
        sequence.setSchema("dbo");
        SqlServerCreateSequenceParam param = new SqlServerCreateSequenceParam();
        param.setSchema("dbo");
        param.setSequence(sequence);
        String sql = SqlServerSequenceCreateSqlGenerator.generateSqlSingle(param);
        assertTrue(sql.contains("INCREMENT BY 1"));
        assertTrue(sql.contains("MINVALUE 1"));
        assertTrue(sql.contains("START WITH 1"));
        assertFalse(sql.contains("NO MINVALUE"));
    }

    @Test
    public void exposesOnlySqlServer2022TypesAndParsesSequenceDefaults() throws Exception {
        ShellSqlServerColumnUtil.init();
        List<String> types = DBColumnFieldManager.fieldNames(DBDialect.SQLSERVER);
        assertEquals(List.of(
                "INT", "BIGINT", "NVARCHAR", "VARCHAR", "DECIMAL", "BIT",
                "DATETIME2", "DATE", "DATETIME", "UNIQUEIDENTIFIER"
        ), types.subList(0, 10));
        assertTrue(types.contains("INT"));
        assertTrue(types.contains("NVARCHAR"));
        assertTrue(types.contains("VARBINARY"));
        assertTrue(types.contains("DATETIMEOFFSET"));
        assertFalse(types.contains("JSONB"));
        assertFalse(types.contains("BYTEA"));
        assertFalse(types.contains("SERIAL"));
        assertFalse(types.contains("BOOLEAN"));
        assertFalse(types.contains("INET"));
        assertFalse(types.contains("POINT"));

        SqlServerColumn sequenceColumn = column("ID", "int", 1, false);
        sequenceColumn.setDefaultValue("(NEXT VALUE FOR [dbo].[SEQ_ORDER])");
        assertTrue(sequenceColumn.isDefaultValueSequence());
        assertEquals("[dbo].[SEQ_ORDER]", sequenceColumn.getSequenceName());
        assertEquals("(NEXT VALUE FOR [dbo].[SEQ_ORDER])", sequenceColumn.getDefaultValueFix());

        SqlServerColumn textColumn = column("TEXT_VALUE", "nvarchar(100)", 2, false);
        DBDataImportConfig config = new DBDataImportConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals("", ShellSqlServerDataImportHelper.parameterized(textColumn, "", config));

        String viewFxml = Files.readString(Path.of(
                "src/main/resources/tabs/sqlserver/view/shellSqlServerViewDesignTab.fxml"));
        assertFalse(viewFxml.contains("SecurityTypeComboBox"));
        assertFalse(viewFxml.contains("AlgorithmComboBox"));

        ShellSqlServerIndexMethodComboBox indexMethod = new ShellSqlServerIndexMethodComboBox();
        assertTrue(indexMethod.getItems().contains("CLUSTERED"));
        assertTrue(indexMethod.getItems().contains("NONCLUSTERED"));
        assertFalse(indexMethod.getItems().contains("BTREE"));
        assertFalse(indexMethod.getItems().contains("HASH"));
        assertFalse(indexMethod.getItems().contains("GIN"));

        ShellSqlServerForeignKeyPolicyComboBox foreignKeyPolicy =
                new ShellSqlServerForeignKeyPolicyComboBox();
        assertFalse(foreignKeyPolicy.getItems().contains("RESTRICT"));
        assertTrue(foreignKeyPolicy.getItems().contains("SET NULL"));

        ShellSqlServerTriggerPolicyComboBox triggerPolicy =
                new ShellSqlServerTriggerPolicyComboBox();
        assertFalse(triggerPolicy.getItems().contains("BEFORE INSERT"));
        assertTrue(triggerPolicy.getItems().contains("INSTEAD OF INSERT"));
    }

    @Test
    public void loadsAllSqlServerFxmlPages() throws Exception {
        List<String> resources;
        try (var paths = Files.walk(Path.of("src/main/resources"))) {
            resources = paths.filter(path -> path.toString().endsWith(".fxml"))
                    .map(Path::toString)
                    .filter(path -> path.contains("/sqlserver/"))
                    .map(path -> "/" + Path.of("src/main/resources").relativize(Path.of(path)))
                    .map(path -> path.replace('\\', '/'))
                    .sorted()
                    .toList();
        }
        assertTrue(!resources.isEmpty());
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                for (String resource : resources) {
                    FXMLResult result = FXMLLoaderExt.loadFromUrl(resource);
                    assertNotNull(resource, result.getNode());
                    assertNotNull(resource, result.getController());
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void usesSqlServerDefaultsInAddAndUpdateConnectionForms() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                assertConnectionDefaults(FXMLLoaderExt.loadFromUrl(
                                "/fxml/connect/sqlserver/shellAddSqlServerConnect.fxml"),
                        ShellAddSqlServerConnectController.class);
                assertConnectionDefaults(FXMLLoaderExt.loadFromUrl(
                                "/fxml/connect/sqlserver/shellUpdateSqlServerConnect.fxml"),
                        ShellUpdateSqlServerConnectController.class);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void keepsEditedRecordValuesMappedToTheirColumns() throws Exception {
        ShellSqlServerColumnUtil.init();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                SqlServerColumns columns = new SqlServerColumns(List.of(
                        column("ID", "bigint(8)", 1, true),
                        column("C_INT", "int", 2, false),
                        column("C_DECIMAL", "decimal(20,6)", 3, false),
                        column("C_BIT", "bit", 4, false),
                        column("C_NVARCHAR", "nvarchar(200)", 5, false),
                        column("C_VARBINARY", "varbinary(MAX)", 6, false),
                        column("C_DATETIME2", "datetime2(7)", 7, false),
                        column("C_DATETIMEOFFSET", "datetimeoffset(7)", 8, false),
                        column("C_UUID", "uniqueidentifier", 9, false),
                        column("C_XML", "xml", 10, false)
                ));
                assertTrue(columns.column("C_VARBINARY").supportBinary());
                assertTrue(!columns.column("C_VARBINARY").supportString());
                assertTrue(columns.column("C_DATETIME2").supportTimestamp());
                assertTrue(columns.column("C_DATETIMEOFFSET").supportTimestamp());
                assertTrue(columns.column("C_XML").supportText());
                assertTrue(columns.column("C_BIT").supportBoolean());
                assertTrue(!columns.column("C_BIT").supportBit());
                assertEquals(Types.VARBINARY,
                        ShellSqlServerClient.nullSqlType(columns.column("C_VARBINARY")));
                assertEquals(Types.TIMESTAMP_WITH_TIMEZONE,
                        ShellSqlServerClient.nullSqlType(columns.column("C_DATETIMEOFFSET")));
                assertEquals(Types.TIMESTAMP,
                        ShellSqlServerClient.nullSqlType(columns.column("C_DATETIME2")));
                SqlServerRecord record = new SqlServerRecord(columns);
                for (SqlServerColumn column : columns) {
                    record.putValue(column, null);
                }
                assertTrue(getNode(record, "C_VARBINARY") instanceof BinaryTextFiled);
                assertTrue(getNode(record, "C_DATETIME2") instanceof DateTimeTextField);
                assertTrue(getNode(record, "C_DATETIMEOFFSET") instanceof DateTimeTextField);
                assertTrue(getNode(record, "C_BIT") instanceof BooleanTextFiled);
                setNodeValue(record, "C_INT", 7);
                setNodeValue(record, "C_DECIMAL", new BigDecimal("12.500000"));
                setNodeValue(record, "C_BIT", true);
                setNodeValue(record, "C_NVARCHAR", "中文 O'Reilly");
                setNodeValue(record, "C_VARBINARY", new byte[]{0, 1, 2, (byte) 255});
                setNodeValue(record, "C_DATETIME2", LocalDateTime.of(2026, 11, 22, 10, 20, 30));
                setNodeValue(record, "C_DATETIMEOFFSET", "2026-11-22T10:20:30+08:00");
                setNodeValue(record, "C_UUID", "12345678-1234-5678-1234-567812345678");
                setNodeValue(record, "C_XML", "<root><value>中文</value></root>");

                DBRecordData data = record.getRecordData();
                assertTrue(data.value("C_VARBINARY") instanceof byte[]);
                assertTrue(data.value("C_DATETIME2") instanceof java.util.Date);
                assertTrue(data.value("C_DATETIMEOFFSET") instanceof CharSequence
                        || data.value("C_DATETIMEOFFSET") instanceof java.util.Date);
                assertEquals(true, data.value("C_BIT"));
                assertEquals("中文 O'Reilly", data.value("C_NVARCHAR"));
                assertEquals(7, ((Number) data.value("C_INT")).intValue());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void displaysSqlServerBitAsBoolean() throws Exception {
        ShellSqlServerColumnUtil.init();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                SqlServerColumn bitColumn = column("C_BIT", "bit", 1, false);
                SqlServerRecord record = new SqlServerRecord(List.of(bitColumn));
                record.putValue(bitColumn, true);
                BooleanTextFiled node = (BooleanTextFiled) getNode(record, "C_BIT");
                assertEquals("true", node.getText());
                assertEquals(Boolean.TRUE, node.getValue());

                record.putValue(bitColumn, false);
                assertEquals("false", node.getText());
                assertEquals(Boolean.FALSE, node.getValue());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    @Test
    public void openingExistingTableDesignDoesNotMarkTabChanged() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<ShellSqlServerClient> clientRef = new AtomicReference<>();
        AtomicReference<TestableTableDesignTab> tabRef = new AtomicReference<>();
        AtomicReference<Thread> loaderRef = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);
        Platform.runLater(() -> {
            ShellSqlServerClient client = null;
            try {
                ShellConnect connect = new ShellConnect();
                connect.setType(ShellPrototype.SQLSERVER);
                connect.setHost("127.0.0.1:14333");
                connect.setDatabase("EASYSHELL_SQLSERVER");
                connect.setUser("sa");
                connect.setPassword("easyshell@2026");
                connect.setSSLMode(false);
                connect.setConnectTimeOut(10);
                client = new ShellSqlServerClient(connect);
                client.start(10000);
                clientRef.set(client);

                TestableTableDesignTab tab = new TestableTableDesignTab();
                tab.loadContentForTest();
                tabRef.set(tab);

                ShellSqlServerTreeView treeView = new ShellSqlServerTreeView();
                treeView.setClient(client);
                ShellSqlServerSchemaTreeItem schemaItem = new ShellSqlServerSchemaTreeItem(
                        client.schema("EASYSHELL_SRC"), treeView);
                treeView.root().addChild(schemaItem);
                SqlServerTable table = client.selectTableSimple("EASYSHELL_SRC", "COMPLEX_TYPES");

                ShellSqlServerTableDesignTabController controller = tab.controllerForTest();
                setField(controller, "dbItem", schemaItem);
                setField(controller, "table", table);
                setField(controller, "newData", false);
                loaderRef.set(new Thread(() -> {
                    try {
                        var method = ShellSqlServerTableDesignTabController.class
                                .getDeclaredMethod("doInit");
                        method.setAccessible(true);
                        method.invoke(controller);
                    } catch (Throwable ex) {
                        error.set(ex);
                    }
                }, "sqlserver-table-design-init-test"));
                loaderRef.get().start();
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                started.countDown();
            }
        });
        assertTrue(started.await(10, TimeUnit.SECONDS));
        Thread loader = loaderRef.get();
        if (loader != null) {
            loader.join(20_000);
            assertFalse(loader.isAlive());
        }
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }

        Thread.sleep(500);
        AtomicReference<Boolean> unsaved = new AtomicReference<>();
        AtomicReference<Integer> columnCount = new AtomicReference<>();
        CountDownLatch checked = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                TestableTableDesignTab tab = tabRef.get();
                ShellSqlServerTableDesignTabController controller = tab.controllerForTest();
                unsaved.set(tab.isUnsaved());
                DBStatusTableView<?> columns = (DBStatusTableView<?>) getField(controller, "columnTable");
                columnCount.set(columns.getItems().size());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                checked.countDown();
            }
        });
        assertTrue(checked.await(10, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
        assertFalse(unsaved.get());
        assertTrue(columnCount.get() > 0);

        ShellSqlServerClient client = clientRef.get();
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void insertsBinaryAndDateTimeValuesFromUiRecord() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            ShellSqlServerClient client = null;
            String table = "COMPLEX_TYPES_UI_SMOKE";
            try {
                ShellSqlServerColumnUtil.init();
                ShellConnect connect = new ShellConnect();
                connect.setType(ShellPrototype.SQLSERVER);
                connect.setHost("127.0.0.1:14333");
                connect.setDatabase("EASYSHELL_SQLSERVER");
                connect.setUser("sa");
                connect.setPassword("easyshell@2026");
                connect.setSSLMode(false);
                connect.setConnectTimeOut(10);
                client = new ShellSqlServerClient(connect);
                client.start(10000);
                client.executeSqlSimple("EASYSHELL_SRC", "DROP TABLE IF EXISTS [EASYSHELL_SRC].["
                        + table + "]");
                client.executeSqlSimple("EASYSHELL_SRC", """
                        CREATE TABLE [EASYSHELL_SRC].[COMPLEX_TYPES_UI_SMOKE] (
                            [ID] BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                            [C_INT] INT NULL,
                            [C_DECIMAL] DECIMAL(20,6) NULL,
                            [C_BIT] BIT NULL,
                            [C_NVARCHAR] NVARCHAR(200) NULL,
                            [C_VARBINARY] VARBINARY(MAX) NULL,
                            [C_DATETIMEOFFSET] DATETIMEOFFSET(7) NULL,
                            [C_DATETIME2] DATETIME2(7) NULL,
                            [C_UUID] UNIQUEIDENTIFIER NULL,
                            [C_XML] XML NULL
                        )
                        """);
                SqlServerColumns columns = new SqlServerColumns(client.selectColumns(
                        new SqlServerSelectColumnParam("EASYSHELL_SRC", table)));
                SqlServerRecord record = new SqlServerRecord(columns);
                for (SqlServerColumn column : columns) {
                    record.putValue(column, null);
                }
                setNodeValue(record, "C_INT", 7);
                setNodeValue(record, "C_DECIMAL", new BigDecimal("12.500000"));
                setNodeValue(record, "C_BIT", true);
                setNodeValue(record, "C_NVARCHAR", "中文 O'Reilly");
                setNodeValue(record, "C_VARBINARY", new byte[]{0, 1, 2, (byte) 255});
                setNodeValue(record, "C_DATETIME2", LocalDateTime.of(2026, 11, 22, 10, 20, 30));
                setNodeValue(record, "C_UUID", "12345678-1234-5678-1234-567812345678");
                setNodeValue(record, "C_XML", "<root><value>中文</value></root>");

                DBRecordData data = record.getRecordData();
                assertEquals(7, ((Number) data.value("C_INT")).intValue());
                assertEquals("中文 O'Reilly", data.value("C_NVARCHAR"));
                assertEquals(true, data.value("C_BIT"));
                assertTrue(data.value("C_VARBINARY") instanceof byte[]);
                assertEquals(null, data.value("C_DATETIMEOFFSET"));
                assertTrue(data.value("C_DATETIME2") instanceof java.util.Date);

                SqlServerRecordPrimaryKey primaryKey = new SqlServerRecordPrimaryKey();
                primaryKey.init(columns.column("ID"), record);
                SqlServerInsertRecordParam param = new SqlServerInsertRecordParam();
                param.setSchema("EASYSHELL_SRC");
                param.setTableName(table);
                param.setRecord(data);
                param.setPrimaryKey(primaryKey);
                assertEquals(1, client.insertRecord(param));

                SqlServerSelectRecordParam selectParam = new SqlServerSelectRecordParam();
                selectParam.setSchema("EASYSHELL_SRC");
                selectParam.setTableName(table);
                SqlServerRecord selected = client.selectRecords(selectParam).getFirst();
                assertTrue(selected.getValue("C_VARBINARY") instanceof byte[]);
                assertEquals(true, selected.getValue("C_BIT"));
                assertEquals(null, selected.getValue("C_DATETIMEOFFSET"));
                assertNotNull(selected.getValue("C_DATETIME2"));
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                if (client != null) {
                    try {
                        client.executeSqlSimple("EASYSHELL_SRC", "DROP TABLE IF EXISTS [EASYSHELL_SRC].["
                                + table + "]");
                    } catch (Throwable ignored) {
                    }
                    client.close();
                }
                done.countDown();
            }
        });
        assertTrue(done.await(30, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    private static SqlServerColumn column(String name, String type, int position, boolean autoIncrement) {
        SqlServerColumn column = new SqlServerColumn(name);
        column.setType(type);
        column.setPosition(position);
        column.setAutoIncrement(autoIncrement);
        column.setPrimaryKey(autoIncrement);
        column.setTableName("COMPLEX_TYPES");
        column.setSchema("EASYSHELL_SRC");
        return column;
    }

    private static void setNodeValue(SqlServerRecord record, String columnName, Object value) {
        SqlServerColumn column = record.getColumns().column(columnName);
        Node node = getNode(record, columnName);
        assertTrue(columnName + " node", node instanceof FXTextField);
        ((FXTextField) node).setValue(value);
        record.putValue(column, value);
        ((cn.oyzh.fx.db.DBRecordProperty) record.getProperty(columnName)).setChanged(true);
    }

    private static Node getNode(SqlServerRecord record, String columnName) {
        return (Node) ((cn.oyzh.easyshell.sqlserver.record.SqlServerRecordProperty)
                record.getProperty(columnName)).getValue();
    }

    private static void assertConnectionDefaults(FXMLResult result, Class<?> controllerType)
            throws Exception {
        assertTrue(controllerType.isInstance(result.getController()));
        Object controller = result.getController();
        assertEquals(1433, ((PortTextField) getField(controller, "hostPort")).getValue().intValue());
        assertEquals("master",
                ((ClearableTextField) getField(controller, "database")).getTextTrim());
        assertEquals("sa", ((ClearableTextField) getField(controller, "userName")).getTextTrim());
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class TestableTableDesignTab extends ShellSqlServerTableDesignTab {

        private void loadContentForTest() {
            this.loadContent();
        }

        private ShellSqlServerTableDesignTabController controllerForTest() {
            return this.controller();
        }
    }
}
