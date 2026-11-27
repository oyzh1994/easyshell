package cn.oyzh.easyshell.trees;

import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeView;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTablesTreeItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Oracle库树节点数量懒加载测试。
 * <p>
 * 场景：模式节点展开时不应查询表/视图/函数/过程/序列的数量（旧实现会在渲染时同步执行数量查询，导致打开缓慢），
 * 只有对应类型列表真正加载后才显示数量，且数量直接取自已加载的子节点。
 *
 * @since 2026-11-27
 */
public class OracleTreeLazyCountTest {

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

    @Test
    public void typeNodeCountIsLazyLoaded() throws Throwable {
        runOnFx(() -> {
            // 未设置client，旧实现的数量查询会直接空指针，可据此确保不再发起数量查询
            ShellOracleTreeView treeView = new ShellOracleTreeView();
            OracleSchema schema = new OracleSchema();
            schema.setName("DEMO");
            ShellOracleSchemaTreeItem schemaItem = new ShellOracleSchemaTreeItem(schema, treeView);
            ShellOracleTablesTreeItem tablesItem = new ShellOracleTablesTreeItem(treeView);
            schemaItem.addChild(tablesItem);

            // 未加载时应返回空，且不访问数据库
            assertNull("unloaded type node should not expose table count", tablesItem.getTableSize());
            assertNull("unloaded type node should not render table count", tablesItem.getValue().extra());
            assertEquals(I18nHelper.table(), tablesItem.getValue().text());

            // 加载完成后数量直接来自已加载的子节点
            tablesItem.setLoaded(true);
            OracleTable table = new OracleTable();
            table.setName("T_DEMO");
            tablesItem.addTable(table);
            assertEquals(Integer.valueOf(1), tablesItem.getTableSize());
            assertEquals(" (1)", tablesItem.getValue().extra());
            assertEquals(I18nHelper.table() + " (1)", tablesItem.getValue().text());

            // 加载中不显示数量，避免出现 (0) 的误导
            tablesItem.setLoading(true);
            assertNull(tablesItem.getTableSize());
            assertEquals(I18nHelper.table(), tablesItem.getValue().text());
            tablesItem.setLoading(false);

            // 删除子节点后数量同步减少
            tablesItem.removeChild(tablesItem.firstChild());
            assertEquals(Integer.valueOf(0), tablesItem.getTableSize());

            // 关闭（清空）后恢复为未加载状态，数量不再显示
            tablesItem.clearChild();
            tablesItem.setLoaded(false);
            assertNull(tablesItem.getTableSize());
        });
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
