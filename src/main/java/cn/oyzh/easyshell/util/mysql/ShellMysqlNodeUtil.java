package cn.oyzh.easyshell.util.mysql;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.gui.text.field.BitTextField;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.DateTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.DigitalTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.TimeTextField;
import cn.oyzh.fx.gui.text.field.YearTextField;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.util.ArrayList;
import java.util.List;

/**
 * db节点工具类
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class ShellMysqlNodeUtil {

    public static Object getNodeVal(Node node) throws Exception {
        Object val = null;
        if (node instanceof TimeTextField node1) {
            val = node1.getValue();
        } else if (node instanceof DateTimeTextField node1) {
            val = node1.getValue();
        } else if (node instanceof DateTextField node1) {
            val = node1.getValue();
        // } else if (node instanceof YearPicker picker) {
        //     val = picker.yearValue();
        // } else if (node instanceof CalendarPicker<?> picker) {
        //     val = picker.getValue();
        } else if (node instanceof NumberTextField textField) {
            val = textField.getValue();
        } else if (node instanceof DecimalTextField textField) {
            val = textField.getValue();
        } else if (node instanceof BitTextField textField) {
            val = textField.getValue();
        } else if (node instanceof ChooseFileTextField textField) {
            val = textField.getData();
//        } else if (node instanceof DBGeometryTextField textField) {
//            val = textField.getGeometryText();
        } else if (node instanceof TextField textField) {
            val = textField.getText();
        } else if (node instanceof TextArea textArea) {
            val = textArea.getText();
        // } else if (node instanceof RichJsonTextAreaPane textAreaPane) {
        //     val = textAreaPane.getJsonStr();
        // } else if (node instanceof RichTextAreaPane<?> textAreaPane) {
        //     val = textAreaPane.getText();
//        } else if (node instanceof DBFieldValueComboBox comboBox) {
//            val = comboBox.getFieldValue();
        } else if (node instanceof ComboBox<?> comboBox) {
            val = comboBox.getSelectionModel().getSelectedItem();
        }
        return val;
    }

    public static void setNodeVal(Node node, Object val) {
        if (node == null || val == null) {
            return;
        }
        // if (node instanceof CalendarPicker<?> picker) {
        //     picker.setValue(val);
        // }
        if (node instanceof NumberTextField textField) {
            textField.setValue(val);
        } else if (node instanceof DecimalTextField textField) {
            textField.setValue(val);
        } else if (node instanceof BitTextField textField) {
            textField.setValue(val);
        } else if (node instanceof ChooseFileTextField textField) {
            textField.setData(val);
        } else if (node instanceof TextField textField) {
            textField.setText(val.toString());
        // } else if (node instanceof RichJsonTextAreaPane textAreaPane) {
        //     textAreaPane.setJsonStr(val.toString());
        // } else if (node instanceof RichTextAreaPane<?> textAreaPane) {
        //     textAreaPane.setText(val.toString());
        } else if (node instanceof TextArea textArea) {
            textArea.setText(val.toString());
        } else if (node instanceof ComboBox comboBox) {
            comboBox.getSelectionModel().select(val);
        }
    }

    public static Node generateNode(MysqlColumn column) {
        return generateNode(column, true);
    }

    public static Node generateNode(MysqlColumn column, boolean handlerDefaultValue) {
        Node node;
        if (column == null) {
            node = new FXTextField();
        } else if (column.supportJson()) {
            Editor editor = new Editor();
            editor.setFormatType(EditorFormatType.JSON);
            node = editor;

//        } else if (column.supportGeometry()) {
//            DBGeometryTextField filed = new DBGeometryTextField();
//            filed.setExample(column.exampleValue());
//            node = filed;
        } else if (column.supportString()) {
            if (column.supportSize() && column.getSize() != null) {
                node = new ClearableTextField((long) column.getSize());
            } else {
                node = new ClearableTextField();
            }
        } else if (column.supportBit()) {
            if (column.getSize() != null) {
                node = new BitTextField((long) column.getSize() * 8L);
            } else {
                node = new BitTextField();
            }
        } else if (column.supportInteger()) {
            Integer size = column.getSize();
            node = new NumberTextField(column.isUnsigned(), size == null ? null : size.longValue(), column.minValue(), column.maxValue());
        } else if (column.supportDigits()) {
            Integer size = column.getSize();
            node = new DecimalTextField(column.isUnsigned(), size == null ? null : size.longValue(), column.minValue(), column.maxValue(), column.getDigits());
        } else if (column.isYearType()) {
            node = new YearTextField();
        } else if (column.isTimeType()) {
            node = new TimeTextField();
        } else if (column.isDateType()) {
            node = new DateTextField();
        } else if (column.supportTimestamp()) {
            node = new DateTimeTextField();
        } else if (column.supportBinary()) {
            node = new ChooseFileTextField();
//        } else if (column.supportEnum()) {
//            node = new DBFieldValueComboBox(column.getValueList());
        } else {
            node = new ClearableTextField();
        }
        node.setId("value");
        if (column != null) {
            handlerDigits(node, column.getDigits());
            handlerComment(node, column.getComment());
            if (handlerDefaultValue && column.getDefaultValue() != null) {
                handlerDefaultValue(node, column.getDefaultValue());
            }
        }
        return node;
    }

    public static List<FXLabel> generateTags(MysqlColumn column) {
        List<FXLabel> labels = new ArrayList<>();
        if (column.isNullable()) {
            FXLabel label = new FXLabel(I18nHelper.nullable());
            label.addClass("tag_nullable");
            labels.add(label);
        }
        if (column.isAutoIncrement()) {
            FXLabel label = new FXLabel(I18nHelper.autoIncrement());
            label.addClass("tag_autoIncrement");
            labels.add(label);
        }
        if (column.isUpdateOnCurrentTimestamp()) {
            FXLabel label = new FXLabel(I18nHelper.updateByCurrentTimestamp());
            label.addClass("tag_updateOnCurrentTimestamp");
            labels.add(label);
        }
        if (column.isPrimaryKey()) {
            FXLabel label = new FXLabel(I18nHelper.primaryKey());
            label.addClass("tag_primaryKey");
            labels.add(label);
        }
        if (column.isUnsigned()) {
            FXLabel label = new FXLabel(I18nHelper.unsigned());
            label.addClass("tag_unsigned");
            labels.add(label);
        }
        if (column.isZeroFill()) {
            FXLabel label = new FXLabel(I18nHelper.zeroFill());
            label.addClass("tag_zeroFill");
            labels.add(label);
        }
        return labels;
    }

    /**
     * 处理小数位
     *
     * @param node          节点
     * @param decimalDigits 小数位
     */
    public static void handlerDigits(Node node, Integer decimalDigits) {
        // 设置小数位
        if (decimalDigits != null && decimalDigits > 0 && node instanceof DecimalTextField textField) {
            textField.setScaleLen(decimalDigits);
        }
    }

    /**
     * 处理注释
     *
     * @param node    节点
     * @param comment 注释
     */
    public static void handlerComment(Node node, String comment) {
        if (comment == null) {
            return;
        }
        if (node instanceof TextInputControl control) {
            control.setPromptText(comment);
        // } else if (node instanceof CalendarPicker<?> control) {
        //     control.setPromptText(comment);
        }
    }

    /**
     * 处理注释
     *
     * @param node         节点
     * @param defaultValue 默认值
     */
    public static void handlerDefaultValue(Node node, Object defaultValue) {
        if (defaultValue == null) {
            return;
        }
        if (node instanceof DigitalTextField field) {
            field.setValue(defaultValue);
        } else if (node instanceof ComboBox comboBox) {
            comboBox.getSelectionModel().select(defaultValue);
        } else if (node instanceof TextInputControl control) {
            control.setText(defaultValue.toString());
        // } else if (node instanceof CalendarPicker<?> picker) {
        //     if (defaultValue instanceof CharSequence sequence) {
        //         if (StrUtil.equalsAnyIgnoreCase(sequence, "CURRENT_TIMESTAMP")) {
        //             picker.setNow();
        //         }
        //     }
        }
    }

    /**
     * 处理注释
     *
     * @param node         节点
     * @param defaultValue 默认值
     */
    public static void handlerExampleValue(Node node, Object defaultValue) {
        if (defaultValue == null) {
            return;
        }
        if (node instanceof DigitalTextField field) {
            field.setValue(defaultValue);
        } else if (node instanceof ChooseFileTextField textField) {
            textField.setData(defaultValue);
        } else if (node instanceof TextInputControl control) {
            control.setText(defaultValue.toString());
        }
    }
}
