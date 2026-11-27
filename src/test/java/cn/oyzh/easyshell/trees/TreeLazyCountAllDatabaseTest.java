package cn.oyzh.easyshell.trees;

import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeView;
import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionsTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProceduresTreeItem;
import cn.oyzh.easyshell.trees.dameng.sequence.ShellDamengSequencesTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTablesTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeView;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProceduresTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTablesTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewsTreeItem;
import cn.oyzh.easyshell.trees.mongo.ShellMongoTreeView;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketsTreeItem;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.user.ShellMongoUsersTreeItem;
import cn.oyzh.easyshell.trees.mongo.view.ShellMongoViewsTreeItem;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeView;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventsTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTablesTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewsTreeItem;
import cn.oyzh.easyshell.trees.oracle.ShellOracleTreeView;
import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionsTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProceduresTreeItem;
import cn.oyzh.easyshell.trees.oracle.sequence.ShellOracleSequencesTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTablesTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewsTreeItem;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeView;
import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.postgresql.sequence.ShellPostgresqlSequencesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTablesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewsTreeItem;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewsTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeView;
import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProceduresTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.sequence.ShellSqlServerSequencesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 各数据库树类型节点数量懒加载测试。
 * <p>
 * 场景：打开模式/数据库节点时不应查询表、视图、函数等数量（旧实现会在渲染时同步查询，导致打开缓慢），
 * 只有对应类型列表真正加载后才显示数量，且数量直接取自已加载的子节点。
 * <p>
 * 说明：树视图不设置客户端，若数量取值再回退为查询数据库，这里会直接空指针失败。
 *
 * @since 2026-11-27
 */
public class TreeLazyCountAllDatabaseTest {

    /**
     * 待校验的类型节点
     */
    private static final List<Object[]> NODES = new ArrayList<>();

    @BeforeClass
    public static void setUp() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));

        ShellOracleTreeView oracle = new ShellOracleTreeView();
        add(oracle, new ShellOracleTablesTreeItem(oracle), "getTableSize");
        add(oracle, new ShellOracleViewsTreeItem(oracle), "getViewSize");
        add(oracle, new ShellOracleFunctionsTreeItem(oracle), "getFunctionSize");
        add(oracle, new ShellOracleProceduresTreeItem(oracle), "getProcedureSize");
        add(oracle, new ShellOracleSequencesTreeItem(oracle), "getSequenceSize");

        ShellMysqlTreeView mysql = new ShellMysqlTreeView();
        add(mysql, new ShellMysqlTablesTreeItem(mysql), "getTableSize");
        add(mysql, new ShellMysqlViewsTreeItem(mysql), "getViewSize");
        add(mysql, new ShellMysqlFunctionsTreeItem(mysql), "getFunctionSize");
        add(mysql, new ShellMysqlProceduresTreeItem(mysql), "getProcedureSize");
        add(mysql, new ShellMysqlEventsTreeItem(mysql), "getEventSize");

        ShellMariadbTreeView mariadb = new ShellMariadbTreeView();
        add(mariadb, new ShellMariadbTablesTreeItem(mariadb), "getTableSize");
        add(mariadb, new ShellMariadbViewsTreeItem(mariadb), "getViewSize");
        add(mariadb, new ShellMariadbFunctionsTreeItem(mariadb), "getFunctionSize");
        add(mariadb, new ShellMariadbProceduresTreeItem(mariadb), "getProcedureSize");
        add(mariadb, new ShellMariadbEventsTreeItem(mariadb), "getEventSize");

        ShellPostgresqlTreeView postgresql = new ShellPostgresqlTreeView();
        add(postgresql, new ShellPostgresqlTablesTreeItem(postgresql), "getTableSize");
        add(postgresql, new ShellPostgresqlViewsTreeItem(postgresql), "getViewSize");
        add(postgresql, new ShellPostgresqlFunctionsTreeItem(postgresql), "getFunctionSize");
        add(postgresql, new ShellPostgresqlProceduresTreeItem(postgresql), "getProcedureSize");
        add(postgresql, new ShellPostgresqlSequencesTreeItem(postgresql), "getSequenceSize");

        ShellSqlServerTreeView sqlserver = new ShellSqlServerTreeView();
        add(sqlserver, new ShellSqlServerTablesTreeItem(sqlserver), "getTableSize");
        add(sqlserver, new ShellSqlServerViewsTreeItem(sqlserver), "getViewSize");
        add(sqlserver, new ShellSqlServerFunctionsTreeItem(sqlserver), "getFunctionSize");
        add(sqlserver, new ShellSqlServerProceduresTreeItem(sqlserver), "getProcedureSize");
        add(sqlserver, new ShellSqlServerSequencesTreeItem(sqlserver), "getSequenceSize");

        ShellDamengTreeView dameng = new ShellDamengTreeView();
        add(dameng, new ShellDamengTablesTreeItem(dameng), "getTableSize");
        add(dameng, new ShellDamengViewsTreeItem(dameng), "getViewSize");
        add(dameng, new ShellDamengFunctionsTreeItem(dameng), "getFunctionSize");
        add(dameng, new ShellDamengProceduresTreeItem(dameng), "getProcedureSize");
        add(dameng, new ShellDamengSequencesTreeItem(dameng), "getSequenceSize");

        ShellSqliteTreeView sqlite = new ShellSqliteTreeView();
        add(sqlite, new ShellSqliteTablesTreeItem(sqlite), "getTableSize");
        add(sqlite, new ShellSqliteViewsTreeItem(sqlite), "getViewSize");
        add(sqlite, new ShellSqliteFunctionsTreeItem(sqlite), "getFunctionSize");

        ShellMongoTreeView mongo = new ShellMongoTreeView();
        add(mongo, new ShellMongoCollectionsTreeItem(mongo), "getCollectionsSize");
        add(mongo, new ShellMongoViewsTreeItem(mongo), "getViewSize");
        add(mongo, new ShellMongoFunctionsTreeItem(mongo), "getFunctionSize");
        add(mongo, new ShellMongoUsersTreeItem(mongo), "getUserSize");
        add(mongo, new ShellMongoBucketsTreeItem(mongo), "getBucketsSize");

        assertTrue("未覆盖到所有数据库类型节点", NODES.size() >= 33);
    }

    private static void add(RichTreeView view, RichTreeItem<?> node, String getter) {
        NODES.add(new Object[]{node.getClass().getSimpleName(), node, getter});
    }

    @Test
    public void typeNodeCountsAreLazyLoaded() throws Throwable {
        runOnFx(() -> NODES.forEach(node -> {
            String label = (String) node[0];
            RichTreeItem<?> item = (RichTreeItem<?>) node[1];
            try {
                Method method = item.getClass().getMethod((String) node[2]);
                // 未加载时不显示数量，且不访问数据库
                assertNull(label + " 未加载时不应返回数量", method.invoke(item));
                assertNull(label + " 未加载时不应渲染数量", item.getValue().extra());
                // 加载完成后数量等于子节点数量，不发起数量查询
                item.setLoaded(true);
                assertEquals(label + " 加载后数量应等于子节点数", Integer.valueOf(0), method.invoke(item));
                // 加载中不显示数量
                item.setLoading(true);
                assertNull(label + " 加载中不应显示数量", method.invoke(item));
                item.setLoading(false);
            } catch (Exception ex) {
                throw new IllegalStateException(label + " 数量懒加载校验失败", ex);
            }
        }));
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
