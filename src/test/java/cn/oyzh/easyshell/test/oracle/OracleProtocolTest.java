package cn.oyzh.easyshell.test.oracle;

import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataRunSqlFileHandler;
import cn.oyzh.easyshell.oracle.ShellOracleConnManager;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.generator.sequence.OracleSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.view.OracleViewCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.view.OracleCreateViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBObjects;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Oracle协议连接与DDL兼容性测试。
 */
public class OracleProtocolTest {

    @Test
    public void buildsServiceNameSidDescriptorAndSslUrls() {
        DBConnConfig config = new DBConnConfig();
        config.setHost("db.example.com");
        config.setPort(1521);
        config.setDatabase("FREEPDB1");

        assertEquals("jdbc:oracle:thin:@tcp://db.example.com:1521/FREEPDB1",
                ShellOracleConnManager.buildConnectionString(config));

        config.setDatabase("SID:ORCL");
        assertEquals("jdbc:oracle:thin:@db.example.com:1521:ORCL",
                ShellOracleConnManager.buildConnectionString(config));

        config.setDatabase("(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=db)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=ORCL)))");
        assertEquals("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=db)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=ORCL)))",
                ShellOracleConnManager.buildConnectionString(config));

        config.setDatabase("FREEPDB1");
        config.setUseSSL(true);
        assertEquals("jdbc:oracle:thin:@tcps://db.example.com:1521/FREEPDB1",
                ShellOracleConnManager.buildConnectionString(config));
    }

    @Test
    public void renamesOracleRoutineDefinitionsWithoutChangingBody() {
        String procedureDefinition = """
                CREATE OR REPLACE PROCEDURE      ADD_USER(
                    p_username IN VARCHAR2
                ) AS
                BEGIN
                    ADD_USER(p_username);
                END;
                /
                """;
        String expectedDefinition = """
                CREATE OR REPLACE PROCEDURE      "ADD_USER_RENAMED"(
                    p_username IN VARCHAR2
                ) AS
                BEGIN
                    ADD_USER(p_username);
                END;
                """;
        assertEquals(expectedDefinition.stripTrailing(), ShellOracleHelper.renameRoutineDefinition(
                procedureDefinition, "PROCEDURE", "ADD_USER", "ADD_USER_RENAMED"));

        String qualifiedDefinition = "CREATE OR REPLACE PROCEDURE \"DEMO\".\"ADD_USER\" AS BEGIN NULL; END;";
        assertEquals("CREATE OR REPLACE PROCEDURE \"DEMO\".\"ADD_USER_RENAMED\" AS BEGIN NULL; END;",
                ShellOracleHelper.renameRoutineDefinition(
                        qualifiedDefinition, "PROCEDURE", "ADD_USER", "ADD_USER_RENAMED"));

        String functionDefinition = "CREATE OR REPLACE NONEDITIONABLE FUNCTION DEMO . USER_COUNT RETURN NUMBER AS BEGIN RETURN 0; END; /";
        assertEquals("CREATE OR REPLACE NONEDITIONABLE FUNCTION DEMO . \"USER_COUNT_RENAMED\" RETURN NUMBER AS BEGIN RETURN 0; END;",
                ShellOracleHelper.renameRoutineDefinition(
                        functionDefinition, "FUNCTION", "USER_COUNT", "USER_COUNT_RENAMED"));

        try {
            ShellOracleHelper.renameRoutineDefinition(
                    "CREATE OR REPLACE PROCEDURE ADD_USER AS BEGIN NULL; END;",
                    "PROCEDURE", "MISSING_PROCEDURE", "NEW_PROCEDURE");
            fail("Expected missing routine name to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("MISSING_PROCEDURE"));
        }
    }

    @Test
    public void generatesSequenceTriggerCompatibleAutoIncrement() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);

        OracleColumn name = column("NAME", "VARCHAR2", "APP", "USERS");
        name.setSize(255);
        name.setNullable(false);

        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id, name)));
        param.setTriggers(new DBObjects<>());

        List<String> sql = OracleTableCreateSqlGenerator.generateSql(param);
        String ddl = String.join("\n", sql);
        assertTrue(ddl.contains("CREATE TABLE \"APP\".\"USERS\""));
        assertTrue(ddl.contains("PRIMARY KEY (\"ID\")"));
        assertTrue(sql.contains("CREATE SEQUENCE \"APP\".\"USERS_ID_SEQ\" START WITH 1 INCREMENT BY 1 NOCACHE;"));
        assertTrue(ddl.contains("CREATE OR REPLACE TRIGGER \"APP\".\"USERS_ID_TRG\" BEFORE INSERT ON \"APP\".\"USERS\""));
        assertTrue(ddl.contains(":NEW.\"ID\""));
    }

    @Test
    public void generatesOracleIdentityWhenExplicitlySelected() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");

        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id)));
        param.setTriggers(new DBObjects<>());

        String ddl = String.join("\n", OracleTableCreateSqlGenerator.generateSql(param));
        assertTrue("Unexpected Oracle identity DDL: " + ddl,
                ddl.contains("\"ID\" NUMBER GENERATED BY DEFAULT ON NULL AS IDENTITY"));
        assertFalse(ddl.contains("CREATE SEQUENCE"));
        assertFalse(ddl.contains("CREATE OR REPLACE TRIGGER"));
    }

    @Test
    public void renamesLegacyAutoIncrementObjects() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        id.setName("USER_ID");

        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id)));
        param.setTriggers(new DBObjects<>());

        String ddl = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(ddl.contains("RENAME COLUMN \"ID\" TO \"USER_ID\""));
        assertTrue(ddl.contains("DROP TRIGGER \"APP\".\"USERS_ID_TRG\""));
        assertTrue(ddl.contains("DROP SEQUENCE \"APP\".\"USERS_ID_SEQ\""));
        assertTrue(ddl.contains("CREATE SEQUENCE \"APP\".\"USERS_USER_ID_SEQ\""));
        assertTrue(ddl.contains("CREATE OR REPLACE TRIGGER \"APP\".\"USERS_USER_ID_TRG\""));
    }

    @Test
    public void preservesCustomSequenceAndTriggerNames() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("SEQUENCE_TRIGGER");
        id.setAutoIncrementSequenceName("CUSTOM_ID_SEQ");
        id.setAutoIncrementTriggerName("CUSTOM_ID_TRG");
        id.setName("USER_ID");

        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id)));
        param.setTriggers(new DBObjects<>());

        String ddl = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(ddl.contains("RENAME COLUMN \"ID\" TO \"USER_ID\""));
        assertFalse(ddl.contains("DROP TRIGGER"));
        assertFalse(ddl.contains("DROP SEQUENCE"));
        assertFalse(ddl.contains("CREATE SEQUENCE"));
        assertTrue(ddl.contains("CREATE OR REPLACE TRIGGER \"APP\".\"CUSTOM_ID_TRG\""));
        assertTrue(ddl.contains("SELECT \"APP\".\"CUSTOM_ID_SEQ\".NEXTVAL INTO :NEW.\"USER_ID\""));
    }

    @Test
    public void generatesCompatibleSequenceAndViewDdl() {
        OracleSequence sequence = new OracleSequence("USER_ID_SEQ");
        sequence.setSchema("APP");
        sequence.setIncrementBy(BigInteger.ONE);
        sequence.setMinVal(BigInteger.ONE);
        sequence.setCurrentVal(BigInteger.ONE);

        OracleCreateSequenceParam sequenceParam = new OracleCreateSequenceParam();
        sequenceParam.setSequence(sequence);
        String sequenceDdl = OracleSequenceCreateSqlGenerator.generateSqlSingle(sequenceParam);
        assertTrue(sequenceDdl.startsWith("CREATE SEQUENCE \"APP\".\"USER_ID_SEQ\""));
        assertTrue(sequenceDdl.contains(" START WITH 1 "));
        assertTrue(sequenceDdl.endsWith("NOCYCLE"));

        OracleView view = new OracleView();
        view.setSchema("APP");
        view.setName("ACTIVE_USERS");
        view.setDefinition("SELECT * FROM \"APP\".\"USERS\" WHERE \"ACTIVE\" = 1\nWITH READ ONLY");
        view.setSecurityType("CURRENT_USER");
        view.setUpdatable(false);

        OracleCreateViewParam viewParam = new OracleCreateViewParam();
        viewParam.setView(view);
        String viewDdl = OracleViewCreateSqlGenerator.generateSqlSingle(viewParam);
        assertTrue(viewDdl.contains("CREATE VIEW \"APP\".\"ACTIVE_USERS\"\nBEQUEATH CURRENT_USER AS"));
        assertTrue(viewDdl.endsWith("WITH READ ONLY;"));
        assertEquals(viewDdl.indexOf("WITH READ ONLY"), viewDdl.lastIndexOf("WITH READ ONLY"));
        assertFalse(OracleViewCreateSqlGenerator.generateSql(viewParam, false).get(0)
                .contains("BEQUEATH CURRENT_USER"));
    }

    @Test
    public void normalizesFullAndBareTriggerBodies() {
        String fullDdl = "CREATE OR REPLACE TRIGGER \"APP\".\"T\" BEFORE INSERT ON \"APP\".\"USERS\" FOR EACH ROW BEGIN :NEW.\"ID\" := 1; END;";
        assertEquals("BEGIN :NEW.\"ID\" := 1; END;", ShellOracleHelper.normalizeTriggerBody(fullDdl));
        assertEquals("BEGIN NULL; END;", ShellOracleHelper.normalizeTriggerBody("NULL"));
        assertEquals("BEGIN SELECT 1 INTO :NEW.\"ID\" FROM DUAL; END;",
                ShellOracleHelper.normalizeTriggerBody("BEGIN SELECT 1 INTO :NEW.\"ID\" FROM DUAL; END;"));
    }

    @Test
    public void splitsOracleSqlPlusAndPlSqlScripts() {
        String script = """
                CREATE OR REPLACE TYPE APP.T_VALUE AS OBJECT (
                    ID NUMBER,
                    LABEL VARCHAR2(100)
                );
                /
                CREATE OR REPLACE PROCEDURE APP.P_TEST AS
                BEGIN
                    INSERT INTO APP.LOGS (MESSAGE) VALUES (q'[a;b]');
                END;
                /
                SELECT q'{c;d}' FROM DUAL;
                """;

        List<String> statements = ShellOracleDataRunSqlFileHandler.splitOracleSql(script);
        assertEquals(3, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE OR REPLACE TYPE"));
        assertFalse(statements.get(0).contains("\n/"));
        assertTrue(statements.get(1).contains("q'[a;b]'"));
        assertTrue(statements.get(1).endsWith("END;"));
        assertFalse(statements.get(1).contains("\n/"));
        assertEquals("SELECT q'{c;d}' FROM DUAL", statements.get(2));
    }

    private static OracleColumn column(String name, String type, String schema, String tableName) {
        OracleColumn column = new OracleColumn();
        column.setName(name);
        column.setType(type);
        column.setSchema(schema);
        column.setTableName(tableName);
        return column;
    }
}
