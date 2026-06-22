package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecordProperty;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BitTextField;
import cn.oyzh.fx.gui.text.field.DateTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.ExampleTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.gui.text.field.TimeTextField;
import cn.oyzh.fx.gui.text.field.YearTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuManager;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/17
 */
public class ShellMysqlRecordUtil {

    /**
     * 获取节点
     *
     * @param property 属性
     * @param object   对象
     * @param column   字段
     * @return 节点
     */
    public static Node getNode(MysqlRecordProperty property, Object object, MysqlColumn column) {
        Node node;
        if (column.supportJson()) {
            JsonTextFiled textField = new JsonTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportBinary()) {
            BinaryTextFiled textField = new BinaryTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportEnum()) {
            SelectTextFiled<String> textField = new SelectTextFiled<>();
            textField.setEditable(false);
            textField.setItemList(column.getValueList());
            textField.setValue(object);
            node = textField;
        } else if (column.supportInteger()) {
            NumberTextField textField = new NumberTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportDigits()) {
            DecimalTextField textField = new DecimalTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportBit()) {
            BitTextField textField = new BitTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.isDateType()) {
            DateTextField textField = new DateTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.isTimeType()) {
            TimeTextField textField = new TimeTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.isYearType()) {
            YearTextField textField = new YearTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportTimestamp() || column.isDateTimeType()) {
            DateTimeTextField textField = new DateTimeTextField();
            textField.setValue(object);
            node = textField;
            //        } else if (column.supportString()) {
            //            ClearableTextField textField = new ClearableTextField();
            //            textField.setValue(object);
            //            node = textField;
        } else if (column.supportGeometry()) {
            ExampleTextField textField = new ExampleTextField();
            textField.setExample(column.exampleValue());
            textField.setValue(object);
            node = textField;
        } else {
            FXTextField textField = new FXTextField();
            textField.setValue(object);
            node = textField;
        }
        if (node instanceof TextField textField) {
            if (object == null) {
                textField.setPromptText(nullPromptText());
            }
            textField.setOnContextMenuRequested(event -> {
                if (textField.getContextMenu() == null) {
                    List<FXMenuItem> menuItems = getColumnMenuItem(property);
                    FXContextMenu contextMenu = ContextMenuManager.createContextMenu(textField, menuItems);
                    ContextMenuManager.setContextMenu(textField, contextMenu);
                    ContextMenuManager.showContextMenu(contextMenu, textField, event);
                }
            });
            textField.textProperty().addListener((observable, oldValue, newValue) -> property.setChanged(true));
        }
        return node;
    }

    /**
     * 格式值
     *
     * @param object 对象
     * @param column 字段
     * @return 值
     */
    public static String formatValue(Object object, MysqlColumn column) {
        String val = null;
        String columnType = column.getType();
        if (StringUtil.isBlank(columnType)) {
            if (object instanceof CharSequence sequence) {
                val = sequence.toString();
            } else if (object instanceof byte[] bytes) {
                val = new String(bytes);
            } else if (object instanceof Date date) {
                val = date.toString();
            } else if (object != null) {
                val = object.toString();
            }
        } else if (column.supportJson()) {
            val = JsonTextFiled.format(object);
        } else if (column.supportBinary()) {
            val = BinaryTextFiled.format(object);
        } else if (column.supportEnum()) {
            val = SelectTextFiled.format(object);
        } else if (column.supportInteger()) {
            val = NumberTextField.format(object);
        } else if (column.supportDigits()) {
            val = DecimalTextField.format(object);
        } else if (column.supportBit()) {
            val = BitTextField.format(object);
        } else if (column.isDateType()) {
            val = DateTextField.format(object);
        } else if (column.isTimeType()) {
            val = TimeTextField.format(object);
        } else if (column.isYearType()) {
            val = YearTextField.format(object);
        } else if (column.supportTimestamp() || column.isDateTimeType()) {
            val = DateTimeTextField.format(object);
            //        } else if (column.supportString()) {
            //            val = ClearableTextField.format(object);
        } else if (column.supportGeometry()) {
            val = ExampleTextField.format(object);
        } else {
            val = FXTextField.format(object);
        }
        return val;
    }

    /**
     * null背景内容
     *
     * @return 结果
     */
    public static String nullPromptText() {
        return "(Null)";
    }

    /**
     * 计算合适的字段宽
     *
     * @param column 字段
     * @return 结果
     */
    public static double suitableColumnWidth(ShellMysqlRecordColumn column) {
        String str1 = column.getName();
        String str2 = column.getType();
        if (column.supportSize() && column.getSize() != null) {
            str2 = column.getType() + "(" + column.getSize() + ")";
        }
        double w1 = FontUtil.textWidth(str1, column.getFont());
        double w2 = FontUtil.textWidth(str2, column.getFont());
        double w3 = Math.max(w1, w2);
        return w3 + 50;
    }

    //    public static ContextMenu getColumnContextMenu(MysqlRecordProperty property) {
    //        ContextMenu contextMenu = new ContextMenu();
    //        contextMenu.getItems().setAll(getColumnMenuItem(property));
    //        return contextMenu;
    //    }

    /**
     * 获取字段菜单列表
     *
     * @param property 属性
     * @return 菜单列表
     */
    public static List<FXMenuItem> getColumnMenuItem(MysqlRecordProperty property) {
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem copy = MenuItemHelper.copy_no_graphic(property::vCopy);
        menuItems.add(copy);
        FXMenuItem paste = MenuItemHelper.paste_no_graphic(property::vPaste);
        menuItems.add(paste);
        FXMenuItem setToNull = MenuItemHelper.setToNull_no_graphic(property::vSetToNull);
        menuItems.add(setToNull);
        FXMenuItem setToEmptyString = MenuItemHelper.setToEmptyString_no_graphic(property::vSetToEmptyString);
        menuItems.add(setToEmptyString);
        FXMenuItem copyAsInsertStatement = MenuItemHelper.copyAsInsertStatement_no_graphic(property::vCopyAsInsertSql);
        menuItems.add(copyAsInsertStatement);
        FXMenuItem copyAsUpdateStatement = MenuItemHelper.copyAsUpdateStatement_no_graphic(property::vCopyAsUpdateSql);
        menuItems.add(copyAsUpdateStatement);
        return menuItems;
    }
}
