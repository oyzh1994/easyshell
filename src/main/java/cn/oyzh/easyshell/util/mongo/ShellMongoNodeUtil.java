package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mongo.ShellMongoCodeTextFiled;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.util.DBNodeUtil;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * mongodb节点工具类
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoNodeUtil {

    public static Object getNodeVal(Node node) {
//        Object val = null;
//        if (node instanceof NumberTextField textField) {
//            val = textField.getValue();
//        } else if (node instanceof DecimalTextField textField) {
//            val = textField.getValue();
//        } else if (node instanceof DateTimeTextField textField) {
//            val = textField.getValue();
//        } else if (node instanceof BinaryTextFiled textField) {
//            val = textField.getValue();
//        } else if (node instanceof BooleanTextFiled textField) {
//            val = textField.getValue();
//            //        } else if (node instanceof ShellMongoCodeTextFiled textField) {
//            //            val = textField.getValue();
//        } else if (node instanceof JsonTextFiled textField) {
//            val = textField.getValue();
//        } else if (node instanceof TextField textField) {
//            val = textField.getText();
//        }
        return DBNodeUtil.getNodeVal(node);
    }

    /**
     * 获取节点值并按 Mongo 列类型还原 BSON 类型。
     *
     * @param node   节点
     * @param column 字段
     * @return 节点值
     */
    public static Object getNodeValue(Node node, MongoColumn column) {
        Object value = getNodeVal(node);
        return normalizeValue(value, column);
    }

    /**
     * 按 Mongo 列类型规范化编辑值，避免 BigInteger、字符串等类型直接写入 Mongo。
     *
     * @param value  编辑值
     * @param column 字段
     * @return 规范化后的值
     */
    public static Object normalizeValue(Object value, MongoColumn column) {
        if (column == null) {
            return value;
        }
        String type = column.getType();
        if ("minKey".equalsIgnoreCase(type)) {
            return new MinKey();
        }
        if ("maxKey".equalsIgnoreCase(type)) {
            return new MaxKey();
        }
        if ("code".equalsIgnoreCase(type)) {
            if (value instanceof Code code) {
                return code.getCode() == null ? new Code("") : code;
            }
            if (value instanceof CharSequence text) {
                return new Code(text.toString());
            }
        }
        if (value instanceof String text) {
            if (column.supportObjectId() && StringUtil.isNotBlank(text)) {
                return new ObjectId(text);
            }
            if ("decimal".equalsIgnoreCase(type) && StringUtil.isNotBlank(text)) {
                return Decimal128.parse(text);
            }
            if ("regex".equalsIgnoreCase(type)) {
                return ShellMongoDataUtil.parseRegex(text);
            }
        }
        if (value instanceof BigInteger integer) {
            if ("int".equalsIgnoreCase(type)) {
                return integer.intValue();
            }
            if ("long".equalsIgnoreCase(type)) {
                if (integer.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0
                        && integer.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                    return integer.longValue();
                }
                return Decimal128.parse(integer.toString());
            }
            if ("decimal".equalsIgnoreCase(type)) {
                return Decimal128.parse(integer.toString());
            }
            if ("double".equalsIgnoreCase(type)) {
                return integer.doubleValue();
            }
        }
        if ("decimal".equalsIgnoreCase(type) && value instanceof Number number) {
            return Decimal128.parse(number.toString());
        }
        return value;
    }

    public static void setNodeVal(Node node, Object val) {
        val = ShellMongoDataUtil.valueStandardization(val);
        DBNodeUtil.setNodeVal(node, val);
        //        if (node == null || val == null) {
        //            return;
        //        }
        //        if (node instanceof NumberTextField textField) {
        //            textField.setValue(val);
        //        } else if (node instanceof DateTimeTextField textField) {
        //            textField.setValue(val);
        //        } else if (node instanceof BinaryTextFiled textField) {
        //            textField.setValue(val);
        //        } else if (node instanceof BooleanTextFiled textField) {
        //            textField.setValue(val);
        //        } else if (node instanceof JsonTextFiled textField) {
        //            textField.setValue(val);
        //        } else if (node instanceof TextField textField) {
        //            textField.setText(val.toString());
        //        }

    }

    public static Node generateNode(MongoColumn column) {
        Node node;
        if (column.supportInteger() || column.supportBigInteger()) {
            node = new NumberTextField();
        } else if ("decimal".equalsIgnoreCase(column.getType())) {
            node = new FXTextField();
        } else if (column.supportDigits()) {
            node = new DecimalTextField();
        } else if (column.supportTimestamp()) {
            node = new DateTimeTextField();
        } else if (column.supportBinary()) {
            node = new BinaryTextFiled();
        } else if (column.supportCode()) {
            node = new ShellMongoCodeTextFiled();
        } else if (column.supportJson()) {
            node = new JsonTextFiled();
        } else if (column.supportJsonArray()) {
            JsonTextFiled filed = new JsonTextFiled();
            filed.setArray(true);
            node = filed;
            //        } else if (column.supportCode()) {
            //            node = new ShellMongoCodeTextFiled();
        } else if (column.supportBoolean()) {
            node = new BooleanTextFiled();
        } else {
            node = new FXTextField();
        }
        node.setId("value");
        return node;
    }

    /**
     * 生成字段标签
     *
     * @param column 字段信息
     * @return 标签
     */
    public static List<FXLabel> generateTags(MongoColumn column) {
        List<FXLabel> labels = new ArrayList<>();
        // 主键
        if (column.is_id()) {
            FXLabel label = new FXLabel(I18nHelper.primaryKey());
            label.addClass("tag_primaryKey");
            labels.add(label);
        }
        // 嵌套字段
        if (column.getName() != null && column.getName().contains(".")) {
            FXLabel label = new FXLabel(ShellI18nHelper.mongoNestedField());
            label.addClass("tag_unsigned");
            labels.add(label);
        }
        return labels;
    }

    // /**
    //  * 设置节点内容为null字符串
    //  *
    //  * @param node 节点
    //  */
    // public static void setToNullString(Node node) {
    //     if (node instanceof TextField textField) {
    //         textField.clear();
    //         textField.setPromptText(ShellDBRecordUtil.nullPromptText());
    //         NodeUtil.unFocus(node);
    //     }
    // }
    //
    // /**
    //  * 设置节点内容为空字符串
    //  *
    //  * @param node 节点
    //  */
    // public static void setToEmptyString(Node node) {
    //     if (node instanceof TextField textField) {
    //         textField.setText("");
    //         textField.setPromptText("");
    //         NodeUtil.unFocus(node);
    //     }
    // }

}
