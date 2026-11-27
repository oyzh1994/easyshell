package cn.oyzh.easyshell.db.type;

import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.tabs.db.type.ShellTypeDesignTabController;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeView;
import cn.oyzh.easyshell.trees.type.ShellTypeTreeItem;
import cn.oyzh.easyshell.trees.type.ShellTypesTreeItem;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTypeDefinition;
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
 * 公共自定义类型实现测试。
 * <p>
 * 支持自定义类型的数据库共用同一套类型树节点与设计页面，这里校验展示、懒加载数量、页面装载与语句解析。
 *
 * @since 2026-11-28
 */
public class ShellTypeSharedTest {

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
     * 测试用类型
     */
    private static class TestType implements DBTypeDefinition {

        private String name;

        private String objectType;

        private String createDefinition;

        TestType(String name, String objectType) {
            this.name = name;
            this.objectType = objectType;
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
        public String getObjectType() {
            return objectType;
        }

        @Override
        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        @Override
        public String getCreateDefinition() {
            return createDefinition;
        }

        @Override
        public void setCreateDefinition(String createDefinition) {
            this.createDefinition = createDefinition;
        }
    }

    /**
     * 测试用类型目标
     */
    private static class TestTarget implements ShellTypeTarget {

        @Override
        public DBDialect dialect() {
            return DBDialect.ORACLE;
        }

        @Override
        public String connectName() {
            return "local";
        }

        @Override
        public String typeScope() {
            return "DEMO";
        }

        @Override
        public List<? extends DBTypeDefinition> selectTypes() {
            return List.of(new TestType("T_ADDRESS", "OBJECT"));
        }

        @Override
        public String selectTypeDefinition(String typeName) {
            return "CREATE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100))";
        }

        @Override
        public void executeTypeSql(List<String> sqlList) {
        }

        @Override
        public boolean existType(String typeName) {
            return false;
        }

        @Override
        public void dropType(String typeName, String objectType) {
        }

        @Override
        public DBTypeDefinition newType() {
            return new TestType(null, null);
        }

        @Override
        public void reloadTypes() {
        }
    }

    @Test
    public void typeNodeShowsNameAndObjectType() throws Throwable {
        runOnFx(() -> {
            RichTreeView view = new ShellOracleTreeView();
            ShellTypeTarget target = new TestTarget();
            ShellTypeTreeItem item = new ShellTypeTreeItem(new TestType("T_ADDRESS", "OBJECT"), view, target);
            // 类型不隶属于表，直接展示名称
            assertEquals("T_ADDRESS", item.getValue().name());
            assertEquals(" (OBJECT)", item.getValue().extra());
            assertEquals("T_ADDRESS", item.typeName());
            assertEquals("OBJECT", item.objectType());
            // 无对象类型时不展示附加信息
            ShellTypeTreeItem item1 = new ShellTypeTreeItem(new TestType("T_ADDRESS", null), view, target);
            assertNull(item1.getValue().extra());
        });
    }

    @Test
    public void typeListNodeKeepsLazyCount() throws Throwable {
        runOnFx(() -> {
            RichTreeView view = new ShellOracleTreeView();
            ShellTypesTreeItem item = new ShellTypesTreeItem(view, new TestTarget());
            // 未加载时不展示数量，也不访问数据库
            assertNull(item.getTypeSize());
            assertNull(item.getValue().extra());
            assertEquals("DEMO", item.scope());
        });
    }

    @Test
    public void designTabLoadsWithDefinitionEditor() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLResult result = FXMLLoaderExt.loadFromUrl("/tabs/db/type/shellTypeDesignTab.fxml");
                assertNotNull(result.getNode());
                ShellTypeDesignTabController controller =
                        (ShellTypeDesignTabController) result.getController();
                assertNotNull(controller);
                // 只提供一个定义编辑器
                Field field = ShellTypeDesignTabController.class.getDeclaredField("definition");
                field.setAccessible(true);
                assertTrue(field.get(controller) instanceof ShellDataEditor);
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("type design FXML load timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("type design FXML load failed", error.get());
        }
    }

    @Test
    public void parseTypeNameAcrossDialects() {
        assertEquals("T_ADDRESS", ShellTypeHelper.parseTypeName(
                "CREATE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100));"));
        assertEquals("MOOD", ShellTypeHelper.parseTypeName(
                "CREATE TYPE \"DEMO\".\"MOOD\" AS ENUM ('sad', 'ok', 'happy');"));
        assertEquals("T_POSITIVE", ShellTypeHelper.parseTypeName(
                "CREATE DOMAIN T_POSITIVE AS INTEGER CHECK (VALUE > 0);"));
        assertEquals("T_ADDRESS", ShellTypeHelper.parseTypeName(
                "CREATE OR REPLACE EDITIONABLE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100));"));
        assertEquals("T_ADDR_BODY", ShellTypeHelper.parseTypeName(
                "CREATE TYPE BODY T_ADDR_BODY AS MEMBER FUNCTION f RETURN NUMBER IS BEGIN RETURN 1; END;"));
        assertEquals("MyType", ShellTypeHelper.parseTypeName(
                "CREATE TYPE [dbo].[MyType] AS TABLE (ID INT);"));
        assertEquals("T_NUMBERS", ShellTypeHelper.parseTypeName(
                "ALTER TYPE T_NUMBERS ADD ATTRIBUTE (N NUMBER);"));
        assertEquals("T_DM_CLASS", ShellTypeHelper.parseTypeName(
                "CREATE OR REPLACE CLASS T_DM_CLASS AS OBJECT (N NUMBER);"));
        assertNull(ShellTypeHelper.parseTypeName("DROP TYPE T_ADDRESS"));
        assertNull(ShellTypeHelper.parseTypeName(null));
    }

    @Test
    public void detectTypeStatements() {
        assertTrue(ShellTypeHelper.isCreateTypeSql("CREATE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100))"));
        assertTrue(ShellTypeHelper.isCreateTypeSql("  create or replace type T_ADDRESS as object (CITY VARCHAR2(100))"));
        assertTrue(ShellTypeHelper.isCreateTypeSql("CREATE DOMAIN T_POSITIVE AS INTEGER"));
        assertTrue(ShellTypeHelper.isCreateTypeSql("CREATE TYPE BODY T_ADDRESS AS BEGIN END;"));
        assertTrue(ShellTypeHelper.isCreateTypeSql("ALTER TYPE T_ADDRESS ADD ATTRIBUTE (N NUMBER)"));
        assertTrue(ShellTypeHelper.isCreateTypeSql("CREATE CLASS T_DM AS OBJECT (N NUMBER)"));
        assertTrue(!ShellTypeHelper.isCreateTypeSql("DROP TYPE T_ADDRESS"));
        assertTrue(!ShellTypeHelper.isCreateTypeSql(null));
        assertTrue(ShellTypeHelper.isDropTypeSql("DROP TYPE T_ADDRESS"));
        assertTrue(ShellTypeHelper.isDropTypeSql("DROP DOMAIN IF EXISTS T_POSITIVE"));
        assertTrue(ShellTypeHelper.isDropTypeSql("DROP TYPE BODY T_ADDRESS"));
        assertTrue(ShellTypeHelper.isDropTypeSql("DROP CLASS T_DM"));
        assertTrue(!ShellTypeHelper.isDropTypeSql("CREATE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100))"));
        assertTrue(!ShellTypeHelper.isDropTypeSql(null));
    }

    @Test
    public void replaceTypeStatements() {
        // OR REPLACE / ALTER 为原地修改，保存时不应先删除原类型
        assertTrue(ShellTypeHelper.isReplaceTypeSql("CREATE OR REPLACE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100))"));
        assertTrue(ShellTypeHelper.isReplaceTypeSql("ALTER TYPE T_ADDRESS ADD ATTRIBUTE (N NUMBER)"));
        assertTrue(ShellTypeHelper.isReplaceTypeSql("CREATE OR REPLACE EDITIONABLE TYPE T_ADDRESS AS OBJECT (N NUMBER)"));
        assertTrue(ShellTypeHelper.isReplaceTypeSql("CREATE OR REPLACE CLASS T_DM AS OBJECT (N NUMBER)"));
        assertTrue(!ShellTypeHelper.isReplaceTypeSql("CREATE TYPE T_ADDRESS AS OBJECT (CITY VARCHAR2(100))"));
        assertTrue(!ShellTypeHelper.isReplaceTypeSql("DROP TYPE T_ADDRESS"));
        assertTrue(!ShellTypeHelper.isReplaceTypeSql(null));
    }

    @Test
    public void stripTerminator() {
        assertEquals("CREATE TYPE T_A AS OBJECT (N NUMBER)",
                ShellTypeHelper.stripTerminator("CREATE TYPE T_A AS OBJECT (N NUMBER);"));
        assertEquals("CREATE TYPE T_A AS OBJECT (N NUMBER)",
                ShellTypeHelper.stripTerminator("CREATE TYPE T_A AS OBJECT (N NUMBER);  "));
        assertEquals("SELECT 1", ShellTypeHelper.stripTerminator("SELECT 1"));
        assertNull(ShellTypeHelper.stripTerminator(null));
    }

    @Test
    public void allTypeModelsImplementDBTypeDefinition() {
        List<DBTypeDefinition> types = List.of(
                new cn.oyzh.easyshell.oracle.type.OracleTypeDefinition(),
                new cn.oyzh.easyshell.postgresql.type.PostgresqlTypeDefinition(),
                new cn.oyzh.easyshell.sqlserver.type.SqlServerTypeDefinition(),
                new cn.oyzh.easyshell.dameng.type.DamengTypeDefinition()
        );
        for (DBTypeDefinition type : types) {
            type.setName("T_ADDRESS");
            type.setObjectType("OBJECT");
            type.setCreateDefinition("CREATE TYPE T_ADDRESS AS OBJECT (N NUMBER)");
            assertEquals(type.getClass().getSimpleName(), "T_ADDRESS", type.getName());
            assertEquals(type.getClass().getSimpleName(), "OBJECT", type.getObjectType());
            assertEquals(type.getClass().getSimpleName(), "CREATE TYPE T_ADDRESS AS OBJECT (N NUMBER)",
                    type.getCreateDefinition());
        }
    }

    @Test
    public void allClientsUseSharedSelectTypeParam() throws NoSuchMethodException {
        // 支持自定义类型的数据库统一使用公共查询参数，且提供一致的列表/定义/存在/删除/执行方法
        List<Class<?>> clients = List.of(
                cn.oyzh.easyshell.oracle.ShellOracleClient.class,
                cn.oyzh.easyshell.postgresql.ShellPostgresqlClient.class,
                cn.oyzh.easyshell.sqlserver.ShellSqlServerClient.class,
                cn.oyzh.easyshell.dameng.ShellDamengClient.class
        );
        for (Class<?> client : clients) {
            assertNotNull(client.getSimpleName(),
                    client.getMethod("selectSchemaTypes", ShellSelectTypeParam.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("selectTypeDefinition", String.class, String.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("existType", String.class, String.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("dropType", String.class, String.class, String.class));
            assertNotNull(client.getSimpleName(),
                    client.getMethod("executeTypeSql", String.class, List.class));
            for (java.lang.reflect.Method method : client.getMethods()) {
                if (method.getName().equals("selectSchemaTypes")) {
                    assertEquals(client.getSimpleName(), 1, method.getParameterCount());
                    assertEquals(client.getSimpleName(), ShellSelectTypeParam.class, method.getParameterTypes()[0]);
                }
            }
        }
    }

    @Test
    public void allSchemaNodesSupportTypeTarget() {
        List<Class<?>> nodes = List.of(
                cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem.class,
                cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem.class
        );
        for (Class<?> node : nodes) {
            assertTrue(node.getSimpleName(), ShellTypeTarget.class.isAssignableFrom(node));
        }
    }

    @Test
    public void allTabPanesHandleTypeEvents() throws NoSuchMethodException {
        List<Class<?>> panes = List.of(
                cn.oyzh.easyshell.tabs.oracle.ShellOracleTabPane.class,
                cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlTabPane.class,
                cn.oyzh.easyshell.tabs.sqlserver.ShellSqlServerTabPane.class,
                cn.oyzh.easyshell.tabs.dameng.ShellDamengTabPane.class
        );
        for (Class<?> pane : panes) {
            assertNotNull(pane.getSimpleName(),
                    pane.getDeclaredMethod("onTypeDesign",
                            cn.oyzh.easyshell.event.db.type.ShellTypeDesignEvent.class));
            assertNotNull(pane.getSimpleName(),
                    pane.getDeclaredMethod("onTypeDropped",
                            cn.oyzh.easyshell.event.db.type.ShellTypeDroppedEvent.class));
        }
    }

    @Test
    public void selectTypeParamBean() {
        ShellSelectTypeParam param = new ShellSelectTypeParam("SCOPE", "T_ADDRESS");
        assertEquals("SCOPE", param.getScope());
        assertEquals("T_ADDRESS", param.getTypeName());
        ShellSelectTypeParam all = new ShellSelectTypeParam("SCOPE");
        assertEquals("SCOPE", all.getScope());
        assertNull(all.getTypeName());
        ShellSelectTypeParam bean = new ShellSelectTypeParam();
        bean.setScope("PUBLIC");
        bean.setTypeName("MOOD");
        assertEquals("PUBLIC", bean.getScope());
        assertEquals("MOOD", bean.getTypeName());
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
