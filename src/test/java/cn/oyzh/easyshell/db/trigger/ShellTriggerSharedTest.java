package cn.oyzh.easyshell.db.trigger;

import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.tabs.db.trigger.ShellTriggerDesignTabController;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeView;
import cn.oyzh.easyshell.trees.trigger.ShellTriggerTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.ext.FXMLResult;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 公共触发器实现测试。
 * <p>
 * 各数据库共用同一套触发器树节点与触发器设计页面，这里校验展示、懒加载数量、页面装载与语句解析。
 *
 * @since 2026-11-28
 */
public class ShellTriggerSharedTest {

    @BeforeClass
    public static void setUp() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
    }

    /**
     * 测试用触发器
     */
    private static class TestTrigger implements DBTrigger {

        private String name;

        private String tableName;

        TestTrigger(String name, String tableName) {
            this.name = name;
            this.tableName = tableName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getTableName() {
            return tableName;
        }

        @Override
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }

    /**
     * 测试用触发器目标
     */
    private static class TestTarget implements ShellTriggerTarget {

        @Override
        public DBDialect dialect() {
            return DBDialect.MYSQL;
        }

        @Override
        public String connectName() {
            return "local";
        }

        @Override
        public String triggerScope() {
            return "demo";
        }

        @Override
        public List<? extends DBTrigger> selectTriggers() {
            return List.of(new TestTrigger("TRG_NAME", "T_USER"));
        }

        @Override
        public String selectTriggerDefinition(String triggerName) {
            return "CREATE TRIGGER TRG_NAME AFTER INSERT ON T_USER FOR EACH ROW BEGIN END";
        }

        @Override
        public void executeTriggerSql(List<String> sqlList) {
        }

        @Override
        public boolean existTrigger(String triggerName) {
            return false;
        }

        @Override
        public void dropTrigger(String triggerName, String tableName) {
        }

        @Override
        public DBTrigger newTrigger() {
            return new TestTrigger(null, null);
        }

        @Override
        public void reloadTriggers() {
        }
    }

    @Test
    public void triggerNodeShowsTableQualifiedName() throws Throwable {
        runOnFx(() -> {
            RichTreeView view = new ShellMysqlTreeView();
            ShellTriggerTarget target = new TestTarget();
            // 有表名时展示“表名.触发器名”
            ShellTriggerTreeItem item = new ShellTriggerTreeItem(new TestTrigger("TRG_NAME", "T_USER"), view, target);
            assertEquals("T_USER.TRG_NAME", item.getValue().name());
            // 无表名时只展示触发器名称
            ShellTriggerTreeItem item1 = new ShellTriggerTreeItem(new TestTrigger("TRG_NAME", null), view, target);
            assertEquals("TRG_NAME", item1.getValue().name());
            // 空表名同样只展示触发器名称
            ShellTriggerTreeItem item2 = new ShellTriggerTreeItem(new TestTrigger("TRG_NAME", ""), view, target);
            assertEquals("TRG_NAME", item2.getValue().name());
        });
    }

    @Test
    public void typeNodeKeepsLazyCount() throws Throwable {
        runOnFx(() -> {
            RichTreeView view = new ShellMysqlTreeView();
            ShellTriggersTreeItem item = new ShellTriggersTreeItem(view, new TestTarget());
            // 未加载时不展示数量，也不访问数据库
            assertNull(item.getTriggerSize());
            assertEquals("demo", item.scope());
        });
    }

    @Test
    public void designTabLoadsWithDefinitionEditor() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/db/trigger/shellTriggerDesignTab.fxml");
                assertNotNull(result.getNode());
                ShellTriggerDesignTabController controller =
                        (ShellTriggerDesignTabController) result.getController();
                assertNotNull(controller);
                // 只提供一个定义编辑器
                Field field = ShellTriggerDesignTabController.class.getDeclaredField("definition");
                field.setAccessible(true);
                assertTrue(field.get(controller) instanceof ShellDataEditor);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("trigger design FXML load timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("trigger design FXML load failed", error.get());
        }
    }

    @Test
    public void parseTriggerNameAcrossDialects() {
        assertEquals("TRG_A", ShellTriggerHelper.parseTriggerName(
                "CREATE TRIGGER TRG_A AFTER INSERT ON T_A FOR EACH ROW BEGIN END;"));
        assertEquals("TRG_B", ShellTriggerHelper.parseTriggerName(
                "CREATE OR REPLACE TRIGGER \"DEMO\".\"TRG_B\" BEFORE UPDATE ON T_B FOR EACH ROW BEGIN END;"));
        assertEquals("TRG_C", ShellTriggerHelper.parseTriggerName(
                "CREATE DEFINER=`root`@`localhost` TRIGGER `TRG_C` AFTER INSERT ON `t` FOR EACH ROW BEGIN END"));
        assertEquals("TRG_D", ShellTriggerHelper.parseTriggerName(
                "CREATE TEMP TRIGGER IF NOT EXISTS TRG_D AFTER DELETE ON T_D BEGIN END;"));
        assertEquals("TRG_E", ShellTriggerHelper.parseTriggerName(
                "CREATE CONSTRAINT TRIGGER TRG_E AFTER INSERT ON T_E DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION f()"));
        assertEquals("TRG_F", ShellTriggerHelper.parseTriggerName(
                "ALTER TRIGGER [dbo].[TRG_F] ON [dbo].[T_F] AFTER INSERT AS BEGIN END"));
        assertEquals("TRG_G", ShellTriggerHelper.parseTriggerName(
                "CREATE OR REPLACE EDITIONABLE TRIGGER TRG_G AFTER INSERT ON T_G BEGIN END;"));
        assertNull(ShellTriggerHelper.parseTriggerName("DROP TRIGGER TRG_A"));
        assertNull(ShellTriggerHelper.parseTriggerName(null));
    }

    @Test
    public void detectTriggerStatements() {
        assertTrue(ShellTriggerHelper.isCreateTriggerSql("CREATE TRIGGER TRG_A AFTER INSERT ON T_A BEGIN END"));
        assertTrue(ShellTriggerHelper.isCreateTriggerSql("  create or replace trigger TRG_A after insert on T_A begin end"));
        assertTrue(ShellTriggerHelper.isCreateTriggerSql("ALTER TRIGGER TRG_A ON T_A AFTER INSERT AS BEGIN END"));
        assertTrue(!ShellTriggerHelper.isCreateTriggerSql("DROP TRIGGER TRG_A"));
        assertTrue(!ShellTriggerHelper.isCreateTriggerSql(null));
        assertTrue(ShellTriggerHelper.isDropTriggerSql("DROP TRIGGER TRG_A"));
        assertTrue(ShellTriggerHelper.isDropTriggerSql("DROP TRIGGER IF EXISTS TRG_A ON T_A"));
        assertTrue(!ShellTriggerHelper.isDropTriggerSql("CREATE TRIGGER TRG_A AFTER INSERT ON T_A BEGIN END"));
        assertTrue(!ShellTriggerHelper.isDropTriggerSql(null));
    }

    @Test
    public void replaceTriggerStatements() {
        // OR REPLACE / ALTER 为原地修改，保存时不应先删除原触发器
        assertTrue(ShellTriggerHelper.isReplaceTriggerSql("CREATE OR REPLACE TRIGGER TRG_A AFTER INSERT ON T_A BEGIN END"));
        assertTrue(ShellTriggerHelper.isReplaceTriggerSql("ALTER TRIGGER TRG_A ON T_A AFTER INSERT AS BEGIN END"));
        assertTrue(!ShellTriggerHelper.isReplaceTriggerSql("CREATE TRIGGER TRG_A AFTER INSERT ON T_A BEGIN END"));
        assertTrue(!ShellTriggerHelper.isReplaceTriggerSql("DROP TRIGGER TRG_A"));
        assertTrue(!ShellTriggerHelper.isReplaceTriggerSql(null));
    }

    @Test
    public void stripTerminator() {
        assertEquals("CREATE TRIGGER TRG_A BEGIN END",
                ShellTriggerHelper.stripTerminator("CREATE TRIGGER TRG_A BEGIN END;"));
        assertEquals("CREATE TRIGGER TRG_A BEGIN END",
                ShellTriggerHelper.stripTerminator("CREATE TRIGGER TRG_A BEGIN END;  "));
        assertEquals("SELECT 1", ShellTriggerHelper.stripTerminator("SELECT 1"));
        assertNull(ShellTriggerHelper.stripTerminator(null));
    }

    @Test
    public void allTriggerModelsImplementShellTrigger() {
        List<DBTrigger> triggers = List.of(
                new cn.oyzh.easyshell.oracle.trigger.OracleTrigger(),
                new cn.oyzh.easyshell.mysql.trigger.MysqlTrigger(),
                new cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger(),
                new cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger(),
                new cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger(),
                new cn.oyzh.easyshell.dameng.trigger.DamengTrigger(),
                new cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger()
        );
        for (DBTrigger trigger : triggers) {
            assertTrue(trigger.getClass().getSimpleName(), trigger instanceof DBTrigger);
            trigger.setName("TRG_NAME");
            trigger.setTableName("T_USER");
            assertEquals(trigger.getClass().getSimpleName(), "TRG_NAME", trigger.getName());
            assertEquals(trigger.getClass().getSimpleName(), "T_USER", trigger.getTableName());
        }
    }

    @Test
    public void allClientsUseSharedSelectTriggerParam() throws NoSuchMethodException {
        // 各数据库统一使用公共触发器查询参数，且提供一致的列表/定义/存在/删除/执行方法
        List<Class<?>> clients = List.of(
                cn.oyzh.easyshell.oracle.ShellOracleClient.class,
                cn.oyzh.easyshell.mysql.ShellMysqlClient.class,
                cn.oyzh.easyshell.mariadb.ShellMariadbClient.class,
                cn.oyzh.easyshell.postgresql.ShellPostgresqlClient.class,
                cn.oyzh.easyshell.sqlserver.ShellSqlServerClient.class,
                cn.oyzh.easyshell.dameng.ShellDamengClient.class,
                cn.oyzh.easyshell.sqlite.ShellSqliteClient.class
        );
        for (Class<?> client : clients) {
            assertNotNull(client.getSimpleName(),
                    client.getMethod("selectSchemaTriggers", ShellSelectTriggerParam.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("selectTriggerDefinition", String.class, String.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("existTrigger", String.class, String.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("dropTrigger", String.class, String.class, String.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("executeTriggerSql", String.class, List.class));
            for (java.lang.reflect.Method method : client.getMethods()) {
                if (method.getName().equals("selectSchemaTriggers")) {
                    assertEquals(client.getSimpleName(), 1, method.getParameterCount());
                    assertEquals(client.getSimpleName(), ShellSelectTriggerParam.class, method.getParameterTypes()[0]);
                }
            }
        }
    }

    @Test
    public void allSchemaNodesSupportTriggerTarget() {
        List<Class<?>> nodes = List.of(
                cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem.class,
                cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem.class,
                cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem.class
        );
        for (Class<?> node : nodes) {
            assertTrue(node.getSimpleName(), ShellTriggerTarget.class.isAssignableFrom(node));
        }
    }

    @Test
    public void allTabPanesHandleTriggerEvents() throws NoSuchMethodException {
        List<Class<?>> panes = List.of(
                cn.oyzh.easyshell.tabs.oracle.ShellOracleTabPane.class,
                cn.oyzh.easyshell.tabs.mysql.ShellMysqlTabPane.class,
                cn.oyzh.easyshell.tabs.mariadb.ShellMariadbTabPane.class,
                cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlTabPane.class,
                cn.oyzh.easyshell.tabs.sqlserver.ShellSqlServerTabPane.class,
                cn.oyzh.easyshell.tabs.dameng.ShellDamengTabPane.class,
                cn.oyzh.easyshell.tabs.sqlite.ShellSqliteTabPane.class
        );
        for (Class<?> pane : panes) {
            assertNotNull(pane.getSimpleName(),
                    pane.getDeclaredMethod("onTriggerDesign",
                            cn.oyzh.easyshell.event.db.trigger.ShellTriggerDesignEvent.class));
            assertNotNull(pane.getSimpleName(),
                    pane.getDeclaredMethod("onTriggerDropped",
                            cn.oyzh.easyshell.event.db.trigger.ShellTriggerDroppedEvent.class));
        }
    }

    @Test
    public void selectTriggerParamBean() {
        ShellSelectTriggerParam param = new ShellSelectTriggerParam("SCOPE", "TRG_NAME");
        assertEquals("SCOPE", param.getScope());
        assertEquals("TRG_NAME", param.getTriggerName());
        ShellSelectTriggerParam all = new ShellSelectTriggerParam("SCOPE");
        assertEquals("SCOPE", all.getScope());
        assertNull(all.getTriggerName());
        ShellSelectTriggerParam bean = new ShellSelectTriggerParam();
        bean.setScope("DB");
        bean.setTriggerName("TRG");
        assertEquals("DB", bean.getScope());
        assertEquals("TRG", bean.getTriggerName());
    }

    /**
     * 在JavaFX线程执行任务并等待完成
     *
     * @param task 任务
     */
    private static void runOnFx(Runnable task) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable ex) {
                errorRef.set(ex);
            } finally {
                latch.countDown();
            }
        });
        assertTrue("JavaFX task timed out", latch.await(20, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            throw errorRef.get();
        }
    }
}
