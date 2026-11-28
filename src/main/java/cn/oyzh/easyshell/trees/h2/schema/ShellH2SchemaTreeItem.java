package cn.oyzh.easyshell.trees.h2.schema;

import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.easyshell.db.index.ShellSelectIndexParam;
import cn.oyzh.easyshell.db.trigger.ShellSelectTriggerParam;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.function.ShellH2FunctionsTreeItem;
import cn.oyzh.easyshell.trees.h2.procedure.ShellH2ProceduresTreeItem;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueriesTreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.sequence.ShellH2SequencesTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TablesTreeItem;
import cn.oyzh.easyshell.trees.h2.terminal.ShellH2TerminalTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewsTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.h2.ShellH2ViewFactory;
import cn.oyzh.fx.db.DBIndex;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 模式节点。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2SchemaTreeItem extends ShellH2TreeItem<ShellH2SchemaTreeItemValue>
        implements ShellIndexTarget, ShellTriggerTarget {

    private final H2Schema value;

    public ShellH2SchemaTreeItem(H2Schema value, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(true);
        this.value = value;
        this.setValue(new ShellH2SchemaTreeItemValue(this));
    }

    @Override
    public ShellH2RootTreeItem parent() {
        return (ShellH2RootTreeItem) super.parent();
    }

    public String schema() {
        return this.value.getName();
    }

    public H2Schema value() {
        return this.value;
    }

    /**
     * 更新模式节点名称。
     *
     * @param name 新模式名称
     */
    public void setSchemaName(String name) {
        this.value.setName(name);
        this.refresh();
    }

    public String dbName() {
        return this.schema();
    }

    public ShellH2Client client() {
        return this.parent().client();
    }

    public ShellConnect info() {
        return this.parent().info();
    }

    public String infoName() {
        return this.parent().infoName();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        if (!this.isChildEmpty()) {
            items.add(MenuItemHelper.closeSchema(this::closeDB));
        }
        items.add(MenuItemHelper.editSchema(this::editDB));
        items.add(MenuItemHelper.deleteSchema(this::delete));
        items.add(MenuItemManager.getSeparatorMenuItem());
        items.add(MenuItemHelper.dumpData(this::dump));
        items.add(MenuItemHelper.runSqlFile(this::runSqlFile));
        items.add(MenuItemHelper.transportData(this::transportData));
        return items;
    }

    /**
     * 转储当前模式。
     */
    private void dump() {
        ShellH2ViewFactory.dumpData(this.client(), this.schema(), null, 1, 0);
    }

    /**
     * 在当前模式中运行 SQL 文件。
     */
    private void runSqlFile() {
        ShellH2ViewFactory.runSqlFile(this.client(), this.schema());
    }

    /**
     * 以当前模式为来源传输数据。
     */
    private void transportData() {
        ShellH2ViewFactory.transportData(this.info(), this.schema());
    }

    /**
     * 关闭模式节点。
     */
    public void closeDB() {
        this.parent().tabPane().closeSchemaTabs(this.schema());
        this.clearChild();
        this.collapse();
        this.setLoaded(false);
    }

    /**
     * 编辑模式名称。
     */
    public void editDB() {
        ShellH2ViewFactory.updateSchema(this.value, this.parent());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(
                ShellI18nHelper.confirmDeleteSchema(this.schema()), this.getWindow())) {
            return;
        }
        try {
            this.parent().dropSchema(this.schema());
            this.closeDB();
            this.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public String connectName() {
        return this.infoName();
    }

    @Override
    public DBDialect dialect() {
        return this.client().dialect();
    }

    @Override
    public void loadChild() {
        if (this.isLoading() || this.isLoaded()) {
            return;
        }
        this.setLoading(true);
        try {
            List<TreeItem<?>> items = new ArrayList<>();
            items.add(new ShellH2TablesTreeItem(this.getTreeView()));
            items.add(new ShellH2ViewsTreeItem(this.getTreeView()));
            items.add(new ShellIndexesTreeItem(this.getTreeView(), this));
            items.add(new ShellTriggersTreeItem(this.getTreeView(), this));
            items.add(new ShellH2FunctionsTreeItem(this.getTreeView()));
            items.add(new ShellH2ProceduresTreeItem(this.getTreeView()));
            items.add(new ShellH2SequencesTreeItem(this.getTreeView()));
            items.add(new ShellH2QueriesTreeItem(this.getTreeView()));
            items.add(new ShellH2TerminalTreeItem(this.getTreeView()));
            this.setChild(items);
            this.setLoaded(true);
            this.doFilter();
            this.expend();
        } finally {
            this.setLoading(false);
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    @Override
    public String indexScope() {
        return this.schema();
    }

    @Override
    public List<? extends DBIndex> selectIndexes() {
        return this.client().selectSchemaIndexes(new ShellSelectIndexParam(this.schema()));
    }

    @Override
    public String selectIndexDefinition(String indexName) {
        return this.client().selectIndexDefinition(this.schema(), indexName);
    }

    @Override
    public void executeIndexSql(List<String> sqlList) {
        this.client().executeIndexSql(this.schema(), sqlList);
    }

    @Override
    public boolean existIndex(String indexName) {
        return this.client().existIndex(this.schema(), indexName);
    }

    @Override
    public void dropIndex(String indexName, String tableName) {
        this.client().dropIndex(this.schema(), indexName, tableName);
    }

    @Override
    public DBIndex newIndex() {
        return new H2Index();
    }

    @Override
    public void reloadIndexes() {
        ShellIndexesTreeItem item = this.getIndexTypeChild();
        if (item != null) {
            if (item.isLoaded()) {
                item.reloadChild();
            } else {
                item.refresh();
            }
        }
    }

    public ShellIndexesTreeItem getIndexTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellIndexesTreeItem item) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String triggerScope() {
        return this.schema();
    }

    @Override
    public List<? extends DBTrigger> selectTriggers() {
        return this.client().selectSchemaTriggers(new ShellSelectTriggerParam(this.schema()));
    }

    @Override
    public String selectTriggerDefinition(String triggerName) {
        return this.client().selectTriggerDefinition(this.schema(), triggerName);
    }

    @Override
    public void executeTriggerSql(List<String> sqlList) {
        this.client().executeTriggerSql(this.schema(), sqlList);
    }

    @Override
    public boolean existTrigger(String triggerName) {
        return this.client().existTrigger(this.schema(), triggerName);
    }

    @Override
    public void dropTrigger(String triggerName, String tableName) {
        this.client().dropTrigger(this.schema(), triggerName, tableName);
    }

    @Override
    public DBTrigger newTrigger() {
        return new H2Trigger();
    }

    @Override
    public void reloadTriggers() {
        ShellTriggersTreeItem item = this.getTriggerTypeChild();
        if (item != null) {
            if (item.isLoaded()) {
                item.reloadChild();
            } else {
                item.refresh();
            }
        }
    }

    public ShellTriggersTreeItem getTriggerTypeChild() {
        for (RichTreeItem<?> child : this.richChildren()) {
            if (child instanceof ShellTriggersTreeItem item) {
                return item;
            }
        }
        return null;
    }
}
