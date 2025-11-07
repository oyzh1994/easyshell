package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.record.DBJsonTextFiled;
import cn.oyzh.easyshell.fx.mysql.record.MysqlBinaryTextFiled;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecordProperty;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.text.field.BitTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.DateTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.ExampleTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.gui.text.field.TimeTextField;
import cn.oyzh.fx.gui.text.field.YearTextField;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/17
 */
public class ShellMysqlRecordUtil {

    public static Node getNode(MysqlRecordProperty property, Object object, MysqlColumn column) {
        Node node;
        String columnType = column.getType();
        if (column.supportJson()) {
            DBJsonTextFiled textField = new DBJsonTextFiled();
            textField.setValue(object);
            node = textField;
        } else if (column.supportBinary()) {
            MysqlBinaryTextFiled textField = new MysqlBinaryTextFiled(columnType);
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
        } else if (column.supportTimestamp()) {
            DateTimeTextField textField = new DateTimeTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportString()) {
            ClearableTextField textField = new ClearableTextField();
            textField.setValue(object);
            node = textField;
        } else if (column.supportGeometry()) {
            ExampleTextField textField = new ExampleTextField();
            textField.setExample(column.exampleValue());
            textField.setValue(object);
            node = textField;
        } else {
            ClearableTextField textField = new ClearableTextField();
            textField.setValue(object);
            node = textField;
        }
        if (node instanceof TextField textField) {
            if (object == null) {
                textField.setPromptText(nullPromptText());
            }
            textField.setContextMenu(getColumnContextMenu(property));
            textField.textProperty().addListener((observable, oldValue, newValue) -> property.setChanged(true));
        }
        return node;
    }

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
            val = DBJsonTextFiled.format(object);
        } else if (column.supportBinary()) {
            if (object instanceof byte[] bytes) {
                val = MysqlBinaryTextFiled.format(columnType, bytes);
            }
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
        } else if (column.supportTimestamp()) {
            val = DateTimeTextField.format(object);
        } else if (column.supportString()) {
            val = ClearableTextField.format(object);
        } else if (column.supportGeometry()) {
            val = ExampleTextField.format(object);
        } else {
            val = ClearableTextField.format(object);
        }
        return val;
    }

    public static String nullPromptText() {
        return "(Null)";
    }

    // public static double suitableColumnWidth(String columnType) {
    //     if (ShellMysqlColumnUtil.isGeometryType(columnType)) {
    //         return 120;
    //     }
    //     if (ShellMysqlColumnUtil.isPointType(columnType)) {
    //         return 110;
    //     }
    //     if (ShellMysqlColumnUtil.isMultiPointType(columnType)) {
    //         return 200;
    //     }
    //     if (ShellMysqlColumnUtil.isPolygonType(columnType)) {
    //         return 220;
    //     }
    //     if (ShellMysqlColumnUtil.isMultiPolygonType(columnType)) {
    //         return 420;
    //     }
    //     if (ShellMysqlColumnUtil.isLineStringType(columnType)) {
    //         return 180;
    //     }
    //     if (ShellMysqlColumnUtil.isMultiLineStringType(columnType)) {
    //         return 320;
    //     }
    //     if (ShellMysqlColumnUtil.isGeomCollectionType(columnType)) {
    //         return 600;
    //     }
    //     if (ShellMysqlColumnUtil.isYearType(columnType)) {
    //         return 80;
    //     }
    //     if (ShellMysqlColumnUtil.supportJson(columnType)) {
    //         return 150;
    //     }
    //     if (ShellMysqlColumnUtil.supportTimestamp(columnType)) {
    //         return 160;
    //     }
    //     if (ShellMysqlColumnUtil.supportBinary(columnType)) {
    //         return 140;
    //     }
    //     if (ShellMysqlColumnUtil.isDateType(columnType)) {
    //         return 110;
    //     }
    //     return 100;
    // }

    /**
     * 计算合适的字段宽
     *
     * @param column 字段
     * @return 结果
     */
    public static double suitableColumnWidth(MysqlColumn column) {
        double w1 = FontUtil.stringWidth(column.getName());
        double w2;
        if (column.supportSize() && column.getSize() != null) {
            w2 = FontUtil.stringWidth(column.getType() + "(" + column.getSize() + ")");
        } else {
            w2 = FontUtil.stringWidth(column.getType());
        }
        double w3 = Math.max(w1, w2);
        return w3 + 30;
    }

    public static ContextMenu getColumnContextMenu(MysqlRecordProperty property) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(getColumnMenuItem(property));
        return contextMenu;
    }

    public static List<FXMenuItem> getColumnMenuItem(MysqlRecordProperty property) {
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem copy = MenuItemHelper.copy(property::vCopy);
        menuItems.add(copy);
        FXMenuItem paste = MenuItemHelper.paste(property::vPaste);
        menuItems.add(paste);
        // FXMenuItem delete = MenuItemHelper.deleteRecord(property::vDelete);
        FXMenuItem setToNull = MenuItemHelper.setToNull(property::vSetToNull);
        menuItems.add(setToNull);
        FXMenuItem setToEmptyString = MenuItemHelper.setToEmptyString(property::vSetToEmptyString);
        menuItems.add(setToEmptyString);
        FXMenuItem copyAsInsertStatement = MenuItemHelper.copyAsInsertStatement(property::vCopyAsInsertSql);
        menuItems.add(copyAsInsertStatement);
        FXMenuItem copyAsUpdateStatement = MenuItemHelper.copyAsUpdateStatement(property::vCopyAsUpdateSql);
        menuItems.add(copyAsUpdateStatement);
        // menuItems.add(delete);
        return menuItems;
    }
}
