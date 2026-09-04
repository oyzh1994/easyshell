package cn.oyzh.easyshell.tabs.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.check.DamengCheck;
import cn.oyzh.easyshell.dameng.check.DamengCheckControl;
import cn.oyzh.easyshell.dameng.check.DamengChecks;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumnControl;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKeyControl;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKeys;
import cn.oyzh.easyshell.dameng.generator.table.DamengTableAlertSqlGenerator;
import cn.oyzh.easyshell.dameng.generator.table.DamengTableCreateSqlGenerator;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.index.DamengIndexControl;
import cn.oyzh.easyshell.dameng.index.DamengIndexes;
import cn.oyzh.easyshell.dameng.table.DamengAlertTableParam;
import cn.oyzh.easyshell.dameng.table.DamengCreateTableParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.dameng.trigger.DamengTriggerControl;
import cn.oyzh.easyshell.dameng.trigger.DamengTriggers;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.fx.dameng.table.DamengTableSpaceComboBox;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
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
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;

import java.util.List;

/**
 * db表设计业务
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellDamengTableDesignTabController extends ParentTabController {

    // /**
    //  * 新增按钮
    //  */
    // @FXML
    // private SVGGlyph add;
    //
    // /**
    //  * 删除按钮
    //  */
    // @FXML
    // private SVGGlyph delete;
    //
    // /**
    //  * 上移按钮
    //  */
    // @FXML
    // private SVGGlyph moveUp;
    //
    // /**
    //  * 下移按钮
    //  */
    // @FXML
    // private SVGGlyph moveDown;

    /**
     * 切换面板
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 表空间
     */
    @FXML
    private DamengTableSpaceComboBox tableSpace;

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
    private DBStatusTableView<DamengColumnControl> columnTable;

    /**
     * 表索引组件
     */
    @FXML
    private DBStatusTableView<DamengIndexControl> indexTable;

    /**
     * 表外键组件
     */
    @FXML
    private DBStatusTableView<DamengForeignKeyControl> foreignKeyTable;

    /**
     * db表
     */
    private DamengTable table;

    /**
     * 触发器组件
     */
    @FXML
    private DBStatusTableView<DamengTriggerControl> triggerTable;

    /**
     * 检查器组件
     */
    @FXML
    private DBStatusTableView<DamengCheckControl> checkTable;

    /**
     * db库节点
     */
    private ShellDamengSchemaTreeItem dbItem;

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
    private ShellDamengTableColumnExtraController tableColumnExtraController;

    private DamengCreateTableParam initCreateParam() {
        return (DamengCreateTableParam) this.initParam(true);
    }

    private DamengAlertTableParam initAlertParam() {
        return (DamengAlertTableParam) this.initParam(false);
    }

    /**
     * 初始化参数
     *
     * @param isCreate 是否新建
     * @return 结果
     */
    private Object initParam(boolean isCreate) {
        DamengTable tempTable = new DamengTable();
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
        DamengColumns columns = new DamengColumns();
        for (DamengColumn column : this.columnTable.getItems()) {
            if (!column.isInvalid()) {
                columns.add(column);
            }
        }
        if (CollectionUtil.isNotEmpty(this.columnTable.getDeleteItems())) {
            columns.addAll(this.columnTable.getDeleteItems());
        }

        // 索引处理
        DamengIndexes indexes = new DamengIndexes();
        for (DamengIndex index : this.indexTable.getItems()) {
            if (!index.isInvalid()) {
                indexes.add(index);
            }
        }
        if (CollectionUtil.isNotEmpty(this.indexTable.getDeleteItems())) {
            indexes.addAll(this.indexTable.getDeleteItems());
        }

        // 外键处理
        DamengForeignKeys foreignKeys = new DamengForeignKeys();
        for (DamengForeignKey foreignKey : this.foreignKeyTable.getItems()) {
            if (!foreignKey.isInvalid()) {
                foreignKeys.add(foreignKey);
            }
        }
        if (CollectionUtil.isNotEmpty(this.foreignKeyTable.getDeleteItems())) {
            foreignKeys.addAll(this.foreignKeyTable.getDeleteItems());
        }

        // 触发器处理
        DamengTriggers triggers = new DamengTriggers();
        for (DamengTrigger trigger : this.triggerTable.getItems()) {
            if (!trigger.isInvalid()) {
                triggers.add(trigger);
            }
        }
        if (CollectionUtil.isNotEmpty(this.triggerTable.getDeleteItems())) {
            triggers.addAll(this.triggerTable.getDeleteItems());
        }

        // 检查处理
        DamengChecks checks = null;
        if (this.dbItem.isSupportCheckFeature()) {
            checks = new DamengChecks();
            for (DamengCheck check : this.checkTable.getItems()) {
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
            this.init(this.table, this.dbItem);
            this.flushTab();
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
            for (DamengColumn column : this.columnTable.getItems()) {
                if (column.isInvalid()) {
                    this.tabPane.selectTab("columnTab");
                    MessageBox.warn(I18nHelper.invalidColumn());
                    return;
                }
            }

            // 索引检查
            for (DamengIndex index : this.indexTable.getItems()) {
                if (index.isInvalid()) {
                    this.tabPane.selectTab("indexTab");
                    MessageBox.warn(I18nHelper.invalidIndex());
                    return;
                }
            }

            // 外键检查
            for (DamengForeignKey foreignKey : this.foreignKeyTable.getItems()) {
                if (foreignKey.isInvalid()) {
                    this.tabPane.selectTab("foreignKeyTab");
                    MessageBox.warn(I18nHelper.invalidForeignKey());
                    return;
                }
            }

            // 触发器检查
            for (DamengTrigger trigger : this.triggerTable.getItems()) {
                if (trigger.isInvalid()) {
                    this.tabPane.selectTab("triggerTab");
                    MessageBox.warn(I18nHelper.invalidTrigger());
                    return;
                }
            }

            // 检查检查
            if (this.dbItem.isSupportCheckFeature()) {
                for (DamengCheck check : this.checkTable.getItems()) {
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
                DamengCreateTableParam param = this.initCreateParam();
                param.setTableName(this.tableName);
                if (param.getColumns() != null) {
                    for (DamengColumn column : param.getColumns()) {
                        column.setSchema(param.schema());
                        column.setTableName(param.tableName());
                    }
                }
                this.dbItem.createTable(param);
                this.table = this.dbItem.selectTable(tableName);
                this.dbItem.getTableTypeChild().addTable(table);
            } else {// 修改表
                DamengAlertTableParam param = this.initAlertParam();
                this.dbItem.alterTable(param);
                ShellDamengEventUtil.tableAlerted(tableName, this.dbItem);
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
        //        // 重载表数据
        //        this.tableEngine.select("innoDB");
    }

    /**
     * 初始化信息
     */
    protected void initNormal() {
        NodeGroupUtil.disappear(this.getTab(), "action2");
        NodeGroupUtil.display(this.getTab(), "action3");

        // 基本信息
        this.tableComment.text(this.table.getComment());
        this.tableSpace.select(this.table.getTableSpace());

        // 检查器
        if (this.dbItem.isSupportCheckFeature()) {
            this.checkTable.setItem(DamengCheckControl.of(this.dbItem.checks(this.tableName())));
        }
        // 索引
        this.indexTable.setItem(DamengIndexControl.of(this.dbItem.indexes(this.tableName())));
        // 字段
        this.columnTable.setItem(DamengColumnControl.of(this.dbItem.columns(this.tableName())));
        // 触发器
        this.triggerTable.setItem(DamengTriggerControl.of(this.dbItem.triggers(this.tableName())));
        // 外键
        this.foreignKeyTable.setItem(DamengForeignKeyControl.of(this.dbItem.foreignKeys(this.tableName())));
    }

    /**
     * 新增字段
     */
    private void addColumn() {
        DamengColumnControl column = new DamengColumnControl();
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
            DamengColumn column = this.columnTable.getSelectedItem();
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
        DamengIndexControl index = new DamengIndexControl();
        index.setCreated(true);
        this.indexTable.addItem(index);
        this.indexTable.selectLast();
    }

    /**
     * 删除索引
     */
    private void deleteIndex() {
        try {
            DamengIndex index = this.indexTable.getSelectedItem();
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
        DamengForeignKeyControl foreignKey = new DamengForeignKeyControl();
        foreignKey.setCreated(true);
        this.foreignKeyTable.addItem(foreignKey);
        this.foreignKeyTable.selectLast();
    }

    /**
     * 删除外键
     */
    private void deleteForeignKey() {
        try {
            DamengForeignKey foreignKey = this.foreignKeyTable.getSelectedItem();
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
        DamengTriggerControl trigger = new DamengTriggerControl();
        trigger.setCreated(true);
        this.triggerTable.addItem(trigger);
        this.triggerTable.selectLast();
    }

    /**
     * 删除触发器
     */
    private void deleteTrigger() {
        try {
            DamengTrigger trigger = this.triggerTable.getSelectedItem();
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
        DamengCheckControl check = new DamengCheckControl();
        check.setCreated(true);
        this.checkTable.addItem(check);
        this.checkTable.selectLast();
    }

    /**
     * 删除检查
     */
    private void deleteCheck() {
        try {
            DamengCheck check = this.checkTable.getSelectedItem();
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
        NodeUtil.nodeOnCtrlS(this.preview, this::save);

        // 更新字段列表
        this.columnTable.itemsProperty().get().addListener((ListChangeListener<DamengColumn>) c -> {
            //CacheHelper.set("dameng:columnList", this.columnTable.getItems());
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
                // this.add.display();
                NodeGroupUtil.display(this.getTab(), "action1");
                if (this.newData) {
                    // this.moveUp.display();
                    NodeGroupUtil.display(this.getTab(), "action2");
                }
            } else {
                // this.add.disappear();
                NodeGroupUtil.disappear(this.getTab(), "action1");
                if (this.newData) {
                    // this.moveUp.disappear();
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
        this.indexTable.itemList().addListener((ListChangeListener<DamengIndex>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initIndexTable();
            }
        });
        this.initIndexTable();
        // 初始化外键列表
        this.foreignKeyTable.itemList().addListener((ListChangeListener<DamengForeignKey>) c -> {
            while (c.next() && (c.wasAdded() || c.wasReplaced())) {
                this.initForeignKeyTable();
            }
        });
        this.initForeignKeyTable();

        this.initTable();
    }

    private void initIndexTable() {
        List list = this.columnTable.getItems();
        for (DamengIndexControl index : this.indexTable.itemList()) {
            index.setColumnList(list);
        }
    }

    private void initForeignKeyTable() {
        List list = this.columnTable.getItems();
        for (DamengForeignKeyControl foreignKey : this.foreignKeyTable.itemList()) {
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
            DamengCreateTableParam param = this.initCreateParam();
            if (param.tableName() == null) {
                param.setTableName(I18nHelper.unnamedTable());
            }
            if (param.getColumns() != null) {
                for (DamengColumn column : param.getColumns()) {
                    column.setSchema(param.schema());
                    column.setTableName(param.tableName());
                }
            }
            sql = DamengTableCreateSqlGenerator.generateSqlSingle(param);
        } else {
            DamengAlertTableParam param = this.initAlertParam();
            sql = DamengTableAlertSqlGenerator.generateSqlSingle(param);
        }
        this.preview.text(sql);
    }

    /**
     * 执行初始化
     *
     * @param table  表信息
     * @param dbItem db库树节点
     */
    public void init(DamengTable table, ShellDamengSchemaTreeItem dbItem) throws Exception {
        // 获取对象
        this.dbItem = dbItem;
        this.table = table;
        // 更新新数据标志位
        this.newData = table.isNew();
        this.table.setSchema(this.dbItem.schema());
        StageManager.showMask(this::doInit);
    }

    /**
     * 执行初始化
     */
    private void doInit() {
        // 初始化监听器
        this.initDBListener();

        // 初始化引擎
        this.tableSpace.init(this.dbItem.client());

        //// 设置缓存
        //CacheHelper.set("mysql:dbName", this.dbItem.dbName());
        //CacheHelper.set("mysql:dbClient", this.dbItem.client());

        // 初始化信息
        FXUtil.runWait(this::initInfo);
        //        this.initInfo();

        //        // 监听组件
        //        DBStatusListenerManager.bindListener(this.tableEngine, this.listener);
        //        DBStatusListenerManager.bindListener(this.tableCharset, this.listener);
        //        DBStatusListenerManager.bindListener(this.tableComment, this.listener);
        //        DBStatusListenerManager.bindListener(this.tableRowFormat, this.listener);
        //        DBStatusListenerManager.bindListener(this.tableCollation, this.listener);
        //        DBStatusListenerManager.bindListener(this.tableAutoIncrement, this.listener);

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

    public String tableName() {
        return this.table.getName();
    }

    public String schema() {
        return this.table.getSchema();
    }

    @Override
    public List<? extends SubTabController> getSubControllers() {
        return List.of(this.tableColumnExtraController);
    }

    public ShellDamengSchemaTreeItem getDbItem() {
        return dbItem;
    }

    // public void setDbItem(ShellMysqlDatabaseTreeItem dbItem) {
    //     this.dbItem = dbItem;
    // }

    public boolean isUnsaved() {
        return unsaved;
    }

    // public void setUnsaved(boolean unsaved) {
    //     this.unsaved = unsaved;
    // }

    //    @Override
    //    public void destroy() {
    //        this.sqlPreview.destroy();
    //        super.destroy();
    //    }
}
