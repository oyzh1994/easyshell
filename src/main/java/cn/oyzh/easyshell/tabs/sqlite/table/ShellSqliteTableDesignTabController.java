package cn.oyzh.easyshell.tabs.sqlite.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.check.SQLiteCheckControl;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumnControl;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumnDefinitionParser;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKey;
import cn.oyzh.easyshell.sqlite.foreignKey.SQLiteForeignKeyControl;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndex;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndexControl;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTriggerControl;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQLite 表设计器控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTableDesignTabController extends ParentTabController {

    @FXML
    private FXTabPane tabPane;

    @FXML
    private DBStatusTableView<SQLiteColumnControl> columnTable;

    @FXML
    private DBStatusTableView<SQLiteIndexControl> indexTable;

    @FXML
    private DBStatusTableView<SQLiteTriggerControl> triggerTable;

    @FXML
    private DBStatusTableView<SQLiteForeignKeyControl> foreignKeyTable;

    @FXML
    private DBStatusTableView<SQLiteCheckControl> checkTable;

    @FXML
    private FXCheckBox withoutRowid;

    @FXML
    private FXCheckBox strict;

    @FXML
    private FXTextArea constraints;

    @FXML
    private ShellDataEditor preview;

    @FXML
    private ShellSqliteTableColumnExtraController tableColumnExtraController;

    private ShellSqliteClient client;

    private ShellSqliteRootTreeItem dbItem;

    private SQLiteTable table;

    private String originalName;

    private boolean unsaved;

    private boolean initiating;

    private DBStatusListener statusListener;

    /**
     * 初始化
     *
     * @param table  表对象
     * @param dbItem 数据库树节点
     */
    public void init(SQLiteTable table, ShellSqliteRootTreeItem dbItem) {
        this.dbItem = dbItem;
        this.client = dbItem.client();
        this.table = table == null ? new SQLiteTable() : table;
        this.originalName = table == null ? null : table.getName();
        this.withoutRowid.setSelected(this.table.isWithoutRowid());
        this.strict.setSelected(this.table.isStrict());
        this.constraints.setText(StringUtil.blankToDefault(this.table.getConstraints(), ""));
        this.columnTable.setCtrlSAction(this::save);
        this.indexTable.setCtrlSAction(this::save);
        this.triggerTable.setCtrlSAction(this::save);
        this.foreignKeyTable.setCtrlSAction(this::save);
        this.checkTable.setCtrlSAction(this::save);
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        StageManager.showMask(this.getWindow(), this::loadData);
    }

    private void loadData() {
        this.initiating = true;
        try {
            this.initStatusListener();
            this.columnTable.reset();
            this.indexTable.reset();
            this.triggerTable.reset();
            this.foreignKeyTable.reset();
            this.checkTable.reset();
            if (StringUtil.isBlank(this.originalName)) {
                this.columnTable.setItem(new ArrayList<>());
                SQLiteColumnControl column = new SQLiteColumnControl();
                column.setCreated(true);
                column.setName("id");
                column.setType("INTEGER");
                column.setPrimaryKey(true);
                column.setAutoIncrement(true);
                this.columnTable.addItem(column);
                this.columnTable.selectLast();
                this.indexTable.setItem(new ArrayList<>());
                this.triggerTable.setItem(new ArrayList<>());
                this.foreignKeyTable.setItem(new ArrayList<>());
                this.checkTable.setItem(new ArrayList<>());
                this.unsaved = true;
            } else {
                SQLiteTable loaded = this.client.selectTable(this.originalName);
                if (loaded == null) {
                    throw new IllegalArgumentException("SQLite table does not exist: " + this.originalName);
                }
                this.table = loaded;
                this.withoutRowid.setSelected(loaded.isWithoutRowid());
                this.strict.setSelected(loaded.isStrict());
                String createSql = this.client.showCreate(loaded.getName());
                this.constraints.setText(StringUtil.blankToDefault(
                        SQLiteColumnDefinitionParser.baseTableConstraints(createSql), ""));
                this.columnTable.setItem(SQLiteColumnControl.of(loaded.getColumns()));
                this.indexTable.setItem(this.loadIndexes());
                this.triggerTable.setItem(this.loadTriggers());
                this.foreignKeyTable.setItem(SQLiteForeignKeyControl.of(
                        SQLiteColumnDefinitionParser.foreignKeys(createSql)));
                this.checkTable.setItem(SQLiteCheckControl.of(
                        SQLiteColumnDefinitionParser.checks(createSql)));
                this.columnTable.selectFirst();
                this.unsaved = false;
            }
            this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.client);
            this.updatePreview();
            this.flushTab();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            FXUtil.runPulse(() -> this.initiating = false);
        }
    }

    private List<SQLiteIndexControl> loadIndexes() {
        return SQLiteIndexControl.of(this.loadSchemaObjects("index").stream()
                .map(SQLiteIndex::of)
                .toList());
    }

    private List<SQLiteTriggerControl> loadTriggers() {
        return SQLiteTriggerControl.of(this.loadSchemaObjects("trigger").stream()
                .map(SQLiteTrigger::of)
                .toList());
    }

    private List<SQLiteSchemaObject> loadSchemaObjects(String type) {
        return this.client.selectSchemaObjects(type).stream()
                .filter(item -> StringUtil.equalsIgnoreCase(item.getTableName(), this.originalName))
                .filter(item -> StringUtil.isNotBlank(item.getSql()))
                .toList();
    }

    private void initStatusListener() {
        if (this.statusListener == null) {
            this.statusListener = new DBStatusListener() {
                @Override
                public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                    markUnsaved();
                }
            };
            this.columnTable.setStatusListener(this.statusListener);
            this.indexTable.setStatusListener(this.statusListener);
            this.triggerTable.setStatusListener(this.statusListener);
            this.foreignKeyTable.setStatusListener(this.statusListener);
            this.checkTable.setStatusListener(this.statusListener);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.constraints.textProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.withoutRowid.selectedProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.strict.selectedProperty().addListener((observable, oldValue, newValue) -> this.markUnsaved());
        this.columnTable.selectedIndexChanged((observable, oldValue, newValue) ->
                this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.client));
        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> this.onTabChanged(newValue));
    }

    private void onTabChanged(Tab tab) {
        String tabId = tab == null ? null : tab.getId();
        if (StringUtil.equalsAny(tabId, "columnTab", "indexTab", "triggerTab", "foreignKeyTab", "checkTab")) {
            NodeGroupUtil.display(this.getTab(), "action1");
        } else {
            NodeGroupUtil.disappear(this.getTab(), "action1");
        }
        if (StringUtil.equals(tabId, "previewTab")) {
            this.updatePreview();
        }
    }

    private void markUnsaved() {
        if (!this.initiating) {
            this.unsaved = true;
            this.flushTab();
        }
    }

    @FXML
    private void doAdd() {
        String tabId = this.tabPane.getSelectedItem() == null ? null : this.tabPane.getSelectedItem().getId();
        if (StringUtil.equals(tabId, "columnTab")) {
            this.addColumn();
        } else if (StringUtil.equals(tabId, "indexTab")) {
            this.addIndex();
        } else if (StringUtil.equals(tabId, "triggerTab")) {
            this.addTrigger();
        } else if (StringUtil.equals(tabId, "foreignKeyTab")) {
            this.addForeignKey();
        } else if (StringUtil.equals(tabId, "checkTab")) {
            this.addCheck();
        }
    }

    @FXML
    private void doDelete() {
        String tabId = this.tabPane.getSelectedItem() == null ? null : this.tabPane.getSelectedItem().getId();
        if (StringUtil.equals(tabId, "columnTab")) {
            this.deleteColumn();
        } else if (StringUtil.equals(tabId, "indexTab")) {
            this.deleteIndex();
        } else if (StringUtil.equals(tabId, "triggerTab")) {
            this.deleteTrigger();
        } else if (StringUtil.equals(tabId, "foreignKeyTab")) {
            this.deleteForeignKey();
        } else if (StringUtil.equals(tabId, "checkTab")) {
            this.deleteCheck();
        }
    }

    private void addColumn() {
        SQLiteColumnControl column = new SQLiteColumnControl();
        column.setCreated(true);
        column.setType("TEXT");
        this.columnTable.addItem(column);
        this.columnTable.selectLast();
        this.markUnsaved();
    }

    private void deleteColumn() {
        SQLiteColumn column = this.columnTable.getSelectedItem();
        if (column == null) {
            return;
        }
        if (!column.isCreated() && !MessageBox.confirm(I18nHelper.deleteField() + " " + column.getName(), this.getWindow())) {
            return;
        }
        this.columnTable.removeItem((SQLiteColumnControl) column);
        column.setDeleted(true);
        this.markUnsaved();
    }

    private void addIndex() {
        SQLiteColumn column = this.columnTable.getSelectedItem();
        if (column == null && !this.columnTable.getItems().isEmpty()) {
            column = this.columnTable.getItems().getFirst();
        }
        SQLiteIndex index = SQLiteIndex.newIndex(
                this.designTableName(), column == null ? null : column.getName());
        this.indexTable.addItem(SQLiteIndexControl.of(index));
        this.indexTable.selectLast();
        this.markUnsaved();
    }

    private void addTrigger() {
        SQLiteTrigger trigger = SQLiteTrigger.newTrigger(this.designTableName());
        this.triggerTable.addItem(SQLiteTriggerControl.of(trigger));
        this.triggerTable.selectLast();
        this.markUnsaved();
    }

    private void addForeignKey() {
        SQLiteForeignKeyControl foreignKey = new SQLiteForeignKeyControl();
        foreignKey.setName(DBUtil.genForeignKeyName());
        foreignKey.setCreated(true);
        this.foreignKeyTable.addItem(foreignKey);
        this.foreignKeyTable.selectLast();
        this.markUnsaved();
    }

    private void addCheck() {
        SQLiteCheckControl check = new SQLiteCheckControl();
        check.setName(DBUtil.genCheckName());
        check.setCreated(true);
        this.checkTable.addItem(check);
        this.checkTable.selectLast();
        this.markUnsaved();
    }

    private void deleteForeignKey() {
        SQLiteForeignKeyControl foreignKey = this.foreignKeyTable.getSelectedItem();
        if (foreignKey == null) {
            return;
        }
        if (!foreignKey.isCreated() && !MessageBox.confirm(
                I18nHelper.deleteForeignKey() + " " + foreignKey.getName(), this.getWindow())) {
            return;
        }
        this.foreignKeyTable.removeItem(foreignKey);
        foreignKey.setDeleted(true);
        this.markUnsaved();
    }

    private void deleteCheck() {
        SQLiteCheckControl check = this.checkTable.getSelectedItem();
        if (check == null) {
            return;
        }
        if (!check.isCreated() && !MessageBox.confirm(
                I18nHelper.deleteCheck() + " " + check.getName(), this.getWindow())) {
            return;
        }
        this.checkTable.removeItem(check);
        check.setDeleted(true);
        this.markUnsaved();
    }

    private void deleteIndex() {
        SQLiteIndex index = this.indexTable.getSelectedItem();
        if (index == null) {
            return;
        }
        if (!index.isCreated() && !MessageBox.confirm(I18nHelper.deleteIndex() + " " + index.getName(), this.getWindow())) {
            return;
        }
        this.indexTable.removeItem((SQLiteIndexControl) index);
        index.setDeleted(true);
        this.markUnsaved();
    }

    private void deleteTrigger() {
        SQLiteTrigger trigger = this.triggerTable.getSelectedItem();
        if (trigger == null) {
            return;
        }
        if (!trigger.isCreated() && !MessageBox.confirm(I18nHelper.deleteTrigger() + " " + trigger.getName(), this.getWindow())) {
            return;
        }
        this.triggerTable.removeItem((SQLiteTriggerControl) trigger);
        trigger.setDeleted(true);
        this.markUnsaved();
    }

    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    private void doSave() {
        String previousName = this.originalName;
        try {
            for (SQLiteColumnControl column : this.columnTable.getItems()) {
                if (column.isInvalid()) {
                    this.tabPane.selectTab("columnTab");
                    MessageBox.warn(I18nHelper.invalidColumn(), this.getWindow());
                    return;
                }
            }
            for (SQLiteIndexControl index : this.indexTable.getItems()) {
                if (!index.isDeleted() && index.isInvalid()) {
                    this.tabPane.selectTab("indexTab");
                    MessageBox.warn(I18nHelper.invalidIndex(), this.getWindow());
                    return;
                }
            }
            for (SQLiteTriggerControl trigger : this.triggerTable.getItems()) {
                if (!trigger.isDeleted() && trigger.isInvalid()) {
                    this.tabPane.selectTab("triggerTab");
                    MessageBox.warn(I18nHelper.invalidTrigger(), this.getWindow());
                    return;
                }
            }
            for (SQLiteForeignKeyControl foreignKey : this.foreignKeyTable.getItems()) {
                if (!foreignKey.isDeleted() && foreignKey.isInvalid()) {
                    this.tabPane.selectTab("foreignKeyTab");
                    MessageBox.warn(I18nHelper.invalidForeignKey(), this.getWindow());
                    return;
                }
            }
            for (SQLiteCheckControl check : this.checkTable.getItems()) {
                if (!check.isDeleted() && check.isInvalid()) {
                    this.tabPane.selectTab("checkTab");
                    MessageBox.warn(I18nHelper.invalidCheck(), this.getWindow());
                    return;
                }
            }
            String targetName = this.originalName;
            if (StringUtil.isBlank(targetName)) {
                targetName = MessageBox.prompt(I18nHelper.pleaseInputTableName(), this.getWindow());
                if (targetName == null) {
                    return;
                }
                targetName = targetName.trim();
                if (StringUtil.isBlank(targetName)) {
                    MessageBox.warn(I18nHelper.pleaseInputTableName(), this.getWindow());
                    return;
                }
            }
            SQLiteTable target = this.buildTable(targetName);
            SQLiteSchemaManager manager = new SQLiteSchemaManager(this.client);
            if (StringUtil.isBlank(previousName)) {
                manager.createTable(target);
            } else {
                this.dropChangedIndexes();
                this.dropChangedTriggers();
                manager.updateTable(previousName, target);
            }
            this.createChangedIndexes(previousName, targetName);
            this.createChangedTriggers(previousName, targetName);
            this.originalName = targetName;
            this.table = target;
            this.unsaved = false;
            ShellSqliteEventUtil.tableChanged(this.client, previousName, targetName);
            FXUtil.runWait(this::loadData);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    private SQLiteTable buildTable(String name) {
        SQLiteTable result = new SQLiteTable();
        result.setName(name);
        List<SQLiteColumn> columns = new ArrayList<>();
        for (SQLiteColumnControl column : this.columnTable.getItems()) {
            if (!column.isInvalid()) {
                columns.add(column);
            }
        }
        result.setColumns(columns);
        result.setConstraints(this.buildConstraints());
        result.setWithoutRowid(this.withoutRowid.isSelected());
        result.setStrict(this.strict.isSelected());
        return result;
    }

    private String buildConstraints() {
        List<String> definitions = new ArrayList<>();
        String base = this.constraints.getTextTrim();
        if (StringUtil.isNotBlank(base)) {
            definitions.add(base);
        }
        for (SQLiteForeignKeyControl foreignKey : this.foreignKeyTable.getItems()) {
            if (!foreignKey.isDeleted() && !foreignKey.isInvalid()) {
                definitions.add(foreignKey.toConstraintSql());
            }
        }
        for (SQLiteCheckControl check : this.checkTable.getItems()) {
            if (!check.isDeleted() && !check.isInvalid()) {
                definitions.add(check.toConstraintSql());
            }
        }
        return String.join(",\n", definitions);
    }

    private void dropChangedIndexes() {
        if (this.indexTable.getDeleteItems() != null) {
            for (SQLiteIndexControl index : this.indexTable.getDeleteItems()) {
                this.dropIndex(index);
            }
        }
        for (SQLiteIndexControl index : this.indexTable.getItems()) {
            if (!index.isCreated() && index.isChangedDefinition()) {
                this.dropIndex(index);
            }
        }
    }

    private void dropChangedTriggers() {
        if (this.triggerTable.getDeleteItems() != null) {
            for (SQLiteTriggerControl trigger : this.triggerTable.getDeleteItems()) {
                this.dropTrigger(trigger);
            }
        }
        for (SQLiteTriggerControl trigger : this.triggerTable.getItems()) {
            if (!trigger.isCreated() && trigger.isChangedDefinition()) {
                this.dropTrigger(trigger);
            }
        }
    }

    private void dropIndex(SQLiteIndex index) {
        String name = StringUtil.blankToDefault(index.originalName(), index.getName());
        if (StringUtil.isNotBlank(name)) {
            this.client.executeSqlSimple("DROP INDEX IF EXISTS " + SQLiteGeneratorUtil.quote(name));
        }
    }

    private void dropTrigger(SQLiteTrigger trigger) {
        String name = StringUtil.blankToDefault(trigger.originalName(), trigger.getName());
        if (StringUtil.isNotBlank(name)) {
            this.client.executeSqlSimple("DROP TRIGGER IF EXISTS " + SQLiteGeneratorUtil.quote(name));
        }
    }

    private void createChangedIndexes(String previousName, String targetName) {
        for (SQLiteIndexControl index : this.indexTable.getItems()) {
            if (index.isDeleted() || StringUtil.isBlank(index.getDefinition())) {
                continue;
            }
            if (index.isCreated() || index.isChangedDefinition()) {
                this.client.executeSqlSimple(index.normalizedDefinition(previousName, targetName));
            }
        }
    }

    private void createChangedTriggers(String previousName, String targetName) {
        for (SQLiteTriggerControl trigger : this.triggerTable.getItems()) {
            if (trigger.isDeleted() || StringUtil.isBlank(trigger.getDefinition())) {
                continue;
            }
            if (trigger.isCreated() || trigger.isChangedDefinition()) {
                this.client.executeSqlSimple(trigger.normalizedDefinition(previousName, targetName));
            }
        }
    }

    @FXML
    private void createPreview() {
        this.updatePreview();
    }

    @FXML
    private void refresh() {
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?", this.getWindow())) {
            return;
        }
        this.loadData();
    }

    @FXML
    private void doMoveUp() {
        try {
            String tabId = this.tabPane.getSelectedItem() == null
                    ? null : this.tabPane.getSelectedItem().getId();
            if (StringUtil.equals(tabId, "columnTab")) {
                TableViewUtil.moveUp(this.columnTable);
            } else if (StringUtil.equals(tabId, "indexTab")) {
                TableViewUtil.moveUp(this.indexTable);
            } else if (StringUtil.equals(tabId, "triggerTab")) {
                TableViewUtil.moveUp(this.triggerTable);
            } else if (StringUtil.equals(tabId, "foreignKeyTab")) {
                TableViewUtil.moveUp(this.foreignKeyTable);
            } else if (StringUtil.equals(tabId, "checkTab")) {
                TableViewUtil.moveUp(this.checkTable);
            }
            this.markUnsaved();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void doMoveDown() {
        try {
            String tabId = this.tabPane.getSelectedItem() == null
                    ? null : this.tabPane.getSelectedItem().getId();
            if (StringUtil.equals(tabId, "columnTab")) {
                TableViewUtil.moveDown(this.columnTable);
            } else if (StringUtil.equals(tabId, "indexTab")) {
                TableViewUtil.moveDown(this.indexTable);
            } else if (StringUtil.equals(tabId, "triggerTab")) {
                TableViewUtil.moveDown(this.triggerTable);
            } else if (StringUtil.equals(tabId, "foreignKeyTab")) {
                TableViewUtil.moveDown(this.foreignKeyTable);
            } else if (StringUtil.equals(tabId, "checkTab")) {
                TableViewUtil.moveDown(this.checkTable);
            }
            this.markUnsaved();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void updatePreview() {
        try {
            String name = this.designTableName();
            SQLiteTable target = this.buildTable(name);
            StringBuilder sql = new StringBuilder(SQLiteTableCreateSqlGenerator.createTable(name, target.getColumns(),
                    target.getConstraints(), target.isWithoutRowid(), target.isStrict()));
            for (SQLiteIndexControl index : this.indexTable.getItems()) {
                if (!index.isDeleted() && StringUtil.isNotBlank(index.getDefinition())) {
                    sql.append('\n').append(index.normalizedDefinition(this.originalName, name));
                }
            }
            for (SQLiteTriggerControl trigger : this.triggerTable.getItems()) {
                if (!trigger.isDeleted() && StringUtil.isNotBlank(trigger.getDefinition())) {
                    sql.append('\n').append(trigger.normalizedDefinition(this.originalName, name));
                }
            }
            this.preview.setText(sql.toString());
        } catch (Exception ex) {
            this.preview.setText("-- " + ex.getMessage());
        }
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.originalName;
    }

    private String designTableName() {
        return StringUtil.blankToDefault(this.originalName, "Unnamed_Table");
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return this.unsaved;
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellSqliteRootTreeItem getDbItem() {
        return this.dbItem;
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.tableColumnExtraController);
    }

    @Override
    public void destroy() {
        if (this.preview != null) {
            this.preview.destroy();
        }
        super.destroy();
    }
}
