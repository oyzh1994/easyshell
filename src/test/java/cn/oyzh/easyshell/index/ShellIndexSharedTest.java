package cn.oyzh.easyshell.index;

import cn.oyzh.easyshell.db.index.ShellIndexHelper;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.tabs.db.index.ShellIndexDesignTabController;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeView;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBIndex;
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
 * 公共索引实现测试。
 * <p>
 * 各数据库共用同一套索引树节点与索引设计页面，这里校验展示、懒加载数量、页面装载与语句解析。
 *
 * @since 2026-11-28
 */
public class ShellIndexSharedTest {

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
     * 测试用索引
     */
    private static class TestIndex implements DBIndex {

        private String name;

        private String tableName;

        TestIndex(String name, String tableName) {
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
     * 测试用索引目标
     */
    private static class TestTarget implements ShellIndexTarget {

        @Override
        public DBDialect dialect() {
            return DBDialect.MYSQL;
        }

        @Override
        public String connectName() {
            return "local";
        }

        @Override
        public String indexScope() {
            return "demo";
        }

        @Override
        public List<? extends DBIndex> selectIndexes() {
            return List.of(new TestIndex("IDX_NAME", "T_USER"));
        }

        @Override
        public String selectIndexDefinition(String indexName) {
            return "CREATE INDEX IDX_NAME ON T_USER (C1)";
        }

        @Override
        public void executeIndexSql(List<String> sqlList) {
        }

        @Override
        public boolean existIndex(String indexName) {
            return false;
        }

        @Override
        public void dropIndex(String indexName, String tableName) {
        }

        @Override
        public DBIndex newIndex() {
            return new TestIndex(null, null);
        }

        @Override
        public void reloadIndexes() {
        }
    }

    @Test
    public void indexNodeShowsTableQualifiedName() throws Throwable {
        runOnFx(() -> {
            RichTreeView view = new ShellMysqlTreeView();
            ShellIndexTarget target = new TestTarget();
            // 有表名时展示“表名.索引名”
            ShellIndexTreeItem item = new ShellIndexTreeItem(new TestIndex("IDX_NAME", "T_USER"), view, target);
            assertEquals("T_USER.IDX_NAME", item.getValue().name());
            // 无表名时只展示索引名称
            ShellIndexTreeItem item1 = new ShellIndexTreeItem(new TestIndex("IDX_NAME", null), view, target);
            assertEquals("IDX_NAME", item1.getValue().name());
            // 空表名同样只展示索引名称
            ShellIndexTreeItem item2 = new ShellIndexTreeItem(new TestIndex("IDX_NAME", ""), view, target);
            assertEquals("IDX_NAME", item2.getValue().name());
        });
    }

    @Test
    public void typeNodeKeepsLazyCount() throws Throwable {
        runOnFx(() -> {
            RichTreeView view = new ShellMysqlTreeView();
            ShellIndexesTreeItem item = new ShellIndexesTreeItem(view, new TestTarget());
            // 未加载时不展示数量，也不访问数据库
            assertNull(item.getIndexSize());
            assertEquals("demo", item.scope());
        });
    }

    @Test
    public void designTabLoadsWithDefinitionEditor() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/db/index/shellIndexDesignTab.fxml");
                assertNotNull(result.getNode());
                ShellIndexDesignTabController controller =
                        (ShellIndexDesignTabController) result.getController();
                assertNotNull(controller);
                // 只提供一个定义编辑器
                Field field = ShellIndexDesignTabController.class.getDeclaredField("definition");
                field.setAccessible(true);
                assertTrue(field.get(controller) instanceof ShellDataEditor);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("index design FXML load timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("index design FXML load failed", error.get());
        }
    }

    @Test
    public void parseIndexNameAcrossDialects() {
        assertEquals("IDX_A", ShellIndexHelper.parseIndexName("CREATE INDEX IDX_A ON T_A (C1)"));
        assertEquals("IDX_B", ShellIndexHelper.parseIndexName("CREATE UNIQUE INDEX \"DEMO\".\"IDX_B\" ON \"DEMO\".\"T_B\" (\"C1\")"));
        assertEquals("IDX_C", ShellIndexHelper.parseIndexName("CREATE INDEX IF NOT EXISTS `IDX_C` ON `t` (`c`)"));
        assertEquals("IDX_D", ShellIndexHelper.parseIndexName("CREATE NONCLUSTERED INDEX [IDX_D] ON [t] ([c])"));
        assertEquals("IDX_E", ShellIndexHelper.parseIndexName("CREATE BITMAP INDEX IDX_E ON T_E (C1)"));
        assertEquals("IDX_F", ShellIndexHelper.parseIndexName("CREATE FULLTEXT INDEX IDX_F ON T_F (C1)"));
        assertNull(ShellIndexHelper.parseIndexName("DROP INDEX IDX_A"));
        assertNull(ShellIndexHelper.parseIndexName(null));
    }

    @Test
    public void detectIndexStatements() {
        assertTrue(ShellIndexHelper.isCreateIndexSql("CREATE INDEX IDX_A ON T_A (C1)"));
        assertTrue(ShellIndexHelper.isCreateIndexSql("  create unique index IDX_A on T_A (C1)"));
        assertTrue(ShellIndexHelper.isCreateIndexSql("CREATE CLUSTERED INDEX IDX_A ON T_A (C1)"));
        assertTrue(ShellIndexHelper.isCreateIndexSql("CREATE SPATIAL INDEX IDX_A ON T_A (C1)"));
        assertTrue(!ShellIndexHelper.isCreateIndexSql("DROP INDEX IDX_A"));
        assertTrue(!ShellIndexHelper.isCreateIndexSql(null));
        assertTrue(ShellIndexHelper.isDropIndexSql("DROP INDEX IDX_A"));
        assertTrue(!ShellIndexHelper.isDropIndexSql("CREATE INDEX IDX_A ON T_A (C1)"));
        assertTrue(!ShellIndexHelper.isDropIndexSql(null));
    }

    @Test
    public void stripTerminator() {
        assertEquals("CREATE INDEX IDX_A ON T_A (C1)", ShellIndexHelper.stripTerminator("CREATE INDEX IDX_A ON T_A (C1);"));
        assertEquals("CREATE INDEX IDX_A ON T_A (C1)", ShellIndexHelper.stripTerminator("CREATE INDEX IDX_A ON T_A (C1);  "));
        assertEquals("SELECT 1", ShellIndexHelper.stripTerminator("SELECT 1"));
        assertNull(ShellIndexHelper.stripTerminator(null));
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

    @Test
    public void oracleSchemaSupportsIndexTarget() {
        // Oracle 模式节点复用公共索引实现，不再保留独立的索引树节点与设计页面
        assertTrue(ShellIndexTarget.class.isAssignableFrom(ShellOracleSchemaTreeItem.class));
        assertTrue(!java.lang.reflect.Modifier.isAbstract(ShellOracleSchemaTreeItem.class.getModifiers()));
    }

    @Test
    public void allClientsUseSharedSelectIndexParam() throws NoSuchMethodException {
        // 各数据库统一使用公共索引查询参数，不再有库专属参数或两参数重载
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
                    client.getMethod("selectSchemaIndexes", ShellSelectIndexParam.class));
            for (java.lang.reflect.Method method : client.getMethods()) {
                if (method.getName().equals("selectSchemaIndexes")) {
                    assertEquals(client.getSimpleName(), 1, method.getParameterCount());
                    assertEquals(client.getSimpleName(), ShellSelectIndexParam.class, method.getParameterTypes()[0]);
                }
            }
        }
    }

    @Test
    public void selectIndexParamBean() {
        ShellSelectIndexParam param = new ShellSelectIndexParam("SCOPE", "IDX_NAME");
        assertEquals("SCOPE", param.getScope());
        assertEquals("IDX_NAME", param.getIndexName());
        ShellSelectIndexParam all = new ShellSelectIndexParam("SCOPE");
        assertEquals("SCOPE", all.getScope());
        assertNull(all.getIndexName());
        ShellSelectIndexParam bean = new ShellSelectIndexParam();
        bean.setScope("DB");
        bean.setIndexName("IDX");
        assertEquals("DB", bean.getScope());
        assertEquals("IDX", bean.getIndexName());
    }

    @Test
    public void allIndexModelsImplementShellIndex() {
        List<DBIndex> indexes = List.of(
                new cn.oyzh.easyshell.oracle.index.OracleIndex(),
                new cn.oyzh.easyshell.mysql.index.MysqlIndex(),
                new cn.oyzh.easyshell.mariadb.index.MariadbIndex(),
                new cn.oyzh.easyshell.postgresql.index.PostgresqlIndex(),
                new cn.oyzh.easyshell.sqlserver.index.SqlServerIndex(),
                new cn.oyzh.easyshell.dameng.index.DamengIndex(),
                new cn.oyzh.easyshell.sqlite.index.SQLiteIndex()
        );
        for (DBIndex index : indexes) {
            index.setName("IDX_NAME");
            index.setTableName("T_USER");
            assertEquals(index.getClass().getSimpleName(), "IDX_NAME", index.getName());
            assertEquals(index.getClass().getSimpleName(), "T_USER", index.getTableName());
        }
    }
}
