package cn.oyzh.easyshell.tabs.h2.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.check.H2CheckControl;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2ColumnControl;
import cn.oyzh.easyshell.h2.column.H2ColumnDefinitionParser;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKey;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKeyControl;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.table.H2TableCreateSqlGenerator;
import cn.oyzh.easyshell.h2.index.H2Index;
import cn.oyzh.easyshell.h2.index.H2IndexControl;
import cn.oyzh.easyshell.h2.schema.H2SchemaManager;
import cn.oyzh.easyshell.h2.schema.H2SchemaObject;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.trigger.H2Trigger;
import cn.oyzh.easyshell.h2.trigger.H2TriggerControl;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
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
 * H2 表设计器控制器。
 *
 * @since 2026-11-02
 */
public class ShellH2TableDesignTabController extends ParentTabController {

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 表字段组件
     */
    @FXML
    private DBStatusTableView<H2ColumnControl> columnTable;

    /**
     * 表索引组件
     */
    @FXML
    private DBStatusTableView<H2IndexControl> indexTable;

    /**
     * 表触发器组件
     */
    @FXML
    private DBStatusTableView<H2TriggerControl> triggerTable;

    /**
     * 表外键组件
     */
    @FXML
    private DBStatusTableView<H2ForeignKeyControl> foreignKeyTable;

    /**
     * 表检查器组件
     */
    @FXML
    private DBStatusTableView<H2CheckControl> checkTable;

    /**
     * 表约束
     */
    @FXML
    private FXTextArea constraints;

    /**
     * 表注释
     */
    @FXML
    private FXTextArea tableComment;

    /**
     * sql预览
     */
    @FXML
    private ShellDataEditor preview;

    /**
     * 额外信息
     */
    @FXML
    private ShellH2TableColumnExtraController tableColumnExtraController;

    /**
     * 客户端
     */
    private ShellH2Client client;

    /**
     * db库节点
     */
    private ShellH2RootTreeItem dbItem;

    /**
     * db表
     */
    private H2Table table;

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
    public void init(H2Table table, ShellH2RootTreeItem dbItem) {
        this.dbItem = dbItem;
        this.client = dbItem.client();
        this.table = table == null ? new H2Table() : table;
        if (StringUtil.isBlank(this.table.getDbName())) {
            this.table.setDbName(this.client.schema());
        }
        // 更新新数据标志位（刷新新建表时，表对象不为空但表名仍为空）
        this.newData = table == null || StringUtil.isBlank(table.getName());
        this.originalName = this.newData ? null : table.getName();
        this.tableName = this.originalName;
        this.constraints.setText(StringUtil.blankToDefault(this.table.getConstraints(), ""));
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        this.initDBListener();
        this.initInfo();
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
                this.table = this.client.selectTable(
                        this.table.getDbName(), this.table.getName());
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
        this.tableComment.clear();
    }

    /**
     * 初始化已有表信息
     */
    protected void initNormal() {
        if (this.table == null) {
            throw new IllegalArgumentException("H2 table does not exist: " + this.originalName);
        }
        NodeGroupUtil.disappear(this.getTab(), "action2");
        NodeGroupUtil.display(this.getTab(), "action3");
        this.tableComment.text(StringUtil.blankToDefault(this.table.getComment(), ""));

        // 基本信息
        String createSql = this.client.showCreate(
                this.table.getDbName(), this.table.getName());
        this.constraints.setText(StringUtil.blankToDefault(
                H2ColumnDefinitionParser.baseTableConstraints(createSql), ""));

        // 检查器
        this.checkTable.setItem(H2CheckControl.of(
                H2ColumnDefinitionParser.checksExcludingColumns(
                        createSql, this.table.getColumns())));
        // 索引
        this.indexTable.setItem(this.loadIndexes());
        // 字段
        this.columnTable.setItem(H2ColumnControl.of(this.table.getColumns()));
        // 触发器
        this.triggerTable.setItem(this.loadTriggers());
        // 外键
        this.foreignKeyTable.setItem(H2ForeignKeyControl.of(
                H2ColumnDefinitionParser.foreignKeys(createSql)));

        this.initIndexTable();
        this.initForeignKeyTable();
        this.columnTable.selectFirst();
        this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.client);
        // 重置保存标志位
        this.unsaved = false;
        this.flushTab();
    }

    private List<H2IndexControl> loadIndexes() {
        return H2IndexControl.of(this.loadSchemaObjects("index").stream()
                .map(H2Index::of)
                .toList());
    }

    private List<H2TriggerControl> loadTriggers() {
        return H2TriggerControl.of(this.loadSchemaObjects("trigger").stream()
                .map(H2Trigger::of)
                .toList());
    }

    private List<H2SchemaObject> loadSchemaObjects(String type) {
        return this.client.selectSchemaObjectsIn(this.table.getDbName(), type).stream()
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
            DBStatusListenerManager.bindListener(this.tableComment, this.listener);
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
        this.indexTable.itemList().addListener((ListChangeListener<H2IndexControl>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initIndexTable();
            }
        });
        this.initIndexTable();
        // 初始化外键列表
        this.foreignKeyTable.itemList().addListener((ListChangeListener<H2ForeignKeyControl>) c -> {
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
        NodeUtil.nodeOnCtrlS(this.getTab() == null ? null : this.getTab().getContent(), this::save);
        NodeUtil.nodeOnCtrlS(this.constraints, this::save);
        NodeUtil.nodeOnCtrlS(this.tableComment, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);

        // 更新字段列表
        this.columnTable.itemList().addListener((ListChangeListener<H2ColumnControl>) change -> {
            this.initIndexTable();
            this.initForeignKeyTable();
        });
    }

    /**
     * 初始化索引表格
     */
    private void initIndexTable() {
        for (H2IndexControl index : this.indexTable.getItems()) {
            index.setColumnList(this.columnTable.getItems());
        }
    }

    /**
     * 初始化外键表格
     */
    private void initForeignKeyTable() {
        for (H2ForeignKeyControl foreignKey : this.foreignKeyTable.getItems()) {
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
        H2ColumnControl column = new H2ColumnControl();
        column.setCreated(true);
        column.setType("TEXT");
        column.setNullable(true);
        this.columnTable.addItem(column);
        this.columnTable.selectLast();
    }

    private void deleteColumn() {
        try {
            H2Column column = this.columnTable.getSelectedItem();
            if (column == null) {
                return;
            }
            // 确认操作
            if (!column.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirmDeleteField(column.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.columnTable.removeItem((H2ColumnControl) column);
            column.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void addIndex() {
        H2IndexControl index = new H2IndexControl();
        index.setTableName(this.designTableName());
        index.setCreated(true);
        this.indexTable.addItem(index);
        this.initIndexTable();
        this.indexTable.selectLast();
    }

    private void addTrigger() {
        H2TriggerControl trigger = new H2TriggerControl();
        trigger.setTableName(this.designTableName());
        trigger.setCreated(true);
        this.triggerTable.addItem(trigger);
        this.triggerTable.selectLast();
    }

    private void addForeignKey() {
        H2ForeignKeyControl foreignKey = new H2ForeignKeyControl();
        foreignKey.setName(DBUtil.genForeignKeyName());
        foreignKey.setCreated(true);
        this.foreignKeyTable.addItem(foreignKey);
        this.initForeignKeyTable();
        this.foreignKeyTable.selectLast();
    }

    private void addCheck() {
        H2CheckControl check = new H2CheckControl();
        check.setName(DBUtil.genCheckName());
        check.setCreated(true);
        this.checkTable.addItem(check);
        this.checkTable.selectLast();
    }

    private void deleteForeignKey() {
        try {
            H2ForeignKeyControl foreignKey = this.foreignKeyTable.getSelectedItem();
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
            H2CheckControl check = this.checkTable.getSelectedItem();
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
            H2Index index = this.indexTable.getSelectedItem();
            if (index == null) {
                return;
            }
            // 确认操作
            if (!index.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirmDeleteIndex(index.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.indexTable.removeItem((H2IndexControl) index);
            index.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private void deleteTrigger() {
        try {
            H2Trigger trigger = this.triggerTable.getSelectedItem();
            if (trigger == null) {
                return;
            }
            // 确认操作
            if (!trigger.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirmDeleteTrigger(trigger.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.triggerTable.removeItem((H2TriggerControl) trigger);
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
            for (H2ColumnControl column : this.columnTable.getItems()) {
                if (column.isInvalid()) {
                    this.tabPane.selectTab("columnTab");
                    MessageBox.warn(I18nHelper.invalidColumn(), this.getWindow());
                    return;
                }
            }
            for (H2IndexControl index : this.indexTable.getItems()) {
                if (!index.isDeleted() && index.isInvalid()) {
                    this.tabPane.selectTab("indexTab");
                    MessageBox.warn(I18nHelper.invalidIndex(), this.getWindow());
                    return;
                }
            }
            for (H2TriggerControl trigger : this.triggerTable.getItems()) {
                if (!trigger.isDeleted() && trigger.isInvalid()) {
                    this.tabPane.selectTab("triggerTab");
                    MessageBox.warn(I18nHelper.invalidTrigger(), this.getWindow());
                    return;
                }
            }
            for (H2ForeignKeyControl foreignKey : this.foreignKeyTable.getItems()) {
                if (!foreignKey.isDeleted() && foreignKey.isInvalid()) {
                    this.tabPane.selectTab("foreignKeyTab");
                    MessageBox.warn(I18nHelper.invalidForeignKey(), this.getWindow());
                    return;
                }
            }
            for (H2CheckControl check : this.checkTable.getItems()) {
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
            H2Table target = this.buildTable(targetName);
            H2SchemaManager manager = new H2SchemaManager(this.client);
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
            ShellH2EventUtil.tableChanged(this.client, previousName, targetName);
            // 初始化信息
            this.initInfo();
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

    private H2Table buildTable(String name) {
        H2Table result = new H2Table();
        result.setName(name);
        result.setDbName(this.table.getDbName());
        result.setComment(this.tableComment.getTextTrim());
        List<H2Column> columns = new ArrayList<>();
        for (H2ColumnControl column : this.columnTable.getItems()) {
            if (!column.isInvalid()) {
                columns.add(column);
            }
        }
        result.setColumns(columns);
        List<cn.oyzh.easyshell.h2.check.H2Check> checks = new ArrayList<>();
        checks.addAll(this.checkTable.getItems());
        if (this.checkTable.getDeleteItems() != null) {
            checks.addAll(this.checkTable.getDeleteItems());
        }
        result.setChecks(checks);
        List<cn.oyzh.easyshell.h2.foreignKey.H2ForeignKey> foreignKeys = new ArrayList<>();
        foreignKeys.addAll(this.foreignKeyTable.getItems());
        if (this.foreignKeyTable.getDeleteItems() != null) {
            foreignKeys.addAll(this.foreignKeyTable.getDeleteItems());
        }
        result.setForeignKeys(foreignKeys);
        result.setConstraints(this.buildConstraints());
        return result;
    }

    private String buildConstraints() {
        List<String> definitions = new ArrayList<>();
        String base = this.constraints.getTextTrim();
        if (StringUtil.isNotBlank(base)) {
            definitions.add(base);
        }
        for (H2ForeignKeyControl foreignKey : this.foreignKeyTable.getItems()) {
            if (!foreignKey.isDeleted() && !foreignKey.isInvalid()) {
                definitions.add(foreignKey.toConstraintSql());
            }
        }
        for (H2CheckControl check : this.checkTable.getItems()) {
            if (!check.isDeleted() && !check.isInvalid()) {
                definitions.add(check.toConstraintSql());
            }
        }
        return String.join(",\n", definitions);
    }

    private void dropChangedIndexes() {
        if (this.indexTable.getDeleteItems() != null) {
            for (H2IndexControl index : this.indexTable.getDeleteItems()) {
                this.dropIndex(index);
            }
        }
        for (H2IndexControl index : this.indexTable.getItems()) {
            if (!index.isCreated() && index.isChangedDefinition()) {
                this.dropIndex(index);
            }
        }
    }

    private void dropChangedTriggers() {
        if (this.triggerTable.getDeleteItems() != null) {
            for (H2TriggerControl trigger : this.triggerTable.getDeleteItems()) {
                this.dropTrigger(trigger);
            }
        }
        for (H2TriggerControl trigger : this.triggerTable.getItems()) {
            if (!trigger.isCreated() && trigger.isChangedDefinition()) {
                this.dropTrigger(trigger);
            }
        }
    }

    private void dropIndex(H2Index index) {
        String name = StringUtil.blankToDefault(index.originalName(), index.getName());
        if (StringUtil.isNotBlank(name)) {
            this.client.executeSqlSimple("DROP INDEX IF EXISTS " + H2GeneratorUtil.quote(name));
        }
    }

    private void dropTrigger(H2Trigger trigger) {
        String name = StringUtil.blankToDefault(trigger.originalName(), trigger.getName());
        if (StringUtil.isNotBlank(name)) {
            this.client.executeSqlSimple("DROP TRIGGER IF EXISTS " + H2GeneratorUtil.quote(name));
        }
    }

    private void createChangedIndexes(String previousName, String targetName) {
        for (H2IndexControl index : this.indexTable.getItems()) {
            if (index.isDeleted() || StringUtil.isBlank(index.getDefinition())) {
                continue;
            }
            if (index.isCreated() || index.isChangedDefinition()) {
                this.client.executeSqlSimple(index.normalizedDefinition(previousName, targetName));
            }
        }
    }

    private void createChangedTriggers(String previousName, String targetName) {
        for (H2TriggerControl trigger : this.triggerTable.getItems()) {
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
        if (!MessageBox.confirm(ShellI18nHelper.confirmRefreshData(), this.getWindow())) {
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
            H2Table target = this.buildTable(name);
            StringBuilder sql = new StringBuilder(H2TableCreateSqlGenerator.createTable(name, target.getColumns(),
                    target.getConstraints()));
            for (String commentSql : H2TableCreateSqlGenerator.commentSql(
                    target.getDbName(), name, target.getComment(), target.getColumns())) {
                sql.append('\n').append(commentSql).append(';');
            }
            for (H2IndexControl index : this.indexTable.getItems()) {
                if (!index.isDeleted() && StringUtil.isNotBlank(index.getDefinition())) {
                    sql.append('\n').append(index.normalizedDefinition(this.originalName, name));
                }
            }
            for (H2TriggerControl trigger : this.triggerTable.getItems()) {
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

    /**
     * 获取表所属模式。
     *
     * @return 模式名称
     */
    public String schema() {
        return this.table == null
                ? (this.dbItem == null ? null : this.dbItem.client().schema())
                : this.table.getDbName();
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
    public ShellH2RootTreeItem getDbItem() {
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
