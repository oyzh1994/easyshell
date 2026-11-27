package cn.oyzh.easyshell.oracle;

import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeView;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Oracle索引树节点展示测试。
 * <p>
 * 索引隶属于表/视图，节点文本为“表名.索引名”；无表名时仅展示索引名称。
 *
 * @since 2026-11-28
 */
public class OracleIndexTreeDisplayTest {

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
     * 构造索引
     *
     * @param name      名称
     * @param tableName 表名称
     * @return 索引
     */
    private OracleIndex index(String name, String tableName) {
        OracleIndex index = new OracleIndex();
        index.setSchema("DEMO");
        index.setName(name);
        index.setTableName(tableName);
        return index;
    }

    @Test
    public void indexNodeShowsTableQualifiedName() throws Throwable {
        runOnFx(() -> {
            ShellOracleTreeView view = new ShellOracleTreeView();
            // 有表名时展示“表名.索引名”
            ShellIndexTreeItem item = new ShellIndexTreeItem(this.index("IDX_NAME", "T_USER"), view, null);
            assertEquals("T_USER.IDX_NAME", item.getValue().name());
            // 无表名（如部分数据库的集合索引）只展示索引名称
            ShellIndexTreeItem item1 = new ShellIndexTreeItem(this.index("IDX_NAME", null), view, null);
            assertEquals("IDX_NAME", item1.getValue().name());
            // 空表名同样只展示索引名称
            ShellIndexTreeItem item2 = new ShellIndexTreeItem(this.index("IDX_NAME", ""), view, null);
            assertEquals("IDX_NAME", item2.getValue().name());
        });
    }

    @Test
    public void typeNodeKeepsLazyCount() throws Throwable {
        runOnFx(() -> {
            ShellOracleTreeView view = new ShellOracleTreeView();
            ShellIndexesTreeItem item = new ShellIndexesTreeItem(view, null);
            // 未加载时不展示数量，也不访问数据库
            assertEquals(null, item.getIndexSize());
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
