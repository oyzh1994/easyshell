package cn.oyzh.easyshell.tabs.oracle.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.check.OracleCheck;
import cn.oyzh.easyshell.oracle.check.OracleCheckControl;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumnControl;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKey;
import cn.oyzh.easyshell.oracle.foreignKey.OracleForeignKeyControl;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.oracle.index.OracleIndexControl;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.trigger.OracleTrigger;
import cn.oyzh.easyshell.oracle.trigger.OracleTriggerControl;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.fx.oracle.table.ShellOracleTableSpaceComboBox;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;

import java.util.List;

/**
 * db表设计业务
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleTableDesignTabController extends ParentTabController {

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 表空间
     */
    @FXML
    private ShellOracleTableSpaceComboBox tableSpace;

    /**
     * 注释
     */
    @FXML
    private FXTextArea tableComment;

    /**
     * sql预览
     */
    @FXML
    private Editor preview;

    /**
     * 表字段组件
     */
    @FXML
    private DBStatusTableView<OracleColumnControl> columnTable;

    /**
     * 表索引组件
     */
    @FXML
    private DBStatusTableView<OracleIndexControl> indexTable;

    /**
     * 表外键组件
     */
    @FXML
    private DBStatusTableView<OracleForeignKeyControl> foreignKeyTable;

    /**
     * db表
     */
    private OracleTable table;

    /**
     * 触发器组件
     */
    @FXML
    private DBStatusTableView<OracleTriggerControl> triggerTable;

    /**
     * 检查器组件
     */
    @FXML
    private DBStatusTableView<OracleCheckControl> checkTable;

    /**
     * db库节点
     */
    private ShellOracleSchemaTreeItem dbItem;

    /**
     * 数据监听器
     */
    private DBStatusListener listener;

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
     * 额外信息
     */
    @FXML
    private ShellOracleTableColumnExtraController tableColumnExtraController;

    /**
     * 初始化建表参数
     *
     * @return 建表参数
     */
    private OracleCreateTableParam initCreateParam() {
        return (OracleCreateTableParam) this.initParam(true);
    }

    /**
     * 初始化改表参数
     *
     * @return 改表参数
     */
    private OracleAlertTableParam initAlertParam() {
        return (OracleAlertTableParam) this.initParam(false);
    }

    /**
     * 初始化参数
     *
     * @param isCreate 是否新建
     * @return 结果
     */
    private Object initParam(boolean isCreate) {
        OracleTable tempTable = new OracleTable();
        // 数据库
        tempTable.setSchema(this.table.getSchema());

        // 表名称
        tempTable.setName(this.table.getName());

        // 注释
        String comment = this.tableComment.getText();
        if (!StringUtil.equals(comment, this.table.getComment())) {
            tempTable.setComment(comment);
        }

        // 表空间
        String tableSpace = this.tableSpace.getSelectedItem();
        if (!StringUtil.equalsIgnoreCase(tableSpace, this.table.getTableSpace())) {
            tempTable.setTableSpace(tableSpace);
        }

        // 字段处理
        OracleColumns columns = new OracleColumns();
        for (OracleColumn column : this.columnTable.getItems()) {
            if (!column.isInvalid()) {
                columns.add(column);
            }
        }
        if (CollectionUtil.isNotEmpty(this.columnTable.getDeleteItems())) {
            columns.addAll(this.columnTable.getDeleteItems());
        }

        // 索引处理
        DBObjects<OracleIndex> indexes = new DBObjects<>();
        for (OracleIndex index : this.indexTable.getItems()) {
            if (!index.isInvalid()) {
                indexes.add(index);
            }
        }
        if (CollectionUtil.isNotEmpty(this.indexTable.getDeleteItems())) {
            indexes.addAll(this.indexTable.getDeleteItems());
        }

        // 外键处理
        DBObjects<OracleForeignKey> foreignKeys = new DBObjects<>();
        for (OracleForeignKey foreignKey : this.foreignKeyTable.getItems()) {
            if (!foreignKey.isInvalid()) {
                foreignKeys.add(foreignKey);
            }
        }
        if (CollectionUtil.isNotEmpty(this.foreignKeyTable.getDeleteItems())) {
            foreignKeys.addAll(this.foreignKeyTable.getDeleteItems());
        }

        // 触发器处理
        DBObjects<OracleTrigger> triggers = new DBObjects<>();
        for (OracleTrigger trigger : this.triggerTable.getItems()) {
            if (!trigger.isInvalid()) {
                triggers.add(trigger);
            }
        }
        if (CollectionUtil.isNotEmpty(this.triggerTable.getDeleteItems())) {
            triggers.addAll(this.triggerTable.getDeleteItems());
        }

        // 检查处理
        DBObjects<OracleCheck> checks = null;
        if (this.dbItem.isSupportCheckFeature()) {
            checks = new DBObjects<>();
            for (OracleCheck check : this.checkTable.getItems()) {
                if (!check.isInvalid()) {
                    checks.add(check);
                }
            }
            if (CollectionUtil.isNotEmpty(this.checkTable.getDeleteItems())) {
                checks.addAll(this.checkTable.getDeleteItems());
            }
        }
        if (isCreate) {
            return this.dbItem.createTableParam(tempTable, columns, indexes, foreignKeys, triggers, checks);
        }
        return this.dbItem.alterTableParam(tempTable, columns, indexes, foreignKeys, triggers, checks);
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

    /**
     * 保存db表
     */
    @FXML
    private void save() {
        StageManager.showMask(this.getWindow(), this::doSave);
    }

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 执行保存
     */
    private void doSave() {
        try {
            // 字段检查
            for (OracleColumn column : this.columnTable.getItems()) {
                if (column.isInvalid()) {
                    this.tabPane.selectTab("columnTab");
                    MessageBox.warn(I18nHelper.invalidColumn(), this.getWindow());
                    return;
                }
            }

            // 索引检查
            for (OracleIndex index : this.indexTable.getItems()) {
                if (index.isInvalid()) {
                    this.tabPane.selectTab("indexTab");
                    MessageBox.warn(I18nHelper.invalidIndex(), this.getWindow());
                    return;
                }
            }

            // 外键检查
            for (OracleForeignKey foreignKey : this.foreignKeyTable.getItems()) {
                if (foreignKey.isInvalid()) {
                    this.tabPane.selectTab("foreignKeyTab");
                    MessageBox.warn(I18nHelper.invalidForeignKey(), this.getWindow());
                    return;
                }
            }

            // 触发器检查
            for (OracleTrigger trigger : this.triggerTable.getItems()) {
                if (trigger.isInvalid()) {
                    this.tabPane.selectTab("triggerTab");
                    MessageBox.warn(I18nHelper.invalidTrigger(), this.getWindow());
                    return;
                }
            }

            // 检查检查
            if (this.dbItem.isSupportCheckFeature()) {
                for (OracleCheck check : this.checkTable.getItems()) {
                    if (check.isInvalid()) {
                        this.tabPane.selectTab("checkTab");
                        MessageBox.warn(I18nHelper.invalidCheck(), this.getWindow());
                        return;
                    }
                }
            }

            if (this.newData) {
                this.tableName = MessageBox.prompt(I18nHelper.pleaseInputTableName(), this.tableName, this.getWindow());
                if (this.tableName == null) {
                    return;
                }
            } else {
                this.tableName = this.table.getName();
            }

            // 创建表
            if (this.newData) {
                OracleCreateTableParam param = this.initCreateParam();
                param.setTableName(this.tableName);
                if (param.getColumns() != null) {
                    for (OracleColumn column : param.getColumns()) {
                        column.setSchema(param.schema());
                        column.setTableName(param.tableName());
                    }
                }
                this.dbItem.createTable(param);
                this.table = this.dbItem.selectTable(tableName);
                this.dbItem.getTableTypeChild().addTable(table);
            } else {// 修改表
                OracleAlertTableParam param = this.initAlertParam();
                this.dbItem.alterTable(param);
                ShellOracleEventUtil.tableAlerted(this.tableName, this.dbItem);
            }
            // 重置保存标志位
            this.unsaved = false;
            // 更新新数据标志位
            this.newData = false;
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
     * 初始化变更标志
     */
    private void initChangedFlag() {
        if (!this.initiating) {
            this.unsaved = true;
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

    /**
     * 初始化信息
     */
    protected void initInfo() {
        // 更新初始化标志位
        this.initiating = true;
        // 新数据
        if (this.newData) {
            this.unsaved = true;
            this.initNew();
        } else {// 已有数据
            this.table = this.dbItem.selectTable(this.table.getName());
            this.initNormal();
        }
        // 标记为结束
        FXUtil.runPulse(() -> this.initiating = false);
    }

    /**
     * 初始化新建表信息
     */
    protected void initNew() {
        NodeGroupUtil.display(this.getTab(), "action2");
        NodeGroupUtil.disappear(this.getTab(), "action3");
    }

    /**
     * 初始化已有表信息
     */
    protected void initNormal() {
        NodeGroupUtil.disappear(this.getTab(), "action2");
        NodeGroupUtil.display(this.getTab(), "action3");

        // 基本信息
        this.tableSpace.select(this.table.getTableSpace());
        this.tableComment.text(this.table.getComment());

        // 检查器
        if (this.dbItem.isSupportCheckFeature()) {
            this.checkTable.setItem(OracleCheckControl.of(this.dbItem.checks(this.tableName())));
        }
        // 索引
        this.indexTable.setItem(OracleIndexControl.of(this.dbItem.indexes(this.tableName())));
        // 字段
        this.columnTable.setItem(OracleColumnControl.of(this.dbItem.columns(this.tableName())));
        // 触发器
        this.triggerTable.setItem(OracleTriggerControl.of(this.dbItem.triggers(this.tableName())));
        // 外键
        this.foreignKeyTable.setItem(OracleForeignKeyControl.of(this.dbItem.foreignKeys(this.tableName())));
    }

    /**
     * 新增字段
     */
    private void addColumn() {
        OracleColumnControl column = new OracleColumnControl();
        column.setCreated(true);
        column.setNullable(true);
        this.columnTable.addItem(column);
        this.columnTable.selectLast();
    }

    /**
     * 删除字段
     */
    private void deleteColumn() {
        try {
            OracleColumn column = this.columnTable.getSelectedItem();
            if (column == null) {
                return;
            }
            // 确认操作
            if (!column.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteField", column.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.columnTable.removeItem(column);
            column.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 新增索引
     */
    private void addIndex() {
        OracleIndexControl index = new OracleIndexControl();
        index.setCreated(true);
        this.indexTable.addItem(index);
        this.indexTable.selectLast();
    }

    /**
     * 删除索引
     */
    private void deleteIndex() {
        try {
            OracleIndex index = this.indexTable.getSelectedItem();
            if (index == null) {
                return;
            }
            // 确认操作
            if (!index.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteIndex", index.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.indexTable.removeItem(index);
            index.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 新增外键
     */
    private void addForeignKey() {
        OracleForeignKeyControl foreignKey = new OracleForeignKeyControl();
        foreignKey.setCreated(true);
        this.foreignKeyTable.addItem(foreignKey);
        this.foreignKeyTable.selectLast();
    }

    /**
     * 删除外键
     */
    private void deleteForeignKey() {
        try {
            OracleForeignKey foreignKey = this.foreignKeyTable.getSelectedItem();
            if (foreignKey == null) {
                return;
            }
            // 确认操作
            if (!foreignKey.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteForeignKey", foreignKey.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.foreignKeyTable.removeItem(foreignKey);
            foreignKey.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 新增触发器
     */
    private void addTrigger() {
        OracleTriggerControl trigger = new OracleTriggerControl();
        trigger.setCreated(true);
        this.triggerTable.addItem(trigger);
        this.triggerTable.selectLast();
    }

    /**
     * 删除触发器
     */
    private void deleteTrigger() {
        try {
            OracleTrigger trigger = this.triggerTable.getSelectedItem();
            if (trigger == null) {
                return;
            }
            // 确认操作
            if (!trigger.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteTrigger", trigger.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.triggerTable.removeItem(trigger);
            trigger.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 新增检查
     */
    private void addCheck() {
        OracleCheckControl check = new OracleCheckControl();
        check.setCreated(true);
        this.checkTable.addItem(check);
        this.checkTable.selectLast();
    }

    /**
     * 删除检查
     */
    private void deleteCheck() {
        try {
            OracleCheck check = this.checkTable.getSelectedItem();
            if (check == null) {
                return;
            }
            // 确认操作
            if (!check.isCreated() && !MessageBox.confirm(ShellI18nHelper.confirm("deleteCheck", check.getName()), this.getWindow())) {
                return;
            }
            // 从table移除数据
            this.checkTable.removeItem(check);
            check.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }


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
        NodeUtil.nodeOnCtrlS(this.tableComment, this::save);
        NodeUtil.nodeOnCtrlS(this.preview, this::save);

        // 更新字段列表
        this.columnTable.itemsProperty().get().addListener((ListChangeListener<OracleColumn>) c -> {
            this.initIndexTable();
            this.initForeignKeyTable();
        });
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 表格下标监听
        this.tabPane.selectedItemChanged((observable, oldValue, newValue) -> {
            String tabId = newValue == null ? null : newValue.getId();
            if (StringUtil.equalsAny(tabId, "columnTab", "indexTab", "foreignKeyTab", "triggerTab", "checkTab")) {
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
            // 预览
            if (StringUtil.equals(tabId, "previewTab")) {
                this.initPreview();
            }
        });

        this.columnTable.selectedIndexChanged((observable, oldValue, newValue) -> this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.dbItem.client()));

        // 初始化索引列表
        this.indexTable.itemList().addListener((ListChangeListener<OracleIndex>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initIndexTable();
            }
        });
        this.initIndexTable();
        // 初始化外键列表
        this.foreignKeyTable.itemList().addListener((ListChangeListener<OracleForeignKey>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initForeignKeyTable();
            }
        });
        this.initForeignKeyTable();

        this.initTable();
    }

    /**
     * 初始化索引表格
     */
    private void initIndexTable() {
        List list = this.columnTable.getItems();
        for (OracleIndexControl index : this.indexTable.itemList()) {
            index.setColumnList(list);
        }
    }

    /**
     * 初始化外键表格
     */
    private void initForeignKeyTable() {
        List list = this.columnTable.getItems();
        for (OracleForeignKeyControl foreignKey : this.foreignKeyTable.itemList()) {
            foreignKey.setColumnList(list);
            foreignKey.setSchema(this.dbItem.schema());
            foreignKey.setDbClient(this.dbItem.client());
        }
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        String sql;
        if (this.newData) {
            OracleCreateTableParam param = this.initCreateParam();
            if (param.tableName() == null) {
                param.setTableName(I18nHelper.unnamedTable());
            }
            if (param.getColumns() != null) {
                for (OracleColumn column : param.getColumns()) {
                    column.setSchema(param.schema());
                    column.setTableName(param.tableName());
                }
            }
            sql = OracleTableCreateSqlGenerator.generateSqlSingle(param);
        } else {
            OracleAlertTableParam param = this.initAlertParam();
            sql = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 执行初始化
     *
     * @param table  表信息
     * @param dbItem db库树节点
     * @throws Exception 异常
     */
    public void init(OracleTable table, ShellOracleSchemaTreeItem dbItem) throws Exception {
        // 获取对象
        this.dbItem = dbItem;
        this.table = table;
        // 更新新数据标志位
        this.newData = table.isNew();
        this.table.setSchema(this.dbItem.schema());
        StageManager.showMask(this.getWindow(), this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化监听器
        this.initDBListener();

        // 初始化引擎
        this.tableSpace.init(this.dbItem.client());

        // 初始化信息
        FXUtil.runWait(this::initInfo);

        // 移除tab
        if (!this.dbItem.isSupportCheckFeature()) {
            this.tabPane.removeTab("checkTab");
        }
    }

    /**
     * 初始化数据监听器
     */
    private void initDBListener() {
        if (this.listener == null) {
            // 初始化监听器
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
            DBStatusListenerManager.bindListener(this.tableSpace, this.listener);
            DBStatusListenerManager.bindListener(this.tableComment, this.listener);
        }
    }

    /**
     * 执行添加
     */
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

    /**
     * 执行删除
     */
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

    /**
     * 执行上移
     */
    @FXML
    private void doMoveUp() {
        try {
            if (this.tabPane.isSelectedTab("columnTab")) {
                // this.moveColumnUp();
                TableViewUtil.moveUp(this.columnTable);
            } else if (this.tabPane.isSelectedTab("indexTab")) {
                // this.moveIndexUp();
                TableViewUtil.moveUp(this.indexTable);
            } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
                // this.moveForeignKeyUp();
                TableViewUtil.moveUp(this.foreignKeyTable);
            } else if (this.tabPane.isSelectedTab("triggerTab")) {
                // this.moveTriggerUp();
                TableViewUtil.moveUp(this.triggerTable);
            } else if (this.tabPane.isSelectedTab("checkTab")) {
                // this.moveCheckUp();
                TableViewUtil.moveUp(this.checkTable);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 执行下移
     */
    @FXML
    private void doMoveDown() {
        try {
            if (this.tabPane.isSelectedTab("columnTab")) {
                // this.moveColumnDown();
                TableViewUtil.moveDown(this.columnTable);
            } else if (this.tabPane.isSelectedTab("indexTab")) {
                // this.moveIndexDown();
                TableViewUtil.moveDown(this.indexTable);
            } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
                // this.moveForeignKeyDown();
                TableViewUtil.moveDown(this.foreignKeyTable);
            } else if (this.tabPane.isSelectedTab("triggerTab")) {
                // this.moveTriggerDown();
                TableViewUtil.moveDown(this.triggerTable);
            } else if (this.tabPane.isSelectedTab("checkTab")) {
                // this.moveCheckDown();
                TableViewUtil.moveDown(this.checkTable);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.table.getName();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.table.getSchema();
    }

    @Override
    public List<? extends SubTabController> getSubControllers() {
        return List.of(this.tableColumnExtraController);
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellOracleSchemaTreeItem getDbItem() {
        return dbItem;
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return unsaved;
    }
}
