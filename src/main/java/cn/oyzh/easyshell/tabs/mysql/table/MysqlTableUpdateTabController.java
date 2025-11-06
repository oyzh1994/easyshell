package cn.oyzh.easyshell.tabs.mysql.table;// package cn.oyzh.easymysql.tabs.table;
//
// import cn.hutool.core.collection.CollUtil;
// import cn.hutool.core.util.StrUtil;
// import cn.oyzh.common.cache.CacheHelper;
// import cn.oyzh.easymysql.db.check.MysqlCheck;
// import cn.oyzh.easymysql.db.check.MysqlCheckControl;
// import cn.oyzh.easymysql.db.check.MysqlChecks;
// import cn.oyzh.easymysql.db.column.MysqlColumn;
// import cn.oyzh.easymysql.db.column.MysqlColumnControl;
// import cn.oyzh.easymysql.db.column.MysqlColumns;
// import cn.oyzh.easymysql.db.foreignKey.MysqlForeignKey;
// import cn.oyzh.easymysql.db.foreignKey.MysqlForeignKeyControl;
// import cn.oyzh.easymysql.db.foreignKey.MysqlForeignKeys;
// import cn.oyzh.easymysql.db.index.MysqlIndex;
// import cn.oyzh.easymysql.db.index.MysqlIndexControl;
// import cn.oyzh.easymysql.db.index.MysqlIndexes;
// import cn.oyzh.easymysql.db.table.MysqlTable;
// import cn.oyzh.easymysql.db.table.MysqlAlertTableParam;
// import cn.oyzh.easymysql.db.table.MysqlCreateTableParam;
// import cn.oyzh.easymysql.db.trigger.MysqlTrigger;
// import cn.oyzh.easymysql.db.trigger.MysqlTriggerControl;
// import cn.oyzh.easymysql.db.trigger.MysqlTriggers;
// import cn.oyzh.easyshell.mysql.event.MysqlEventUtil;
// import cn.oyzh.easyshell.fx.mysql.DBCharsetComboBox;
// import cn.oyzh.easyshell.fx.mysql.DBCollationComboBox;
// import cn.oyzh.easyshell.fx.mysql.DBStatusColumn;
// import cn.oyzh.easyshell.fx.mysql.DBStatusTableView;
// import cn.oyzh.easyshell.fx.mysql.table.MysqlEngineComboBox;
// import cn.oyzh.easyshell.fx.mysql.table.MysqlRowFormatComboBox;
// import cn.oyzh.easymysql.generator.table.MysqlTableAlertSqlGenerator;
// import cn.oyzh.easymysql.generator.table.MysqlTableCreateSqlGenerator;
// import cn.oyzh.easyshell.mysql.listener.DBStatusListener;
// import cn.oyzh.easyshell.mysql.listener.DBStatusListenerManager;
// import cn.oyzh.easymysql.trees.database.MysqlDatabaseTreeItem;
// import cn.oyzh.easymysql.trees.table.MysqlTableTreeItem;
// import cn.oyzh.fx.gui.tabs.ParentTabController;
// import cn.oyzh.fx.gui.tabs.SubTabController;
// import cn.oyzh.fx.gui.text.field.NumberTextField;
// import cn.oyzh.fx.plus.controls.box.FXHBox;
// import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
// import cn.oyzh.fx.plus.controls.tab.FXTabPane;
// import cn.oyzh.fx.plus.controls.table.FXTableColumn;
// import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
// import cn.oyzh.fx.plus.information.MessageBox;
// import cn.oyzh.fx.plus.node.NodeUtil;
// import cn.oyzh.fx.plus.util.FXUtil;
// import cn.oyzh.fx.plus.tableview.TableViewUtil;
// import cn.oyzh.i18n.I18nHelper;
// import javafx.beans.value.ObservableValue;
// import javafx.collections.ListChangeListener;
// import javafx.event.Event;
// import javafx.fxml.FXML;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.cell.PropertyValueFactory;
//
// import java.net.URL;
// import java.util.List;
// import java.util.Objects;
// import java.util.ResourceBundle;
//
// /**
//  * db表设计业务
//  *
//  * @author oyzh
//  * @since 2024/08/07
//  */
// public class MysqlTableUpdateTabController extends ParentTabController {
//
//     /**
//      * 新增按钮
//      */
//     @FXML
//     private SVGGlyph add;
//
//     /**
//      * 删除按钮
//      */
//     @FXML
//     private SVGGlyph delete;
//
//     /**
//      * 上移按钮
//      */
//     @FXML
//     private SVGGlyph moveUp;
//
//     /**
//      * 下移按钮
//      */
//     @FXML
//     private SVGGlyph moveDown;
//
//     /**
//      * 切换面板
//      */
//     @FXML
//     private FXTabPane tabPane;
//
//     /**
//      * 引擎
//      */
//     @FXML
//     private MysqlEngineComboBox tableEngine;
//
//     /**
//      * 字符集
//      */
//     @FXML
//     private DBCharsetComboBox tableCharset;
//
//     /**
//      * 排序方式
//      */
//     @FXML
//     private DBCollationComboBox tableCollation;
//
//     /**
//      * 行格式组件
//      */
//     @FXML
//     private FXHBox tableRowFormatBox;
//
//     /**
//      * 行格式
//      */
//     @FXML
//     private MysqlRowFormatComboBox tableRowFormat;
//
//     /**
//      * 自动递增组件
//      */
//     @FXML
//     private FXHBox tableAutoIncrementBox;
//
//     /**
//      * 自动递增
//      */
//     @FXML
//     private NumberTextField tableAutoIncrement;
//
//     /**
//      * 注释
//      */
//     @FXML
//     private FXTextArea tableComment;
//
//     /**
//      * sql预览
//      */
//     @FXML
//     private FXTextArea sqlPreview;
//
//     /**
//      * 表字段组件
//      */
//     @FXML
//     private DBStatusTableView<MysqlColumnControl> columnTable;
//
//     /**
//      * 字段状态列
//      */
//     @FXML
//     private DBStatusColumn<MysqlColumnControl> colStatus;
//
//     /**
//      * 字段名称列
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, String> colName;
//
//     /**
//      * 字段类型列
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, String> colType;
//
//     /**
//      * 字段长度
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, Integer> colSize;
//
//     /**
//      * 字段小数点列
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, Integer> colDigits;
//
//     /**
//      * 字段是否主键列
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, Boolean> colPrimaryKey;
//
//     /**
//      * 字段可为null列
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, Boolean> colNullable;
//
//     /**
//      * 字段注释列
//      */
//     @FXML
//     private FXTableColumn<MysqlColumnControl, String> colComment;
//
//     /**
//      * 表索引组件
//      */
//     @FXML
//     private DBStatusTableView<MysqlIndexControl> indexTable;
//
//     /**
//      * 索引状态列
//      */
//     @FXML
//     private DBStatusColumn<MysqlIndexControl> indexStatus;
//
//     /**
//      * 索引名称列
//      */
//     @FXML
//     private TableColumn<MysqlIndexControl, String> indexName;
//
//     /**
//      * 索引字段列
//      */
//     @FXML
//     private TableColumn<MysqlIndexControl, String> indexColumn;
//
//     /**
//      * 索引类型列
//      */
//     @FXML
//     private TableColumn<MysqlIndexControl, String> indexType;
//
//     /**
//      * 索引方法列
//      */
//     @FXML
//     private TableColumn<MysqlIndexControl, String> indexMethod;
//
//     /**
//      * 索引注释列
//      */
//     @FXML
//     private FXTableColumn<MysqlIndexControl, String> indexComment;
//
//     /**
//      * 表外键组件
//      */
//     @FXML
//     private DBStatusTableView<MysqlForeignKeyControl> foreignKeyTable;
//
//     /**
//      * 外键状态列
//      */
//     @FXML
//     private DBStatusColumn<MysqlForeignKeyControl> foreignKeyStatus;
//
//     /**
//      * 外键名称列
//      */
//     @FXML
//     private TableColumn<MysqlForeignKeyControl, String> foreignKeyName;
//
//     /**
//      * 外键字段列
//      */
//     @FXML
//     private TableColumn<MysqlForeignKeyControl, String> foreignKeyColumn;
//
//     /**
//      * 外键引用库
//      */
//     @FXML
//     private TableColumn<MysqlForeignKeyControl, String> foreignKeyPKDatabase;
//
//     /**
//      * 外键引用表
//      */
//     @FXML
//     private TableColumn<MysqlForeignKeyControl, String> foreignKeyPKTable;
//
//     /**
//      * 外键引用字段
//      */
//     @FXML
//     private FXTableColumn<MysqlForeignKeyControl, String> foreignKeyPKColumn;
//
//     /**
//      * 外键删除策略
//      */
//     @FXML
//     private FXTableColumn<MysqlForeignKeyControl, String> foreignKeyDeletePolicy;
//
//     /**
//      * 外键更新策略
//      */
//     @FXML
//     private FXTableColumn<MysqlForeignKeyControl, String> foreignKeyUpdatePolicy;
//
//     /**
//      * db表
//      */
//     private MysqlTable mysqlTable;
//
//     /**
//      * 触发器组件
//      */
//     @FXML
//     private DBStatusTableView<MysqlTriggerControl> triggerTable;
//
//     /**
//      * 触发器状态
//      */
//     @FXML
//     private DBStatusColumn<MysqlTriggerControl> triggerStatus;
//
//     /**
//      * 触发器名称
//      */
//     @FXML
//     private FXTableColumn<MysqlTriggerControl, String> triggerName;
//
//     /**
//      * 触发器策略
//      */
//     @FXML
//     private FXTableColumn<MysqlTriggerControl, String> triggerPolicy;
//
//     /**
//      * 触发器定义
//      */
//     @FXML
//     private FXTableColumn<MysqlTriggerControl, String> triggerDefinition;
//
//     /**
//      * 检查器组件
//      */
//     @FXML
//     private DBStatusTableView<MysqlCheckControl> checkTable;
//
//     /**
//      * 检查器状态
//      */
//     @FXML
//     private DBStatusColumn<MysqlCheckControl> checkStatus;
//
//     /**
//      * 检查器名称
//      */
//     @FXML
//     private FXTableColumn<MysqlCheckControl, String> checkName;
//
//     /**
//      * 检查器子语句
//      */
//     @FXML
//     private FXTableColumn<MysqlCheckControl, String> checkClause;
//
//     /**
//      * db库节点
//      */
//     private MysqlDatabaseTreeItem dbItem;
//
//     /**
//      * db库节点
//      */
//     private MysqlTableTreeItem tableItem;
//
//     /**
//      * 数据监听器
//      */
//     private DBStatusListener listener;
//
//     /**
//      * 未保存标志位
//      */
//     private boolean unsaved;
//
//     /**
//      * 新数据标志位
//      */
//     private boolean newData;
//
//     /**
//      * 初始化中标志位
//      */
//     private boolean initiating;
//
//     @FXML
//     private MysqlTableColumnExtraController tableColumnExtraController;
//
//     private MysqlCreateTableParam initCreateParam() {
//         return (MysqlCreateTableParam) this.initParam((byte) 1);
//     }
//
//     private MysqlAlertTableParam initAlertParam() {
//         return (MysqlAlertTableParam) this.initParam((byte) 2);
//     }
//
//     private Object initParam(byte type) {
//         MysqlTable tempTable = new MysqlTable();
//         // 数据库
//         tempTable.setDbName(this.mysqlTable.getDbName());
//
//         // 表名称
//         if (!this.newData) {
//             tempTable.setName(this.mysqlTable.getName());
//         }
//
//         // 注释
//         String comment = this.tableComment.getText();
//         if (!StrUtil.equals(comment, this.mysqlTable.getComment())) {
//             tempTable.setComment(comment);
//         }
//
//         // 引擎
//         String engine = this.tableEngine.getSelectedItem();
//         if (!StrUtil.equalsIgnoreCase(engine, this.mysqlTable.getEngine())) {
//             tempTable.setEngine(engine);
//         }
//
//         // 字符集
//         String charset = this.tableCharset.getSelectedItem();
//         if (!StrUtil.equalsIgnoreCase(charset, this.mysqlTable.getCharset())) {
//             tempTable.setCharset(charset);
//         }
//
//         // 排序
//         String collation = this.tableCollation.getSelectedItem();
//         if (!StrUtil.equalsIgnoreCase(collation, this.mysqlTable.getCollation())) {
//             tempTable.setCollation(collation);
//         }
//
//         // 行格式
//         if (this.tableRowFormatBox.isVisible()) {
//             String rowFormat = this.tableRowFormat.getValue();
//             if (!StrUtil.equalsIgnoreCase(rowFormat, this.mysqlTable.getRowFormat())) {
//                 tempTable.setRowFormat(rowFormat);
//             }
//         }
//
//         // 自动递增
//         if (this.tableAutoIncrementBox.isVisible()) {
//             Long autoIncrement = this.tableAutoIncrement.getValue();
//             if (!Objects.equals(autoIncrement, this.mysqlTable.getAutoIncrement())) {
//                 tempTable.setAutoIncrement(autoIncrement);
//             }
//         }
//
//         // 字段处理
//         MysqlColumns columns = new MysqlColumns();
//         for (MysqlColumn column : this.columnTable.getItems()) {
//             if (!column.isInvalid()) {
//                 columns.add(column);
//             }
//         }
//         if (CollUtil.isNotEmpty(this.columnTable.getDeleteItems())) {
//             columns.addAll(this.columnTable.getDeleteItems());
//         }
//
//         // 索引处理
//         MysqlIndexes indexes = new MysqlIndexes();
//         for (MysqlIndex index : this.indexTable.getItems()) {
//             if (!index.isInvalid()) {
//                 indexes.add(index);
//             }
//         }
//
//         // 外键处理
//         MysqlForeignKeys foreignKeys = new MysqlForeignKeys();
//         for (MysqlForeignKey foreignKey : this.foreignKeyTable.getItems()) {
//             if (!foreignKey.isInvalid()) {
//                 foreignKeys.add(foreignKey);
//             }
//         }
//
//         // 触发器处理
//         MysqlTriggers triggers = new MysqlTriggers();
//         for (MysqlTrigger trigger : this.triggerTable.getItems()) {
//             if (!trigger.isInvalid()) {
//                 triggers.add(trigger);
//             }
//         }
//
//         // 检查处理
//         MysqlChecks checks = null;
//         if (this.dbItem.isSupportCheckFeature()) {
//             checks = new MysqlChecks();
//             for (MysqlCheck check : this.checkTable.getItems()) {
//                 if (!check.isInvalid()) {
//                     checks.add(check);
//                 }
//             }
//         }
//         if (type == 1) {
//             return this.dbItem.createTableParam(tempTable, columns, indexes, foreignKeys, triggers, checks);
//         }
//         return this.dbItem.alterTableParam(tempTable, columns, indexes, foreignKeys, triggers, checks);
//     }
//
//     /**
//      * 保存db表
//      */
//     @FXML
//     private void save() {
//         try {
//             // 字段检查
//             for (MysqlColumn column : this.columnTable.getItems()) {
//                 if (column.isInvalid()) {
//                     MessageBox.warn(I18nHelper.invalidData());
//                     this.tabPane.selectTab("columnTab");
//                     return;
//                 }
//             }
//
//             // 索引检查
//             for (MysqlIndex index : this.indexTable.getItems()) {
//                 if (index.isInvalid()) {
//                     MessageBox.warn(I18nHelper.invalidData());
//                     this.tabPane.selectTab("indexTab");
//                     return;
//                 }
//             }
//
//             // 外键检查
//             for (MysqlForeignKey foreignKey : this.foreignKeyTable.getItems()) {
//                 if (foreignKey.isInvalid()) {
//                     MessageBox.warn(I18nHelper.invalidData());
//                     this.tabPane.selectTab("foreignKeyTab");
//                     return;
//                 }
//             }
//
//             // 触发器检查
//             for (MysqlTrigger trigger : this.triggerTable.getItems()) {
//                 if (trigger.isInvalid()) {
//                     MessageBox.warn(I18nHelper.invalidData());
//                     this.tabPane.selectTab("triggerTab");
//                     return;
//                 }
//             }
//
//             // 检查检查
//             if (this.dbItem.isSupportCheckFeature()) {
//                 for (MysqlCheck check : this.checkTable.getItems()) {
//                     if (check.isInvalid()) {
//                         MessageBox.warn(I18nHelper.invalidData());
//                         this.tabPane.selectTab("checkTab");
//                         return;
//                     }
//                 }
//             }
//
//             String tableName;
//             // 表名称
//             if (this.newData) {
//                 tableName = MessageBox.prompt(I18nHelper.pleaseInputTableName());
//                 if (tableName == null) {
//                     return;
//                 }
//             } else {
//                 tableName = this.mysqlTable.getName();
//             }
//
//             this.disableTab();
//
//             // 创建表
//             if (this.newData) {
//                 MysqlCreateTableParam param = this.initCreateParam();
//                 param.setTableName(tableName);
//                 this.dbItem.createTable(param);
//                 MysqlEventUtil.tableAdded(this.dbItem);
//             } else {// 修改表
//                 MysqlAlertTableParam param = this.initAlertParam();
//                 this.dbItem.alterTable(param);
//                 MysqlEventUtil.tableAlerted(tableName, this.dbItem);
//             }
//             // 判断结果
//             this.unsaved = false;
//             // 初始化信息
//             this.initInfo(tableName);
//             // 重置表格
//             this.resetTable();
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         } finally {
//             this.enableTab();
//             this.flushTab();
//         }
//     }
//
//     /**
//      * 初始化变更标志
//      */
//     private void initChangedFlag() {
//         if (!this.initiating) {
//             this.unsaved = true;
//             this.flushTab();
//         }
//     }
//
//     protected void resetTable() throws Exception {
//         this.columnTable.reset();
//     }
//
//     /**
//      * 初始化信息
//      */
//     protected void initInfo(String tableName) {
//         // 更新初始化标志位
//         this.initiating = true;
//         // 新数据
//         if (tableName == null) {
//             this.newData = true;
//             this.mysqlTable = new MysqlTable();
//             this.mysqlTable.setDbName(this.dbItem.dbName());
//             this.mysqlTable.setName(I18nHelper.unnamedTable());
//             this.initNew();
//         } else {// 已有数据
//             this.newData = false;
//             this.mysqlTable = this.dbItem.selectFullTable(tableName);
//             this.initNormal();
//         }
//         // 标记为结束
//         FXUtil.runPulse(() -> this.initiating = false);
//     }
//
//     /**
//      * 初始化信息
//      */
//     protected void initNew() {
//         // 重载表数据
//         this.tableEngine.select("innoDB");
//         // 字符集
//         if (this.tableCharset.isItemEmpty()) {
//             this.tableCharset.init(this.dbItem.client());
//         }
//     }
//
//     /**
//      * 初始化信息
//      */
//     protected void initNormal() {
//         this.moveUp.disappear();
//
//         // 基本信息
//         this.tableEngine.select(this.mysqlTable.getEngine());
//         this.tableComment.setText(this.mysqlTable.getComment());
//         // 字符集
//         if (this.tableCharset.isItemEmpty()) {
//             this.tableCharset.init(this.dbItem.client());
//         }
//         this.tableCharset.select(this.mysqlTable.getCharset());
//         // 排序规则
//         this.tableCollation.init(this.mysqlTable.getCharset(), this.dbItem.client());
//         this.tableCollation.select(this.mysqlTable.getCollation());
//
//         // 检查器
//         if (this.dbItem.isSupportCheckFeature()) {
//             this.checkTable.setItem(MysqlCheckControl.of(this.dbItem.checks(this.tableName())));
//         }
//         // 索引
//         this.indexTable.setItem(MysqlIndexControl.of(this.dbItem.indexes(this.tableName())));
//         // 字段
//         this.columnTable.setItem(MysqlColumnControl.of(this.dbItem.columns(this.tableName())));
//         // 触发器
//         this.triggerTable.setItem(MysqlTriggerControl.of(this.dbItem.triggers(this.tableName())));
//         // 外键
//         this.foreignKeyTable.setItem(MysqlForeignKeyControl.of(this.dbItem.foreignKeys(this.tableName())));
//
//         // 行格式
//         if (this.mysqlTable.isInnoDB()) {
//             this.tableRowFormatBox.display();
//             this.tableRowFormat.select(this.mysqlTable.getRowFormat());
//         }
//
//         // 表自动递增
//         if (this.mysqlTable.hasAutoIncrement()) {
//             this.tableAutoIncrementBox.display();
//             this.tableAutoIncrement.setValue(this.mysqlTable.getAutoIncrement());
//         }
//     }
//
//     /**
//      * 新增字段
//      */
//     private void addColumn() {
//         MysqlColumnControl column = new MysqlColumnControl();
//         column.setCreated(true);
//         column.setNullable(true);
//         this.columnTable.addItem(column);
//         this.columnTable.selectLast();
//     }
//
//     /**
//      * 删除字段
//      */
//     private void deleteColumn() {
//         try {
//             MysqlColumn column = this.columnTable.getSelectedItem();
//             if (column == null) {
//                 return;
//             }
//             // 确认操作
//             if (!column.isCreated() && !MessageBox.confirm(I18nHelper.deleteField() + " " + column.getName())) {
//                 return;
//             }
//             // 从table移除数据
//             this.columnTable.removeItem(column);
//             column.setDeleted(true);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 上移字段
//      */
//     private void moveColumnUp() {
//         try {
//             TableViewUtil.moveUp(this.columnTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 下移字段
//      */
//     private void moveColumnDown() {
//         try {
//             TableViewUtil.moveDown(this.columnTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 新增索引
//      */
//     private void addIndex() {
//         MysqlIndexControl index = new MysqlIndexControl();
//         index.setCreated(true);
//         this.indexTable.addItem(index);
//         this.indexTable.selectLast();
//     }
//
//     /**
//      * 删除索引
//      */
//     private void deleteIndex() {
//         try {
//             MysqlIndex index = this.indexTable.getSelectedItem();
//             if (index == null) {
//                 return;
//             }
//             // 从table移除数据
//             if (index.isCreated()) {
//                 this.indexTable.removeItem(index);
//                 return;
//             }
//             // 删除数据
//             if (MessageBox.confirm(I18nHelper.deleteIndex() + " " + index.getName())) {
//                 index.setDeleted(true);
//             }
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 上移索引
//      */
//     private void moveIndexUp() {
//         try {
//             TableViewUtil.moveUp(this.indexTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 下移索引
//      */
//     private void moveIndexDown() {
//         try {
//             TableViewUtil.moveDown(this.indexTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 新增外键
//      */
//     private void addForeignKey() {
//         MysqlForeignKeyControl foreignKey = new MysqlForeignKeyControl();
//         foreignKey.setCreated(true);
//         this.foreignKeyTable.addItem(foreignKey);
//         this.foreignKeyTable.selectLast();
//     }
//
//     /**
//      * 删除外键
//      */
//     private void deleteForeignKey() {
//         try {
//             MysqlForeignKey foreignKey = this.foreignKeyTable.getSelectedItem();
//             if (foreignKey == null) {
//                 return;
//             }
//             // 从table移除数据
//             if (foreignKey.isCreated()) {
//                 this.foreignKeyTable.removeItem(foreignKey);
//                 return;
//             }
//             // 删除数据
//             if (MessageBox.confirm(I18nHelper.deleteForeignKey() + " " + foreignKey.getName())) {
//                 foreignKey.setDeleted(true);
//             }
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 上移外键
//      */
//     private void moveForeignKeyUp() {
//         try {
//             TableViewUtil.moveUp(this.foreignKeyTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 下移外键
//      */
//     private void moveForeignKeyDown() {
//         try {
//             TableViewUtil.moveDown(this.foreignKeyTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 新增触发器
//      */
//     private void addTrigger() {
//         MysqlTriggerControl trigger = new MysqlTriggerControl();
//         trigger.setCreated(true);
//         this.triggerTable.addItem(trigger);
//         this.triggerTable.selectLast();
//     }
//
//     /**
//      * 删除触发器
//      */
//     private void deleteTrigger() {
//         try {
//             MysqlTrigger trigger = this.triggerTable.getSelectedItem();
//             if (trigger == null) {
//                 return;
//             }
//             // 从table移除数据
//             if (trigger.isCreated()) {
//                 this.triggerTable.removeItem(trigger);
//                 return;
//             }
//             // 删除数据
//             if (MessageBox.confirm(I18nHelper.deleteTrigger() + " " + trigger.getName())) {
//                 trigger.setDeleted(true);
//             }
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 上移触发器
//      */
//     private void moveTriggerUp() {
//         try {
//             TableViewUtil.moveUp(this.triggerTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 下移触发器
//      */
//     private void moveTriggerDown() {
//         try {
//             TableViewUtil.moveDown(this.triggerTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 新增检查
//      */
//     private void addCheck() {
//         MysqlCheckControl check = new MysqlCheckControl();
//         check.setCreated(true);
//         this.checkTable.addItem(check);
//         this.checkTable.selectLast();
//     }
//
//     /**
//      * 删除检查
//      */
//     private void deleteCheck() {
//         try {
//             MysqlCheck check = this.checkTable.getSelectedItem();
//             if (check == null) {
//                 return;
//             }
//             // 从table移除数据
//             if (check.isCreated()) {
//                 this.checkTable.removeItem(check);
//                 return;
//             }
//             // 删除数据
//             if (MessageBox.confirm(I18nHelper.deleteCheck() + " " + check.getName())) {
//                 check.setDeleted(true);
//             }
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 上移检查
//      */
//     private void moveCheckUp() {
//         try {
//             TableViewUtil.moveUp(this.checkTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 下移检查
//      */
//     private void moveCheckDown() {
//         try {
//             TableViewUtil.moveDown(this.checkTable);
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 初始化列表控件
//      */
//     private void initTable() {
//         // 字段
//         this.colName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
//         this.colType.setCellValueFactory(new PropertyValueFactory<>("typeControl"));
//         this.colSize.setCellValueFactory(new PropertyValueFactory<>("sizeControl"));
//         this.colDigits.setCellValueFactory(new PropertyValueFactory<>("digitsControl"));
//         this.colComment.setCellValueFactory(new PropertyValueFactory<>("commentControl"));
//         this.colNullable.setCellValueFactory(new PropertyValueFactory<>("nullableControl"));
//         this.colPrimaryKey.setCellValueFactory(new PropertyValueFactory<>("primaryKeyControl"));
//         // this.colConfig.setCellValueFactory(new PropertyValueFactory<>("configControl"));
//
//         // 索引
//         this.indexName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
//         this.indexColumn.setCellValueFactory(new PropertyValueFactory<>("columnControl"));
//         this.indexType.setCellValueFactory(new PropertyValueFactory<>("typeControl"));
//         this.indexMethod.setCellValueFactory(new PropertyValueFactory<>("methodControl"));
//         this.indexComment.setCellValueFactory(new PropertyValueFactory<>("commentControl"));
//
//         // 外键
//         this.foreignKeyName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
//         this.foreignKeyColumn.setCellValueFactory(new PropertyValueFactory<>("columnControl"));
//         this.foreignKeyPKTable.setCellValueFactory(new PropertyValueFactory<>("primaryKeyTableControl"));
//         this.foreignKeyDeletePolicy.setCellValueFactory(new PropertyValueFactory<>("deletePolicyControl"));
//         this.foreignKeyUpdatePolicy.setCellValueFactory(new PropertyValueFactory<>("updatePolicyControl"));
//         this.foreignKeyPKColumn.setCellValueFactory(new PropertyValueFactory<>("primaryKeyColumnControl"));
//         this.foreignKeyPKDatabase.setCellValueFactory(new PropertyValueFactory<>("primaryKeyDatabaseControl"));
//
//         // 触发器
//         this.triggerName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
//         this.triggerPolicy.setCellValueFactory(new PropertyValueFactory<>("policyControl"));
//         this.triggerDefinition.setCellValueFactory(new PropertyValueFactory<>("definitionControl"));
//
//         // 触发器
//         this.checkName.setCellValueFactory(new PropertyValueFactory<>("nameControl"));
//         this.checkClause.setCellValueFactory(new PropertyValueFactory<>("clauseControl"));
//
//         // 表单保存事件
//         this.indexTable.setCtrlSAction(this::save);
//         this.checkTable.setCtrlSAction(this::save);
//         this.columnTable.setCtrlSAction(this::save);
//         this.triggerTable.setCtrlSAction(this::save);
//         this.foreignKeyTable.setCtrlSAction(this::save);
//
//         // 监听事件
//         NodeUtil.nodeOnCtrlS(this.getTab(), this::save);
//         NodeUtil.nodeOnCtrlS(this.tableComment, this::save);
//         NodeUtil.nodeOnCtrlS(this.tableAutoIncrement, this::save);
//
//         // 更新字段列表
//         this.columnTable.itemsProperty().get().addListener((ListChangeListener<MysqlColumn>) c -> CacheHelper.set("columnList", this.columnTable.getItems()));
//
//         // // 监听列表变化
//         // this.checkTable.itemList().addListener(this.listChangeListener);
//         // this.indexTable.itemList().addListener(this.listChangeListener);
//         // this.columnTable.itemList().addListener(this.listChangeListener);
//         // this.triggerTable.itemList().addListener(this.listChangeListener);
//         // this.foreignKeyTable.itemList().addListener(this.listChangeListener);
//     }
//
//     @Override
//     public void onTabClosed(Event event) {
//         super.onTabClosed(event);
//         CacheHelper.clear();
//     }
//
//     @Override
//     protected void bindListeners() {
//         super.bindListeners();
//         // 字符集选中事件
//         this.tableCharset.selectedItemChanged((observable, oldValue, newValue) -> {
//             this.tableCollation.init(newValue, this.dbItem.client());
//             this.tableCollation.select(0);
//         });
//         // 引擎选中事件
//         this.tableEngine.selectedItemChanged((observable, oldValue, newValue) -> {
//             if (this.tableEngine.isInnoDB()) {
//                 this.tableRowFormatBox.display();
//                 if (StrUtil.isBlank(this.mysqlTable.getRowFormat())) {
//                     this.tableRowFormat.select(this.mysqlTable.getRowFormat());
//                 } else {
//                     this.tableRowFormat.select(3);
//                 }
//             } else {
//                 this.tableRowFormatBox.disappear();
//             }
//         });
//         // 表格下标监听
//         this.tabPane.selectedTabChanged((observable, oldValue, newValue) -> {
//             String tabId = newValue == null ? null : newValue.getId();
//             if (StrUtil.equalsAny(tabId, "columnTab", "indexTab", "foreignKeyTab", "triggerTab", "checkTab")) {
//                 this.add.display();
//                 if (this.newData) {
//                     this.moveUp.display();
//                 }
//             } else {
//                 this.add.disappear();
//                 if (this.newData) {
//                     this.moveUp.disappear();
//                 }
//             }
//             // 预览
//             if (StrUtil.equals(tabId, "previewTab")) {
//                 String sql;
//                 if (this.newData) {
//                     MysqlCreateTableParam param = this.initCreateParam();
//                     if (param.tableName() == null) {
//                         param.setTableName(I18nHelper.unnamedTable());
//                     }
//                     sql = MysqlTableCreateSqlGenerator.generateSql(param);
//                 } else {
//                     MysqlAlertTableParam param = this.initAlertParam();
//                     sql = MysqlTableAlertSqlGenerator.generateSql(param);
//                 }
//                 this.sqlPreview.setText(sql);
//             }
//         });
//         // 初始化监听器
//         this.listener = new DBStatusListener() {
//             @Override
//             public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
//                 initChangedFlag();
//             }
//         };
//
//         // 监听列表变化
//         this.checkTable.setStatusListener(this.listener);
//         this.indexTable.setStatusListener(this.listener);
//         this.columnTable.setStatusListener(this.listener);
//         this.triggerTable.setStatusListener(this.listener);
//         this.foreignKeyTable.setStatusListener(this.listener);
//
//         this.columnTable.selectedIndexChanged((observable, oldValue, newValue) -> this.tableColumnExtraController.init(this.columnTable.getSelectedItem(), this.dbItem.client()));
//     }
//
//     @Override
//     public void initialize(URL location, ResourceBundle resourceBundle) {
//         super.initialize(location, resourceBundle);
//         // 初始化表单
//         this.initTable();
//         // 组件管理
//         this.delete.visibleProperty().bind(this.add.visibleProperty());
//         this.delete.managedProperty().bind(this.add.managedProperty());
//         this.moveDown.visibleProperty().bind(this.moveUp.visibleProperty());
//         this.moveDown.managedProperty().bind(this.moveUp.managedProperty());
//         this.tableRowFormat.managedBindVisible();
//         this.tableAutoIncrementBox.managedBindVisible();
//     }
//
//     /**
//      * 执行初始化
//      *
//      * @param tableName 表信息
//      * @param dbItem    db库树节点
//      */
//     public void init(String tableName, MysqlDatabaseTreeItem dbItem) throws Exception {
//         // 获取对象
//         this.dbItem = dbItem;
//         // 初始化引擎
//         this.tableEngine.init(this.dbItem.client());
//
//         // 设置缓存
//         CacheHelper.set("dbName", this.dbItem.dbName());
//         CacheHelper.set("dbClient", this.dbItem.client());
//
//         // 初始化信息
//         this.initInfo(tableName);
//
//         // // 初始化监听器
//         // this.initDBListener();
//
//         // 监听组件
//         DBStatusListenerManager.bindListener(this.tableEngine, this.listener);
//         DBStatusListenerManager.bindListener(this.tableCharset, this.listener);
//         DBStatusListenerManager.bindListener(this.tableComment, this.listener);
//         DBStatusListenerManager.bindListener(this.tableRowFormat, this.listener);
//         DBStatusListenerManager.bindListener(this.tableCollation, this.listener);
//         DBStatusListenerManager.bindListener(this.tableAutoIncrement, this.listener);
//
//         // 移除tab
//         if (!this.dbItem.isSupportCheckFeature()) {
//             this.tabPane.removeTab("checkTab");
//         }
//     }
//
//     /**
//      * 执行添加
//      */
//     @FXML
//     private void doAdd() {
//         if (this.tabPane.isSelectedTab("columnTab")) {
//             this.addColumn();
//         } else if (this.tabPane.isSelectedTab("indexTab")) {
//             this.addIndex();
//         } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
//             this.addForeignKey();
//         } else if (this.tabPane.isSelectedTab("triggerTab")) {
//             this.addTrigger();
//         } else if (this.tabPane.isSelectedTab("checkTab")) {
//             this.addCheck();
//         }
//     }
//
//     /**
//      * 执行删除
//      */
//     @FXML
//     private void doDelete() {
//         if (this.tabPane.isSelectedTab("columnTab")) {
//             this.deleteColumn();
//         } else if (this.tabPane.isSelectedTab("indexTab")) {
//             this.deleteIndex();
//         } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
//             this.deleteForeignKey();
//         } else if (this.tabPane.isSelectedTab("triggerTab")) {
//             this.deleteTrigger();
//         } else if (this.tabPane.isSelectedTab("checkTab")) {
//             this.deleteCheck();
//         }
//     }
//
//     /**
//      * 执行上移
//      */
//     @FXML
//     private void doMoveUp() {
//         if (this.tabPane.isSelectedTab("columnTab")) {
//             this.moveColumnUp();
//         } else if (this.tabPane.isSelectedTab("indexTab")) {
//             this.moveIndexUp();
//         } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
//             this.moveForeignKeyUp();
//         } else if (this.tabPane.isSelectedTab("triggerTab")) {
//             this.moveTriggerUp();
//         } else if (this.tabPane.isSelectedTab("checkTab")) {
//             this.moveCheckUp();
//         }
//     }
//
//     /**
//      * 执行下移
//      */
//     @FXML
//     private void doMoveDown() {
//         if (this.tabPane.isSelectedTab("columnTab")) {
//             this.moveColumnDown();
//         } else if (this.tabPane.isSelectedTab("indexTab")) {
//             this.moveIndexDown();
//         } else if (this.tabPane.isSelectedTab("foreignKeyTab")) {
//             this.moveForeignKeyDown();
//         } else if (this.tabPane.isSelectedTab("triggerTab")) {
//             this.moveTriggerDown();
//         } else if (this.tabPane.isSelectedTab("checkTab")) {
//             this.moveCheckDown();
//         }
//     }
//
//     public String tableName() {
//         return this.mysqlTable.getName();
//     }
//
//     public String dbName() {
//         return this.mysqlTable.getDbName();
//     }
//
//     @Override
//     public List<? extends SubTabController> getSubControllers() {
//         return List.of(this.tableColumnExtraController);
//     }
// }
