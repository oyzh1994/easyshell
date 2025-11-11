// package cn.oyzh.easyshell.popups.mysql;
//
// import cn.oyzh.easyshell.fx.mysql.ShellMysqlCharsetComboBox;
// import cn.oyzh.easyshell.fx.mysql.DBCollationComboBox;
// import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlEnumTextFiled;
// import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlDefaultValueTextFiled;
// import cn.oyzh.easyshell.mysql.ShellMysqlClient;
// import cn.oyzh.easyshell.mysql.column.MysqlColumn;
// import cn.oyzh.fx.gui.text.field.NumberTextField;
// import cn.oyzh.fx.plus.FXConst;
// import cn.oyzh.fx.plus.controller.PopupController;
// import cn.oyzh.fx.plus.controls.box.FXHBox;
// import cn.oyzh.fx.plus.controls.button.FXCheckBox;
// import cn.oyzh.fx.plus.information.MessageBox;
// import cn.oyzh.fx.plus.window.PopupAdapter;
// import cn.oyzh.fx.plus.window.PopupAttribute;
// import javafx.fxml.FXML;
// import javafx.stage.WindowEvent;
//
// /**
//  * 字段配置弹窗
//  *
//  * @author oyzh
//  * @since 2024/07/12
//  */
// @PopupAttribute(
//         value = FXConst.POPUP_PATH + "mysql/shellMysqlColumnConfigPopup.fxml"
// )
// public class ShellMysqlColumnConfigPopupController extends PopupController {
//
//     /**
//      * 默认值组件
//      */
//     @FXML
//     private FXHBox defaultValueBox;
//
//     /**
//      * 默认值
//      */
//     @FXML
//     private ShellMysqlDefaultValueTextFiled defaultValue;
//
//     /**
//      * 字段值组件
//      */
//     @FXML
//     private FXHBox valueBox;
//
//     /**
//      * 字段值
//      */
//     @FXML
//     private ShellMysqlEnumTextFiled value;
//
//     /**
//      * 主键长度组件
//      */
//     @FXML
//     private FXHBox primaryKeySizeBox;
//
//     /**
//      * 主键长度
//      */
//     @FXML
//     private NumberTextField primaryKeySize;
//
//     /**
//      * 填充零组件
//      */
//     @FXML
//     private FXHBox zeroFillBox;
//
//     /**
//      * 填充零
//      */
//     @FXML
//     private FXCheckBox zeroFill;
//
//     /**
//      * 自动递增组件
//      */
//     @FXML
//     private FXHBox autoIncrementBox;
//
//     /**
//      * 自动递增
//      */
//     @FXML
//     private FXCheckBox autoIncrement;
//
//     /**
//      * 无符号组件
//      */
//     @FXML
//     private FXHBox unsignedBox;
//
//     /**
//      * 无符号
//      */
//     @FXML
//     private FXCheckBox unsigned;
//
//     /**
//      * 根据当前时间戳更新组件
//      */
//     @FXML
//     private FXHBox currentTimestampBox;
//
//     /**
//      * 根据当前时间戳更新
//      */
//     @FXML
//     private FXCheckBox currentTimestamp;
//
//     /**
//      * 字符集组件
//      */
//     @FXML
//     private FXHBox charsetBox;
//
//     /**
//      * 字符集
//      */
//     @FXML
//     private ShellMysqlCharsetComboBox charset;
//
//     /**
//      * 排序方式组件
//      */
//     @FXML
//     private FXHBox collationBox;
//
//     /**
//      * 排序方式
//      */
//     @FXML
//     private DBCollationComboBox collation;
//
//     /**
//      * db字段
//      */
//     private MysqlColumn dbColumn;
//
//     /**
//      * db客户端
//      */
//     private ShellMysqlClient dbClient;
//
//     /**
//      * 提交
//      */
//     @FXML
//     private void submit() {
//         try {
//             // 值处理
//             if (this.valueBox.isVisible()) {
//                 this.dbColumn.setValue(this.value.getTextTrim());
//             }
//             // 字符集、排序处理
//             if (this.charsetBox.isVisible()) {
//                 this.dbColumn.setCharset(this.charset.getValue());
//                 this.dbColumn.setCollation(this.collation.getValue());
//             }
//             // 填充零处理
//             if (this.zeroFillBox.isVisible()) {
//                 this.dbColumn.setZeroFill(this.zeroFill.isSelected());
//             }
//             // 无符号处理
//             if (this.unsignedBox.isVisible()) {
//                 this.dbColumn.setUnsigned(this.unsigned.isSelected());
//             }
//             // 默认值处理
//             if (this.defaultValueBox.isEnable()) {
//                 this.dbColumn.setDefaultValue(this.defaultValue.getValue());
//             }
//             // 自动递增处理
//             if (this.autoIncrementBox.isVisible()) {
//                 this.dbColumn.setAutoIncrement(this.autoIncrement.isSelected());
//             }
//             // 主键长度处理
//             if (this.primaryKeySizeBox.isVisible()) {
//                 this.dbColumn.setPrimaryKeySize(this.primaryKeySize.getIntValue());
//             }
//             // 根据时间戳更新处理
//             if (this.currentTimestampBox.isVisible()) {
//                 this.dbColumn.setUpdateOnCurrentTimestamp(this.currentTimestamp.isSelected());
//             }
//             this.closeWindow();
//         } catch (Exception ex) {
//             MessageBox.exception(ex);
//         }
//     }
//
//     /**
//      * 关闭
//      */
//     @FXML
//     private void close() {
//         this.closeWindow();
//     }
//
//     @Override
//     protected void bindListeners() {
//         super.bindListeners();
//         // 字符集选中事件
//         this.charset.selectedItemChanged((observable, oldValue, newValue) -> {
//             this.collation.init(newValue, this.dbClient);
//             this.collation.select(0);
//         });
//     }
//
//     @Override
//     public void onWindowShowing(WindowEvent event) {
//         super.onWindowShowing(event);
//         this.dbColumn = this.getProp("dbColumn");
//         this.dbClient = this.getProp("dbClient");
//         // 值
//         if (this.dbColumn.supportValue()) {
//             this.valueBox.display();
//             this.value.setValues(this.dbColumn.getValueList());
//         }
//         // 默认值
//         if (this.dbColumn.supportDefaultValue()) {
//             this.defaultValueBox.enable();
//             this.defaultValue.init(this.dbColumn);
//             this.defaultValue.setText(this.dbColumn.getDefaultValueString());
//         }
//         // 自动递增
//         if (this.dbColumn.supportAutoIncrement()) {
//             this.autoIncrementBox.display();
//             this.autoIncrement.setSelected(this.dbColumn.isAutoIncrement());
//         }
//         // 填充零
//         if (this.dbColumn.supportZeroFill()) {
//             this.zeroFillBox.display();
//             this.zeroFill.setSelected(this.dbColumn.isZeroFill());
//         }
//         // 无符号
//         if (this.dbColumn.supportUnsigned()) {
//             this.unsignedBox.display();
//             this.unsigned.setSelected(this.dbColumn.isUnsigned());
//         }
//         // 字符集及排序
//         if (this.dbColumn.supportCharset()) {
//             this.charsetBox.display();
//             this.collationBox.display();
//             this.charset.init(this.dbClient);
//             this.charset.select(this.dbColumn.getCharset());
//             this.collation.select(this.dbColumn.getCollation());
//         }
//         // 主键长度
//         if (this.dbColumn.isPrimaryKey() && this.dbColumn.supportKeySize()) {
//             this.primaryKeySizeBox.display();
//             if (this.dbColumn.getPrimaryKeySize() != null) {
//                 this.primaryKeySize.setValue(this.dbColumn.getPrimaryKeySize());
//             }
//         }
//         // 根据时间戳更新
//         if (this.dbColumn.supportTimestamp()) {
//             this.currentTimestampBox.display();
//             this.currentTimestamp.setSelected(this.dbColumn.isUpdateOnCurrentTimestamp());
//         }
//     }
//
//     @Override
//     public void onPopupInitialize(PopupAdapter window) {
//         super.onPopupInitialize(window);
//         this.valueBox.managedBindVisible();
//         this.charsetBox.managedBindVisible();
//         this.unsignedBox.managedBindVisible();
//         this.zeroFillBox.managedBindVisible();
//         this.collationBox.managedBindVisible();
//         this.autoIncrementBox.managedBindVisible();
//         this.primaryKeySizeBox.managedBindVisible();
//         this.currentTimestampBox.managedBindVisible();
//     }
// }
