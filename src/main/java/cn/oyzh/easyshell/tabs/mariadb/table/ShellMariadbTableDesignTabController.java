package cn.oyzh.easyshell.tabs.mariadb.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.fx.mariadb.ShellMariadbCharsetComboBox;
import cn.oyzh.easyshell.fx.mariadb.ShellMariadbCollationComboBox;
import cn.oyzh.easyshell.fx.mariadb.table.ShellMariadbEngineComboBox;
import cn.oyzh.easyshell.fx.mariadb.table.ShellMariadbRowFormatComboBox;
import cn.oyzh.easyshell.mariadb.check.MariadbCheck;
import cn.oyzh.easyshell.mariadb.check.MariadbCheckControl;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumnControl;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKey;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKeyControl;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableAlertSqlGenerator;
import cn.oyzh.easyshell.mariadb.generator.table.MariadbTableCreateSqlGenerator;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.index.MariadbIndexControl;
import cn.oyzh.easyshell.mariadb.table.MariadbAlertTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbCreateTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTriggerControl;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusTableView;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;

import java.util.List;
import java.util.Objects;

/**
 * MariaDB 表设计标签页控制器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableDesignTabController extends ParentTabController {

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 引擎
     */
    @FXML
    private ShellMariadbEngineComboBox tableEngine;

    /**
     * 字符集
     */
    @FXML
    private ShellMariadbCharsetComboBox tableCharset;

    /**
     * 排序方式
     */
    @FXML
    private ShellMariadbCollationComboBox tableCollation;

    /**
     * 行格式组件
     */
    @FXML
    private FXHBox tableRowFormatBox;

    /**
     * 行格式
     */
    @FXML
    private ShellMariadbRowFormatComboBox tableRowFormat;

    /**
     * 自动递增组件
     */
    @FXML
    private FXHBox tableAutoIncrementBox;

    /**
     * 自动递增
     */
    @FXML
    private NumberTextField tableAutoIncrement;

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
    private DBStatusTableView<MariadbColumnControl> columnTable;

    /**
     * 表索引组件
     */
    @FXML
    private DBStatusTableView<MariadbIndexControl> indexTable;

    /**
     * 表外键组件
     */
    @FXML
    private DBStatusTableView<MariadbForeignKeyControl> foreignKeyTable;

    /**
     * 数据库表
     */
    private MariadbTable table;

    /**
     * 触发器组件
     */
    @FXML
    private DBStatusTableView<MariadbTriggerControl> triggerTable;

    /**
     * 检查器组件
     */
    @FXML
    private DBStatusTableView<MariadbCheckControl> checkTable;

    /**
     * 数据库树节点
     */
    private ShellMariadbDatabaseTreeItem dbItem;

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
    private ShellMariadbTableColumnExtraController tableColumnExtraController;

    /**
     * 初始化建表参数
     *
     * @return 建表参数
     */
    private MariadbCreateTableParam initCreateParam() {
        return (MariadbCreateTableParam) this.initParam(true);
    }

    /**
     * 初始化改表参数
     *
     * @return 改表参数
     */
    private MariadbAlertTableParam initAlertParam() {
        return (MariadbAlertTableParam) this.initParam(false);
    }

    /**
     * 初始化参数
     *
     * @param isCreate 是否新建
     * @return 结果
     */
    private Object initParam(boolean isCreate) {
        MariadbTable tempTable = new MariadbTable();
        // 数据库
        tempTable.setDbName(this.table.getDbName());

        // 表名称
        tempTable.setName(this.table.getName());

        // 注释
        String comment = this.tableComment.getText();
        if (!StringUtil.equals(comment, this.table.getComment())) {
            tempTable.setComment(comment);
        }

        // 引擎
        String engine = this.tableEngine.getSelectedItem();
        if (!StringUtil.equalsIgnoreCase(engine, this.table.getEngine())) {
            tempTable.setEngine(engine);
        }

        // 字符集
        String charset = this.tableCharset.getSelectedItem();
        if (!StringUtil.equalsIgnoreCase(charset, this.table.getCharset())) {
            tempTable.setCharset(charset);
        }

        // 排序
        String collation = this.tableCollation.getSelectedItem();
        if (!StringUtil.equalsIgnoreCase(collation, this.table.getCollation())) {
            tempTable.setCollation(collation);
        }

        // 行格式
        if (this.tableRowFormatBox.isVisible()) {
            String rowFormat = this.tableRowFormat.getValue();
            if (!StringUtil.equalsIgnoreCase(rowFormat, this.table.getRowFormat())) {
                tempTable.setRowFormat(rowFormat);
            }
        }

        // 自动递增
        if (this.tableAutoIncrementBox.isVisible()) {
            Long autoIncrement = this.tableAutoIncrement.getValue();
            if (!Objects.equals(autoIncrement, this.table.getAutoIncrement())) {
                tempTable.setAutoIncrement(autoIncrement);
            }
        }

        // 字段处理
        MariadbColumns columns = new MariadbColumns();
        for (MariadbColumn column : this.columnTable.getItems()) {
            if (!column.isInvalid()) {
                columns.add(column);
            }
        }
        if (CollectionUtil.isNotEmpty(this.columnTable.getDeleteItems())) {
            columns.addAll(this.columnTable.getDeleteItems());
        }

        // 索引处理
        DBObjects<MariadbIndex> indexes = new DBObjects<MariadbIndex>();
        for (MariadbIndex index : this.indexTable.getItems()) {
            if (!index.isInvalid()) {
                indexes.add(index);
            }
        }
        if (CollectionUtil.isNotEmpty(this.indexTable.getDeleteItems())) {
            indexes.addAll(this.indexTable.getDeleteItems());
        }

        // 外键处理
        DBObjects<MariadbForeignKey> foreignKeys = new DBObjects<MariadbForeignKey>();
        for (MariadbForeignKey foreignKey : this.foreignKeyTable.getItems()) {
            if (!foreignKey.isInvalid()) {
                foreignKeys.add(foreignKey);
            }
        }
        if (CollectionUtil.isNotEmpty(this.foreignKeyTable.getDeleteItems())) {
            foreignKeys.addAll(this.foreignKeyTable.getDeleteItems());
        }

        // 触发器处理
        DBObjects<MariadbTrigger> triggers = new DBObjects<MariadbTrigger>();
        for (MariadbTrigger trigger : this.triggerTable.getItems()) {
            if (!trigger.isInvalid()) {
                triggers.add(trigger);
            }
        }
        if (CollectionUtil.isNotEmpty(this.triggerTable.getDeleteItems())) {
            triggers.addAll(this.triggerTable.getDeleteItems());
        }

        // 检查处理
        DBObjects<MariadbCheck> checks = null;
        if (this.dbItem.isSupportCheckFeature()) {
            checks = new DBObjects<>();
            for (MariadbCheck check : this.checkTable.getItems()) {
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
        if (!MessageBox.confirm(I18nHelper.refreshData() + "?")) {
            return;
        }
        try {
            this.initTable();
            this.resetTable();
            this.init(this.table, this.dbItem);
            this.flushTab();
            this.initPreview();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 保存db表
     */
    @FXML
    private void save() {
        StageManager.showMask(this::doSave);
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
            for (MariadbColumn column : this.columnTable.getItems()) {
                if (column.isInvalid()) {
                    this.tabPane.selectTab("columnTab");
                    MessageBox.warn(I18nHelper.invalidColumn());
                    return;
                }
            }

            // 索引检查
            for (MariadbIndex index : this.indexTable.getItems()) {
                if (index.isInvalid()) {
                    this.tabPane.selectTab("indexTab");
                    MessageBox.warn(I18nHelper.invalidIndex());
                    return;
                }
            }

            // 外键检查
            for (MariadbForeignKey foreignKey : this.foreignKeyTable.getItems()) {
                if (foreignKey.isInvalid()) {
                    this.tabPane.selectTab("foreignKeyTab");
                    MessageBox.warn(I18nHelper.invalidForeignKey());
                    return;
                }
            }

            // 触发器检查
            for (MariadbTrigger trigger : this.triggerTable.getItems()) {
                if (trigger.isInvalid()) {
                    this.tabPane.selectTab("triggerTab");
                    MessageBox.warn(I18nHelper.invalidTrigger());
                    return;
                }
            }

            // 检查检查
            if (this.dbItem.isSupportCheckFeature()) {
                for (MariadbCheck check : this.checkTable.getItems()) {
                    if (check.isInvalid()) {
                        this.tabPane.selectTab("checkTab");
                        MessageBox.warn(I18nHelper.invalidCheck());
                        return;
                    }
                }
            }

            if (this.newData) {
                this.tableName = MessageBox.prompt(I18nHelper.pleaseInputTableName(), this.tableName);
                if (this.tableName == null) {
                    return;
                }
            } else {
                this.tableName = this.table.getName();
            }

            // 创建表
            if (this.newData) {
                MariadbCreateTableParam param = this.initCreateParam();
                param.setTableName(tableName);
                if (param.getColumns() != null) {
                    for (MariadbColumn column : param.getColumns()) {
                        column.setDbName(param.dbName());
                        column.setTableName(param.tableName());
                    }
                }
                this.dbItem.createTable(param);
                this.table = this.dbItem.selectTable(tableName);
                this.dbItem.getTableTypeChild().addTable(table);
            } else {// 修改表
                MariadbAlertTableParam param = this.initAlertParam();
                this.dbItem.alterTable(param);
                ShellMariadbEventUtil.tableAlerted(this.tableName, this.dbItem);
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
            MessageBox.exception(ex);
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
     * 初始化信息
     */
    protected void initNew() {
        NodeGroupUtil.display(this.getTab(), "action2");
        NodeGroupUtil.disappear(this.getTab(), "action3");
        // 重载表数据
        this.tableEngine.select("innoDB");
        // 字符集
        if (this.tableCharset.isItemEmpty()) {
            this.tableCharset.init(this.dbItem.client());
        }
    }

    /**
     * 初始化信息
     */
    protected void initNormal() {
        NodeGroupUtil.disappear(this.getTab(), "action2");
        NodeGroupUtil.display(this.getTab(), "action3");

        // 基本信息
        this.tableEngine.select(this.table.getEngine());
        this.tableComment.text(this.table.getComment());
        // 字符集
        if (this.tableCharset.isItemEmpty()) {
            this.tableCharset.init(this.dbItem.client());
        }
        this.tableCharset.select(this.table.getCharset());
        // 排序规则
        this.tableCollation.init(this.table.getCharset(), this.dbItem.client());
        this.tableCollation.select(this.table.getCollation());

        // 检查器
        if (this.dbItem.isSupportCheckFeature()) {
            this.checkTable.setItem(MariadbCheckControl.of(this.dbItem.checks(this.tableName())));
        }
        // 索引
        this.indexTable.setItem(MariadbIndexControl.of(this.dbItem.indexes(this.tableName())));
        // 字段
        this.columnTable.setItem(MariadbColumnControl.of(this.dbItem.columns(this.tableName())));
        // 触发器
        this.triggerTable.setItem(MariadbTriggerControl.of(this.dbItem.triggers(this.tableName())));
        // 外键
        this.foreignKeyTable.setItem(MariadbForeignKeyControl.of(this.dbItem.foreignKeys(this.tableName())));

        // 行格式
        if (this.table.isInnoDB()) {
            this.tableRowFormatBox.display();
            this.tableRowFormat.select(this.table.getRowFormat());
        }

        // 表自动递增
        if (this.table.hasAutoIncrement()) {
            this.tableAutoIncrementBox.display();
            this.tableAutoIncrement.setValue(this.table.getAutoIncrement());
        }
    }

    /**
     * 新增字段
     */
    private void addColumn() {
        MariadbColumnControl column = new MariadbColumnControl();
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
            MariadbColumn column = this.columnTable.getSelectedItem();
            if (column == null) {
                return;
            }
            // 确认操作
            if (!column.isCreated() && !MessageBox.confirm(I18nHelper.deleteField() + " " + column.getName())) {
                return;
            }
            // 从table移除数据
            this.columnTable.removeItem(column);
            column.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增索引
     */
    private void addIndex() {
        MariadbIndexControl index = new MariadbIndexControl();
        index.setCreated(true);
        this.indexTable.addItem(index);
        this.indexTable.selectLast();
    }

    /**
     * 删除索引
     */
    private void deleteIndex() {
        try {
            MariadbIndex index = this.indexTable.getSelectedItem();
            if (index == null) {
                return;
            }
            // 确认操作
            if (!index.isCreated() && !MessageBox.confirm(I18nHelper.deleteIndex() + " " + index.getName())) {
                return;
            }
            // 从table移除数据
            this.indexTable.removeItem(index);
            index.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增外键
     */
    private void addForeignKey() {
        MariadbForeignKeyControl foreignKey = new MariadbForeignKeyControl();
        foreignKey.setCreated(true);
        this.foreignKeyTable.addItem(foreignKey);
        this.foreignKeyTable.selectLast();
    }

    /**
     * 删除外键
     */
    private void deleteForeignKey() {
        try {
            MariadbForeignKey foreignKey = this.foreignKeyTable.getSelectedItem();
            if (foreignKey == null) {
                return;
            }
            // 确认操作
            if (!foreignKey.isCreated() && !MessageBox.confirm(I18nHelper.deleteForeignKey() + " " + foreignKey.getName())) {
                return;
            }
            // 从table移除数据
            this.foreignKeyTable.removeItem(foreignKey);
            foreignKey.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增触发器
     */
    private void addTrigger() {
        MariadbTriggerControl trigger = new MariadbTriggerControl();
        trigger.setCreated(true);
        this.triggerTable.addItem(trigger);
        this.triggerTable.selectLast();
    }

    /**
     * 删除触发器
     */
    private void deleteTrigger() {
        try {
            MariadbTrigger trigger = this.triggerTable.getSelectedItem();
            if (trigger == null) {
                return;
            }
            // 确认操作
            if (!trigger.isCreated() && !MessageBox.confirm(I18nHelper.deleteTrigger() + " " + trigger.getName())) {
                return;
            }
            // 从table移除数据
            this.triggerTable.removeItem(trigger);
            trigger.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 新增检查
     */
    private void addCheck() {
        MariadbCheckControl check = new MariadbCheckControl();
        check.setCreated(true);
        this.checkTable.addItem(check);
        this.checkTable.selectLast();
    }

    /**
     * 删除检查
     */
    private void deleteCheck() {
        try {
            MariadbCheck check = this.checkTable.getSelectedItem();
            if (check == null) {
                return;
            }
            // 确认操作
            if (!check.isCreated() && !MessageBox.confirm(I18nHelper.deleteCheck() + " " + check.getName())) {
                return;
            }
            // 从table移除数据
            this.checkTable.removeItem(check);
            check.setDeleted(true);
        } catch (Exception ex) {
            MessageBox.exception(ex);
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
        NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
        NodeUtil.nodeOnCtrlS(this.tableComment, this::save);
        NodeUtil.nodeOnCtrlS(this.tableAutoIncrement, this::save);
        NodeUtil.nodeOnCtrlS((this.preview), this::save);

        // 更新字段列表
        this.columnTable.itemsProperty().get().addListener((ListChangeListener<MariadbColumn>) c -> {
            this.initIndexTable();
            this.initForeignKeyTable();
        });
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 字符集选中事件
        this.tableCharset.selectedItemChanged((observable, oldValue, newValue) -> {
            this.tableCollation.init(newValue, this.dbItem.client());
            this.tableCollation.selectFirst();
        });
        // 引擎选中事件
        this.tableEngine.selectedItemChanged((observable, oldValue, newValue) -> {
            if (this.tableEngine.isInnoDB()) {
                this.tableRowFormatBox.display();
                if (StringUtil.isBlank(this.table.getRowFormat())) {
                    this.tableRowFormat.select(this.table.getRowFormat());
                } else {
                    this.tableRowFormat.select(3);
                }
            } else {
                this.tableRowFormatBox.disappear();
            }
        });
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
        this.indexTable.itemList().addListener((ListChangeListener<MariadbIndex>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initIndexTable();
            }
        });
        this.initIndexTable();
        // 初始化外键列表
        this.foreignKeyTable.itemList().addListener((ListChangeListener<MariadbForeignKey>) c -> {
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
        for (MariadbIndexControl index : this.indexTable.itemList()) {
            index.setColumnList(list);
        }
    }

    /**
     * 初始化外键表格
     */
    private void initForeignKeyTable() {
        List list = this.columnTable.getItems();
        for (MariadbForeignKeyControl foreignKey : this.foreignKeyTable.itemList()) {
            foreignKey.setColumnList(list);
            foreignKey.setDbName(this.dbItem.dbName());
            foreignKey.setDbClient(this.dbItem.client());
        }
    }

    /**
     * 初始化预览
     */
    private void initPreview() {
        String sql;
        if (this.newData) {
            MariadbCreateTableParam param = this.initCreateParam();
            if (param.tableName() == null) {
                param.setTableName("Unnamed_Table");
            }
            if (param.getColumns() != null) {
                for (MariadbColumn column : param.getColumns()) {
                    column.setDbName(param.dbName());
                    column.setTableName(param.tableName());
                }
            }
            sql = MariadbTableCreateSqlGenerator.generateSqlSingle(param);
        } else {
            MariadbAlertTableParam param = this.initAlertParam();
            sql = MariadbTableAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 执行初始化
     *
     * @param table  表信息
     * @param dbItem 数据库树节点
     * @throws Exception 异常
     */
    public void init(MariadbTable table, ShellMariadbDatabaseTreeItem dbItem) throws Exception {
        // 获取对象
        this.dbItem = dbItem;
        this.table = table;
        // 更新新数据标志位
        this.newData = table.isNew();
        this.table.setDbName(this.dbItem.dbName());
        StageManager.showMask(this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化监听器
        this.initDBListener();

        // 初始化引擎
        this.tableEngine.init(this.dbItem.client());

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
            DBStatusListenerManager.bindListener(this.tableEngine, this.listener);
            DBStatusListenerManager.bindListener(this.tableCharset, this.listener);
            DBStatusListenerManager.bindListener(this.tableComment, this.listener);
            DBStatusListenerManager.bindListener(this.tableRowFormat, this.listener);
            DBStatusListenerManager.bindListener(this.tableCollation, this.listener);
            DBStatusListenerManager.bindListener(this.tableAutoIncrement, this.listener);
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
            MessageBox.exception(ex);
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
            MessageBox.exception(ex);
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
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.table.getDbName();
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
    public ShellMariadbDatabaseTreeItem getDbItem() {
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
