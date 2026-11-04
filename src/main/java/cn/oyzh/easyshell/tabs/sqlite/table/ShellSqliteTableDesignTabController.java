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
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
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
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 表设计器控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTableDesignTabController extends ParentTabController {

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 表字段组件
     */
    @FXML
    private DBStatusTableView<SQLiteColumnControl> columnTable;

    /**
     * 表索引组件
     */
    @FXML
    private DBStatusTableView<SQLiteIndexControl> indexTable;

    /**
     * 表触发器组件
     */
    @FXML
    private DBStatusTableView<SQLiteTriggerControl> triggerTable;

    /**
     * 表外键组件
     */
    @FXML
    private DBStatusTableView<SQLiteForeignKeyControl> foreignKeyTable;

    /**
     * 表检查器组件
     */
    @FXML
    private DBStatusTableView<SQLiteCheckControl> checkTable;

    /**
     * 无行ID
     */
    @FXML
    private FXCheckBox withoutRowid;

    /**
     * 严格模式
     */
    @FXML
    private FXCheckBox strict;

    /**
     * 表约束
     */
    @FXML
    private FXTextArea constraints;

    /**
     * sql预览
     */
    @FXML
    private ShellDataEditor preview;

    /**
     * 额外信息
     */
    @FXML
    private ShellSqliteTableColumnExtraController tableColumnExtraController;

    /**
     * 客户端
     */
    private ShellSqliteClient client;

    /**
     * db库节点
     */
    private ShellSqliteRootTreeItem dbItem;

    /**
     * db表
     */
    private SQLiteTable table;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 原表名称
     */
    private String originalName;

    /**
     * 未保存标志位
     */
    private boolean unsaved;

    /**
     * 新数据标志位
     */
    private boolean newData;

    /**
     * 初始化中标志位
     */
    private boolean initiating;

    /**
     * 数据监听器
     */
    private DBStatusListener listener;

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
        // 更新新数据标志位（刷新新建表时，表对象不为空但表名仍为空）
        this.newData = table == null || StringUtil.isBlank(table.getName());
        this.originalName = this.newData ? null : table.getName();
        this.tableName = this.originalName;
        this.withoutRowid.setSelected(this.table.isWithoutRowid());
        this.strict.setSelected(this.table.isStrict());
        this.constraints.setText(StringUtil.blankToDefault(this.table.getConstraints(), ""));
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        this.initDBListener();
        FXUtil.runWait(this::initInfo);
    }

    /**
     * 初始化信息
     */
    protected void initInfo() {
        this.initiating = true;
        try {
            // 新数据
            if (this.newData) {
                this.unsaved = true;
                this.initNew();
            } else {// 已有数据
                this.table = this.client.selectTable(this.table.getName());
                this.initNormal();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            FXUtil.runPulse(() -> this.initiating = false);
        }
    }

    /**
     * 初始化新建表信息
     */
    protected void initNew() {
        NodeGroupUtil.display(this.getTab(), "action2");
        NodeGroupUtil.disappear(this.getTab(), "action3");
        // 重载表数据
        this.columnTable.setItem(new ArrayList<>());
        this.indexTable.setItem(new ArrayList<>());
        this.triggerTable.setItem(new ArrayList<>());
        this.foreignKeyTable.setItem(new ArrayList<>());
        this.checkTable.setItem(new ArrayList<>());
    }

    /**
     * 初始化已有表信息
     */
    protected void initNormal() {
        if (this.table == null) {
            throw new IllegalArgumentException("SQLite table does not exist: " + this.originalName);
        }
        NodeGroupUtil.disappear(this.getTab(), "action2");
        NodeGroupUtil.display(this.getTab(), "action3");

        // 基本信息
        this.withoutRowid.setSelected(this.table.isWithoutRowid());
        this.strict.setSelected(this.table.isStrict());
        String createSql = this.client.showCreate(this.table.getName());
        this.constraints.setText(StringUtil.blankToDefault(
                SQLiteColumnDefinitionParser.baseTableConstraints(createSql), ""));

        // 检查器
        this.checkTable.setItem(SQLiteCheckControl.of(
                SQLiteColumnDefinitionParser.checks(createSql)));
        // 索引
        this.indexTable.setItem(this.loadIndexes());
        // 字段
        this.columnTable.setItem(SQLiteColumnControl.of(this.table.getColumns()));
        // 触发器
        this.triggerTable.setItem(this.loadTriggers());
        // 外键
        this.foreignKeyTable.setItem(SQLiteForeignKeyControl.of(
                SQLiteColumnDefinitionParser.foreignKeys(createSql)));

        this.initIndexTable();
        this.initForeignKeyTable();
        this.columnTable.selectFirst();
        this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.client);
        // 重置保存标志位
        this.unsaved = false;
        this.flushTab();
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

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        if (this.listener == null) {
            this.listener = new DBStatusListener() {
                @Override
                public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                    initChangedFlag();
                }
            };

            // 监听列表变化
            this.checkTable.setStatusListener(this.listener);
            this.indexTable.setStatusListener(this.listener);
            this.columnTable.setStatusListener(this.listener);
            this.triggerTable.setStatusListener(this.listener);
            this.foreignKeyTable.setStatusListener(this.listener);

            // 监听组件
            DBStatusListenerManager.bindListener(this.constraints, this.listener);
            DBStatusListenerManager.bindListener(this.withoutRowid, this.listener);
            DBStatusListenerManager.bindListener(this.strict, this.listener);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 表格下标监听
        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> this.onTabChanged(newValue));

        this.columnTable.selectedIndexChanged((observable, oldValue, newValue) ->
                this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.client));

        // 初始化索引列表
        this.indexTable.itemList().addListener((ListChangeListener<SQLiteIndexControl>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initIndexTable();
            }
        });
        this.initIndexTable();
        // 初始化外键列表
        this.foreignKeyTable.itemList().addListener((ListChangeListener<SQLiteForeignKeyControl>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initForeignKeyTable();
            }
        });
        this.initForeignKeyTable();

        this.initTable();
    }

    /**
     * 初始化列表控件
     */
    /**
     * 初始化列表控件
     */
    private void initTable() {
        // 表单保存事件
        this.indexTable.setCtrlSAction(this::save);
        this.checkTable.setCtrlSAction(this::save);
        this.columnTable.setCtrlSAction(this::save);
        this.triggerTable.setCtrlSAction(this::save);
        this.foreignKeyTable.setCtrlSAction(this::save);

        // 监听事件
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.constraints, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);

        // 更新字段列表
        this.columnTable.itemList().addListener((ListChangeListener<SQLiteColumnControl>) change -> {
            this.initIndexTable();
            this.initForeignKeyTable();
        });
    }

    /**
     * 初始化索引表格
     */
    private void initIndexTable() {
        for (SQLiteIndexControl index : this.indexTable.getItems()) {
            index.setColumnList(this.columnTable.getItems());
        }
    }

    /**
     * 初始化外键表格
     */
    private void initForeignKeyTable() {
        for (SQLiteForeignKeyControl foreignKey : this.foreignKeyTable.getItems()) {
            foreignKey.setColumnList(this.columnTable.getItems());
            foreignKey.setDbClient(this.client);
        }
    }

    /**
     * 切换面板处理
     *
     * @param tab 面板
     */
    private void onTabChanged(Tab tab) {
        String tabId = tab == null ? null : tab.getId();
        if (StringUtil.equalsAny(tabId, "columnTab", "indexTab", "triggerTab", "foreignKeyTab", "checkTab")) {
            NodeGroupUtil.display(this.getTab(), "action1");
            if (this.newData) {
                NodeGroupUtil.display(this.getTab(), "action2");
            }
        } else {
            NodeGroupUtil.disappear(this.getTab(), "action1");
            if (this.newData) {
                NodeGroupUtil.disappear(this.getTab(), "action2");
            }
        }
        if (StringUtil.equals(tabId, "previewTab")) {
            this.initPreview();
        }
    }

    /**
     * 初始化变更标志
     */
    private void initChangedFlag() {
        if (!this.initiating) {
            this.unsaved = true;
            this.flushTab();
        }
    }

    @FXML
    private void doAdd() {
        if (this.tabPane.isSelectedTab("columnTab")) {
            this.addColumn();
        } else if (this.tabPane.isSelectedTab("indexTab")) {
            this.addIndex();
        } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
            this.addForeignKey();
        } else if (this.tabPane.isSelectedTab("triggerTab")) {
            this.addTrigger();
        } else if (this.tabPane.isSelectedTab("checkTab")) {
            this.addCheck();
        }
    }

    @FXML
    private void doDelete() {
        if (this.tabPane.isSelectedTab("columnTab")) {
            this.deleteColumn();
        } else if (this.tabPane.isSelectedTab("indexTab")) {
            this.deleteIndex();
        } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
            this.deleteForeignKey();
        } else if (this.tabPane.isSelectedTab("triggerTab")) {
            this.deleteTrigger();
        } else if (this.tabPane.isSelectedTab("checkTab")) {
            this.deleteCheck();
        }
    }

    private void addColumn() {
        SQLiteColumnControl column = new SQLiteColumnControl();
        column.setCreated(true);
        column.setType("TEXT");
        column.setNullable(true);
        this.columnTable.addItem(column);
        this.columnTable.selectLast();
    }

    private void deleteColumn() {
        try {
            SQLiteColumn column = this.columnTable.getSelectedItem();
            if (column == null) {
                return;
            }
            // 确认操作
            if (!column.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteField", column.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.columnTable.removeItem((SQLiteColumnControl) column);
            column.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void addIndex() {
        SQLiteIndexControl index = new SQLiteIndexControl();
        index.setTableName(this.designTableName());
        index.setCreated(true);
        this.indexTable.addItem(index);
        this.initIndexTable();
        this.indexTable.selectLast();
    }

    private void addTrigger() {
        SQLiteTriggerControl trigger = new SQLiteTriggerControl();
        trigger.setTableName(this.designTableName());
        trigger.setCreated(true);
        this.triggerTable.addItem(trigger);
        this.triggerTable.selectLast();
    }

    private void addForeignKey() {
        SQLiteForeignKeyControl foreignKey = new SQLiteForeignKeyControl();
        foreignKey.setName(DBUtil.genForeignKeyName());
        foreignKey.setCreated(true);
        this.foreignKeyTable.addItem(foreignKey);
        this.initForeignKeyTable();
        this.foreignKeyTable.selectLast();
    }

    private void addCheck() {
        SQLiteCheckControl check = new SQLiteCheckControl();
        check.setName(DBUtil.genCheckName());
        check.setCreated(true);
        this.checkTable.addItem(check);
        this.checkTable.selectLast();
    }

    private void deleteForeignKey() {
        try {
            SQLiteForeignKeyControl foreignKey = this.foreignKeyTable.getSelectedItem();
            if (foreignKey == null) {
                return;
            }
            // 确认操作
            if (!foreignKey.isCreated() && !MessageBox.confirm(
                    ShellI18nHelper.deleteForeignKey(foreignKey.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.foreignKeyTable.removeItem(foreignKey);
            foreignKey.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void deleteCheck() {
        try {
            SQLiteCheckControl check = this.checkTable.getSelectedItem();
            if (check == null) {
                return;
            }
            // 确认操作
            if (!check.isCreated() && !MessageBox.confirm(
                    ShellI18nHelper.deleteCheck(check.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.checkTable.removeItem(check);
            check.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void deleteIndex() {
        try {
            SQLiteIndex index = this.indexTable.getSelectedItem();
            if (index == null) {
                return;
            }
            // 确认操作
            if (!index.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteIndex", index.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.indexTable.removeItem((SQLiteIndexControl) index);
            index.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void deleteTrigger() {
        try {
            SQLiteTrigger trigger = this.triggerTable.getSelectedItem();
            if (trigger == null) {
                return;
            }
            // 确认操作
            if (!trigger.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteTrigger", trigger.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.triggerTable.removeItem((SQLiteTriggerControl) trigger);
            trigger.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 保存db表
     */
    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    /**
     * 执行保存
     */
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
            if (this.newData) {
                this.tableName = MessageBox.prompt(I18nHelper.pleaseInputTableName(), this.tableName, this.getWindow());
                if (this.tableName == null) {
                    return;
                }
                this.tableName = this.tableName.trim();
                if (StringUtil.isBlank(this.tableName)) {
                    MessageBox.warn(I18nHelper.pleaseInputTableName(), this.getWindow());
                    return;
                }
            } else {
                this.tableName = this.table.getName();
            }
            String targetName = this.tableName;
            SQLiteTable target = this.buildTable(targetName);
            SQLiteSchemaManager manager = new SQLiteSchemaManager(this.client);
            if (this.newData) {
                manager.createTable(target);
            } else {
                this.dropChangedIndexes();
                this.dropChangedTriggers();
                manager.updateTable(previousName, target);
            }
            this.createChangedIndexes(previousName, targetName);
            this.createChangedTriggers(previousName, targetName);
            this.originalName = targetName;
            this.table = this.client.selectTable(targetName);
            if (this.newData) {
                this.dbItem.addTable(this.table);
            }
            this.unsaved = false;
            this.newData = false;
            ShellSqliteEventUtil.tableChanged(this.client, previousName, targetName);
            // 初始化信息
            FXUtil.runWait(this::initInfo);
            // 重置表格
            this.resetTable();
            // 初始化预览
            this.initPreview();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        } finally {
            this.flushTab();
        }
    }

    /**
     * 重置表单组件
     *
     * @throws Exception 异常
     */
    protected void resetTable() throws Exception {
        this.indexTable.reset();
        this.checkTable.reset();
        this.columnTable.reset();
        this.triggerTable.reset();
        this.foreignKeyTable.reset();
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

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("refreshData"), this.getWindow())) {
            return;
        }
        try {
            this.initTable();
            this.resetTable();
            this.init(this.table, this.dbItem);
            this.flushTab();
            this.initPreview();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void doMoveUp() {
        try {
            if (this.tabPane.isSelectedTab("columnTab")) {
                TableViewUtil.moveUp(this.columnTable);
            } else if (this.tabPane.isSelectedTab("indexTab")) {
                TableViewUtil.moveUp(this.indexTable);
            } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
                TableViewUtil.moveUp(this.foreignKeyTable);
            } else if (this.tabPane.isSelectedTab("triggerTab")) {
                TableViewUtil.moveUp(this.triggerTable);
            } else if (this.tabPane.isSelectedTab("checkTab")) {
                TableViewUtil.moveUp(this.checkTable);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void doMoveDown() {
        try {
            if (this.tabPane.isSelectedTab("columnTab")) {
                TableViewUtil.moveDown(this.columnTable);
            } else if (this.tabPane.isSelectedTab("indexTab")) {
                TableViewUtil.moveDown(this.indexTable);
            } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
                TableViewUtil.moveDown(this.foreignKeyTable);
            } else if (this.tabPane.isSelectedTab("triggerTab")) {
                TableViewUtil.moveDown(this.triggerTable);
            } else if (this.tabPane.isSelectedTab("checkTab")) {
                TableViewUtil.moveDown(this.checkTable);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
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
            this.preview.text(sql.toString());
        } catch (Exception ex) {
            this.preview.text("-- " + ex.getMessage());
        }
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.table == null ? null : this.table.getName();
    }

    private String designTableName() {
        return StringUtil.blankToDefault(this.tableName(), "Unnamed_Table");
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
