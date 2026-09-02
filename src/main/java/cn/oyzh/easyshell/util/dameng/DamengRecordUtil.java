package cn.oyzh.easyshell.util.dameng;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.record.DamengRecordProperty;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import cn.oyzh.fx.editor.incubator.control.LongTextFiled;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BitTextField;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import cn.oyzh.fx.gui.text.field.DateTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.TimeTextField;
import cn.oyzh.fx.gui.text.field.YearTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/17
 */
public class DamengRecordUtil {

    public static Node getNode(DamengRecordProperty property, Object object, DamengColumn column) {
        object = DamengDataUtil.valueStandardization(object);
        Node node;
        if (column.supportJson()) {
            JsonTextFiled textField = new JsonTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#C9E4E8")));
            node = textField;
        } else if (column.supportText()) {
            LongTextFiled textField = new LongTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#A1C9D1")));
            node = textField;
        } else if (column.supportBinary()) {
            BinaryTextFiled textField = new BinaryTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#FBF0D0")));
            node = textField;
        } else if (column.supportInteger()) {
            NumberTextField textField = new NumberTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#D7EED0")));
            node = textField;
        } else if (column.supportDigits()) {
            DecimalTextField textField = new DecimalTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#CDECFA")));
            node = textField;
        } else if (column.supportBit()) {
            BitTextField textField = new BitTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#FCE1E4")));
            node = textField;
        } else if (column.supportBoolean()) {
            BooleanTextFiled textField = new BooleanTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#FCE1E4")));
            node = textField;
        } else if (column.isDateType()) {
            DateTextField textField = new DateTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#F1E1F5")));
            node = textField;
        } else if (column.isTimeType()) {
            TimeTextField textField = new TimeTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#E0F0E8")));
            node = textField;
        } else if (column.isYearType()) {
            YearTextField textField = new YearTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#F0E8E0")));
            node = textField;
        } else if (column.supportTimestamp()) {
            DateTimeTextField textField = new DateTimeTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#F1E1F5")));
            node = textField;
        } else if (column.supportString()) {
            FXTextField textField = new FXTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#E8F0E8")));
            node = textField;
        } else {
            FXTextField textField = new FXTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#FDD4D3")));
            node = textField;
        }
        if (node instanceof FXTextField textField) {
            if (object == null) {
                if (column.exampleValue() == null) {
                    textField.setPromptText(DBUtil.nullPromptText());
                } else {
                    textField.setPromptText(column.exampleValue().toString());
                }
            }
            textField.setContextMenu(getColumnContextMenu(property));
            textField.addTextChangeListener((observable, oldValue, newValue) -> property.setChanged(true));
        }
        return node;
    }

    public static String formatValue(Object object, DamengColumn column) {
        object = DamengDataUtil.valueStandardization(object);
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
            //        } else if (column.supportEnum()) {
            //            val = SelectTextFiled.format(object);
        } else if (column.supportInteger()) {
            val = NumberTextField.format(object);
        } else if (column.supportDigits()) {
            val = DecimalTextField.format(object);
        } else if (column.supportBit()) {
            val = BitTextField.format(object);
        } else if (column.supportBoolean()) {
            val = BooleanTextFiled.format(object);
        } else if (column.isDateType()) {
            val = DateTextField.format(object);
        } else if (column.isTimeType()) {
            val = TimeTextField.format(object);
        } else if (column.isYearType()) {
            val = YearTextField.format(object);
        } else if (column.supportText()) {
            val = LongTextFiled.format(object);
            // } else if (column.supportString()) {
            //     val = FXTextField.format(object);
            //        } else if (column.supportGeometry()) {
            //            val = ExampleTextField.format(object);
        } else {
            val = FXTextField.format(object);
        }
        return val;
    }

    //    public static String nullPromptText() {
    //        return "(Null)";
    //    }

    //    /**
    //     * 计算合适的字段宽
    //     *
    //     * @param column 字段
    //     * @return 结果
    //     */
    //    public static double suitableColumnWidth(DamengColumn column) {
    //        double w1 = FontUtil.textWidth(column.getName());
    //        double w2;
    //        if (column.supportSize() && column.getSize() != null) {
    //            w2 = FontUtil.textWidth(column.getType() + "(" + column.getSize() + ")");
    //        } else {
    //            w2 = FontUtil.textWidth(column.getType());
    //        }
    //        double w3 = Math.max(w1, w2);
    //        return w3 + 30;
    //    }

    public static ContextMenu getColumnContextMenu(DamengRecordProperty property) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(getColumnMenuItem(property));
        return contextMenu;
    }

    public static List<FXMenuItem> getColumnMenuItem(DamengRecordProperty property) {
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
