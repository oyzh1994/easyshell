package cn.oyzh.easyshell.mongo.util;

import cn.oyzh.easyshell.fx.mongo.CodeTextFiled;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.node.NodeUtil;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.bson.types.Binary;

/**
 * db节点工具类
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MongoNodeUtil {

    public static Object getNodeVal(Node node) {
        Object val = null;
        if (node instanceof NumberTextField textField) {
            val = textField.getValue();
        } else if (node instanceof DecimalTextField textField) {
            val = textField.getValue();
        } else if (node instanceof DateTimeTextField textField) {
            val = textField.getValue();
        } else if (node instanceof BinaryTextFiled textField) {
            val = textField.getValue();
        } else if (node instanceof BooleanTextFiled textField) {
            val = textField.getValue();
        } else if (node instanceof CodeTextFiled textField) {
            val = textField.getValue();
        } else if (node instanceof JsonTextFiled textField) {
            val = textField.getValue();
        } else if (node instanceof TextField textField) {
            val = textField.getText();
        }
        return val;
    }

    public static void setNodeVal(Node node, Object val) {
        if (node == null || val == null) {
            return;
        }
        if (node instanceof NumberTextField textField) {
            textField.setValue(val);
        } else if (node instanceof DateTimeTextField textField) {
            textField.setValue(val);
        } else if (node instanceof BinaryTextFiled textField) {
            if (val instanceof Binary binary) {
                textField.setValue(binary.getData());
            } else {
                textField.setValue(val);
            }
        } else if (node instanceof BooleanTextFiled textField) {
            textField.setValue(val);
        } else if (node instanceof JsonTextFiled textField) {
            textField.setValue(val);
        } else if (node instanceof TextField textField) {
            textField.setText(val.toString());
        }
    }

    public static Node generateNode(MongoColumn column) {
        Node node;
        if (column.supportInteger()) {
            node = new NumberTextField();
        } else if (column.supportDigits()) {
            node = new DecimalTextField();
        } else if (column.supportDate()) {
            node = new DateTimeTextField();
        } else if (column.supportBinary()) {
            node = new BinaryTextFiled();
        } else if (column.supportList()) {
            JsonTextFiled filed = new JsonTextFiled();
            filed.setArray(true);
            node = filed;
        } else if (column.supportObject()) {
            node = new JsonTextFiled();
        } else if (column.supportCode()) {
            node = new CodeTextFiled();
        } else if (column.supportBoolean()) {
            node = new BooleanTextFiled();
        } else {
            node = new FXTextField();
        }
        node.setId("value");
        return node;
    }

    /**
     * 设置节点内容为null字符串
     *
     * @param node 节点
     */
    public static void setToNullString(Node node) {
        if (node instanceof TextField textField) {
            textField.clear();
            textField.setPromptText(MongoRecordUtil.nullPromptText());
            NodeUtil.unFocus(node);
        }
    }

    /**
     * 设置节点内容为空字符串
     *
     * @param node 节点
     */
    public static void setToEmptyString(Node node) {
        if (node instanceof TextField textField) {
            textField.setText("");
            textField.setPromptText("");
            NodeUtil.unFocus(node);
        }
    }

}
