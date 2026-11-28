package cn.oyzh.easyshell.trees.h2.root;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.db.trigger.ShellSelectTriggerParam;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.tabs.h2.ShellH2TabPane;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeView;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueriesTreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TablesTreeItem;
import cn.oyzh.easyshell.trees.h2.terminal.ShellH2TerminalTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewsTreeItem;
import cn.oyzh.easyshell.util.h2.ShellH2ViewFactory;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * H2树根节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2RootTreeItem extends ShellH2TreeItem<ShellH2RootTreeItemValue> implements ShellIndexTarget, ShellTriggerTarget {

    /**
     * 构造H2树根节点
     *
     * @param treeView 树视图
     */
    public ShellH2RootTreeItem(ShellH2TreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.setValue(new ShellH2RootTreeItemValue());
    }

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client client() {
        return this.getTreeView().getClient();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.client().getShellConnect();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.info().getName();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect connect() {
        return this.info();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.client().schema();
    }

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    @Override
    public DBDialect dialect() {
        return this.client().dialect();
    }

    /**
     * 执行SQL
     *
     * @param sql SQL
     * @return 执行结果
     */
    public DBQueryResults<H2ExecuteResult> executeSql(String sql) {
        return this.client().executeSql(sql);
    }

    public DBQueryResults<H2ExecuteResult> executeSql(String dbName, String sql) {
        return this.client().executeSql(dbName, sql);
    }

    /**
     * 执行单条SQL
     *
     * @param sql SQL
     * @return 执行结果
     */
    public H2ExecuteResult executeSingleSql(String sql) {
        return this.client().executeSingleSql(sql);
    }

    public H2ExecuteResult executeSingleSql(String dbName, String sql) {
        return this.client().executeSingleSql(dbName, sql);
    }

    /**
     * 解释SQL
     *
     * @param sql SQL
     * @return 解释结果
     */
    public DBQueryResults<H2ExecuteResult> explainSql(String sql) {
        return this.client().explainSql(sql);
    }

    public DBQueryResults<H2ExecuteResult> explainSql(String dbName, String sql) {
        return this.client().explainSql(dbName, sql);
    }

    /**
     * 获取数据库标签页
     *
     * @return 数据库标签页
     */
    public ShellH2TabPane tabPane() {
        return this.getTreeView().getTabPane();
    }

    @Override
    public void loadChild() {
        this.clearChild();
        List<TreeItem<?>> items = new ArrayList<>();
        for (H2Schema schema : this.client().selectSchemas()) {
            items.add(new ShellH2SchemaTreeItem(schema, this.getTreeView()));
        }
        this.setChild(items);
        this.doFilter();
        this.expend();
    }

    @Override
    public void reloadChild() {
        this.loadChild();
    }

    /**
     * 新增或更新查询节点
     *
     * @param query 查询对象
     */
    public void upsertQuery(ShellQuery query) {
        ShellH2QueriesTreeItem queryGroup = this.queryGroup(query.getDbName());
        if (queryGroup != null) {
            queryGroup.upsertQuery(query);
        }
    }

    /**
     * 删除查询节点
     *
     * @param query 查询对象
     */
    public void removeQuery(ShellQuery query) {
        ShellH2QueriesTreeItem queryGroup = this.queryGroup(query.getDbName());
        if (queryGroup != null) {
            queryGroup.removeQuery(query);
        }
    }

    private ShellH2QueriesTreeItem queryGroup(String schema) {
        ShellH2SchemaTreeItem schemaItem = this.schemaGroup(
                StringUtil.blankToDefault(schema, this.client().schema()));
        if (schemaItem == null) {
            return null;
        }
        if (!schemaItem.isLoaded()) {
            schemaItem.loadChild();
        }
        for (TreeItem<?> child : schemaItem.unfilteredChildren()) {
            if (child instanceof ShellH2QueriesTreeItem group) {
                return group;
            }
        }
        return null;
    }

    /**
     * 判断模式是否存在。
     *
     * @param schema 模式名称
     * @return 是否存在
     */
    public boolean existSchema(String schema) {
        return this.client().findSchema(schema) != null;
    }

    /**
     * 新增模式。
     */
    private void addSchema() {
        StageAdapter adapter = ShellH2ViewFactory.addSchema(this);
        if (adapter == null) {
            return;
        }
        String schemaName = adapter.getProp("databaseName");
        if (StringUtil.isNotBlank(schemaName)) {
            this.addSchema(this.client().findSchema(schemaName));
        }
    }

    /**
     * 添加模式节点。
     *
     * @param schema 模式
     */
    public void addSchema(H2Schema schema) {
        if (schema != null && this.schemaGroup(schema.getName()) == null) {
            this.addChild(new ShellH2SchemaTreeItem(schema, this.getTreeView()));
            this.doFilter();
            this.doSort();
            this.expend();
        }
    }

    /**
     * 重命名模式。
     *
     * @param originalName 原名称
     * @param targetName   新名称
     */
    public void renameSchema(String originalName, String targetName) {
        this.client().renameSchema(originalName, targetName);
    }

    /**
     * 删除模式。
     *
     * @param schema 模式名称
     */
    public void dropSchema(String schema) {
        this.client().dropSchema(schema);
    }

    /**
     * 新增视图节点
     *
     * @param view 视图对象
     */
    public void addView(H2View view) {
        String schema = view == null ? this.client().schema() : view.getDbName();
        ShellH2ViewsTreeItem viewGroup = this.viewGroup(schema);
        if (viewGroup != null && view != null) {
            viewGroup.addView(view);
        }
    }

    /**
     * 新增视图节点
     *
     * @param viewName 视图名称
     */
    public void addView(String viewName) {
        this.addView(this.client().schema(), viewName);
    }

    public void addView(String schema, String viewName) {
        this.addView(this.client().selectView(schema, viewName));
    }

    /**
     * 新增表节点
     *
     * @param table 表对象
     */
    public void addTable(H2Table table) {
        String schema = table == null ? this.client().schema() : table.getDbName();
        ShellH2TablesTreeItem tableGroup = this.tableGroup(schema);
        if (tableGroup != null && table != null) {
            tableGroup.addTable(table);
        }
    }

    /**
     * 新增表节点
     *
     * @param tableName 表名称
     */
    public void addTable(String tableName) {
        this.addTable(this.client().schema(), tableName);
    }

    public void addTable(String schema, String tableName) {
        this.addTable(this.client().selectTable(schema, tableName));
    }

    /**
     * 刷新视图数量
     */
    public void refreshViewCount() {
        this.refreshViewCount(this.client().schema());
    }

    public void refreshViewCount(String schema) {
        ShellH2ViewsTreeItem viewGroup = this.viewGroup(schema);
        if (viewGroup != null) {
            viewGroup.refreshCount();
        }
    }

    /**
     * 刷新表数量
     */
    public void refreshTableCount() {
        this.refreshTableCount(this.client().schema());
    }

    public void refreshTableCount(String schema) {
        ShellH2TablesTreeItem tableGroup = this.tableGroup(schema);
        if (tableGroup != null) {
            tableGroup.refreshCount();
        }
    }

    /**
     * 获取表分组节点
     *
     * @return 表分组节点
     */
    private ShellH2TablesTreeItem tableGroup(String schema) {
        ShellH2SchemaTreeItem schemaItem = this.schemaGroup(schema);
        if (schemaItem != null) {
            for (TreeItem<?> child : schemaItem.unfilteredChildren()) {
                if (child instanceof ShellH2TablesTreeItem group) {
                    return group;
                }
            }
        }
        return null;
    }

    /**
     * 获取视图分组节点
     *
     * @return 视图分组节点
     */
    private ShellH2ViewsTreeItem viewGroup(String schema) {
        ShellH2SchemaTreeItem schemaItem = this.schemaGroup(schema);
        if (schemaItem != null) {
            for (TreeItem<?> child : schemaItem.unfilteredChildren()) {
                if (child instanceof ShellH2ViewsTreeItem group) {
                    return group;
                }
            }
        }
        return null;
    }

    /**
     * 查找模式节点。
     *
     * @param schema 模式名称
     * @return 模式节点
     */
    public ShellH2SchemaTreeItem schemaGroup(String schema) {
        for (TreeItem<?> child : this.unfilteredChildren()) {
            if (child instanceof ShellH2SchemaTreeItem item
                    && StringUtil.equalsIgnoreCase(item.schema(), schema)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.tabPane().showHome();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(MenuItemHelper.addSchema(this::addSchema));
        FXMenuItem reloadDatabase = MenuItemHelper.reloadDatabase(this::reloadChild);
        items.add(reloadDatabase);
        return items;
    }

    @Override
    public String connectName() {
        return this.infoName();
    }

    @Override
    public String indexScope() {
        return this.dbName();
    }

    @Override
    public List<? extends DBIndex> selectIndexes() {
        return this.client().selectSchemaIndexes(new ShellSelectIndexParam(this.dbName()));
    }

    @Override
    public String selectIndexDefinition(String indexName) {
        return this.client().selectIndexDefinition(this.dbName(), indexName);
    }

    @Override
    public void executeIndexSql(List<String> sqlList) {
        this.client().executeIndexSql(this.dbName(), sqlList);
    }

    @Override
    public boolean existIndex(String indexName) {
        return this.client().existIndex(this.dbName(), indexName);
    }

    @Override
    public void dropIndex(String indexName, String tableName) {
        this.client().dropIndex(this.dbName(), indexName, tableName);
    }

    @Override
    public DBIndex newIndex() {
        return new H2Index();
    }

    @Override
    public void reloadIndexes() {
        ShellIndexesTreeItem typeChild = this.getIndexTypeChild();
        if (typeChild == null) {
            return;
        }
        if (typeChild.isLoaded()) {
            typeChild.reloadChild();
        } else {
            typeChild.refresh();
        }
    }

    /**
     * 获取索引类型子节点
     *
     * @return 索引类型子节点
     */
    public ShellIndexesTreeItem getIndexTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellIndexesTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }

    @Override
    public String triggerScope() {
        return this.indexScope();
    }

    @Override
    public List<? extends DBTrigger> selectTriggers() {
        return this.client().selectSchemaTriggers(new ShellSelectTriggerParam(this.dbName()));
    }

    @Override
    public String selectTriggerDefinition(String triggerName) {
        return this.client().selectTriggerDefinition(this.dbName(), triggerName);
    }

    @Override
    public void executeTriggerSql(List<String> sqlList) {
        this.client().executeTriggerSql(this.dbName(), sqlList);
    }

    @Override
    public boolean existTrigger(String triggerName) {
        return this.client().existTrigger(this.dbName(), triggerName);
    }

    @Override
    public void dropTrigger(String triggerName, String tableName) {
        this.client().dropTrigger(this.dbName(), triggerName, tableName);
    }

    @Override
    public DBTrigger newTrigger() {
        return new H2Trigger();
    }

    @Override
    public void reloadTriggers() {
        ShellTriggersTreeItem typeChild = this.getTriggerTypeChild();
        if (typeChild == null) {
            return;
        }
        if (typeChild.isLoaded()) {
            typeChild.reloadChild();
        } else {
            typeChild.refresh();
        }
    }

    /**
     * 获取触发器类型子节点
     *
     * @return 触发器类型子节点
     */
    public ShellTriggersTreeItem getTriggerTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellTriggersTreeItem treeItem) {
                return treeItem;
            }
        }
        return null;
    }
}
