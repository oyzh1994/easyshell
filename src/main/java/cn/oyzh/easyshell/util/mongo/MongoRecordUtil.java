package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.fx.mongo.CodeTextFiled;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mongo.record.MongoRecordProperty;
import cn.oyzh.fx.editor.incubator.control.JsonTextFiled;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.gui.text.field.BooleanTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.DateTimeTextField;
import cn.oyzh.fx.gui.text.field.DecimalTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.menu.ContextMenuManager;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ControlUtil;
import com.alibaba.fastjson2.JSONObject;
import com.mongodb.client.FindIterable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonDbPointer;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author oyzh
 * @since 2024/7/17
 */
public class MongoRecordUtil {

    public static Node getNode(MongoRecordProperty property, Object object, MongoColumn column) {
        Node node;
        if (column.supportInt32()) {
            NumberTextField textField = new NumberTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#D7EED0")));
            node = textField;
        } else if (column.supportInt64()) {
            NumberTextField textField = new NumberTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#CBE8C3")));
            node = textField;
        } else if (column.supportDigits()) {
            DecimalTextField textField = new DecimalTextField();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#CDECFA")));
            node = textField;
        } else if (column.supportBinary()) {
            BinaryTextFiled textField = new BinaryTextFiled();
            if (object instanceof Binary binary) {
                textField.setValue(binary.getData());
            } else {
                textField.setValue(object);
            }
            textField.setBackground(ControlUtil.background(Color.valueOf("#FBF0D0")));
            node = textField;
        } else if (column.supportDate()) {
            DateTimeTextField textField = new DateTimeTextField();
            textField.setDateFormat(MongoUtil.DATE_FORMAT);
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#F1E1F5")));
            node = textField;
        } else if (column.supportBoolean()) {
            BooleanTextFiled textField = new BooleanTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#FCE1E4")));
            node = textField;
        } else if (column.supportList()) {
            JsonTextFiled textField = new JsonTextFiled();
            textField.setArray(true);
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#FDE5CF")));
            node = textField;
        } else if (column.supportObject()) {
            JsonTextFiled textField = new JsonTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#C9E4E8")));
            node = textField;
        } else if (column.supportCode()) {
            CodeTextFiled textField = new CodeTextFiled();
            textField.setValue(object);
            textField.setBackground(ControlUtil.background(Color.valueOf("#D4E0D0")));
            node = textField;
        } else if (column.supportObjectId()) {
            FXTextField textField = new FXTextField();
            textField.setBackground(ControlUtil.background(Color.valueOf("#FEE9E4")));
            textField.setValue(object);
            node = textField;
        } else {
            FXTextField textField = new FXTextField();
            textField.setBackground(ControlUtil.background(Color.valueOf("#FDD4D3")));
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

    public static String formatValue(Object object, MongoColumn column) {
        String val;
        if (column.supportInteger()) {
            val = NumberTextField.format(object);
        } else if (column.supportDigits()) {
            val = DecimalTextField.format(object);
        } else if (column.supportString()) {
            val = ClearableTextField.format(object);
        } else if (column.supportBinary()) {
            val = BinaryTextFiled.format(object);
        } else if (column.supportObject() || column.supportList()) {
            val = JsonTextFiled.format(object);
        } else if (column.supportDate()) {
            val = MongoUtil.DATE_FORMAT.format(object);
        } else if (column.supportBoolean()) {
            val = BooleanTextFiled.format(object);
        } else {
            val = ClearableTextField.format(object);
        }
        return val;
    }

    public static String nullPromptText() {
        return "(Null)";
    }

    /**
     * 计算合适的字段宽
     *
     * @param column 字段
     * @return 结果
     */
    public static double suitableColumnWidth(MongoColumn column) {
        if (column.is_id()) {
            return FontUtil.textWidth("a".repeat(40), FontManager.currentFont());
        }
        String str1 = column.getName();
        String str2 = column.getType();
        double w1 = FontUtil.textWidth(str1, FontManager.currentFont());
        double w2 = FontUtil.textWidth(str2, FontManager.currentFont());
        double w3 = Math.max(w1, w2);
        return w3 + 50;
    }

    //    public static ContextMenu getColumnContextMenu(MongoRecordProperty property) {
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
    public static List<FXMenuItem> getColumnMenuItem(MongoRecordProperty property) {
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem copy = MenuItemHelper.copy_no_graphic(property::vCopy);
        menuItems.add(copy);
        FXMenuItem paste = MenuItemHelper.paste_no_graphic(property::vPaste);
        menuItems.add(paste);
        FXMenuItem setToNull = MenuItemHelper.setToNull_no_graphic(property::vSetToNull);
        menuItems.add(setToNull);
        FXMenuItem setToEmptyString = MenuItemHelper.setToEmptyString_no_graphic(property::vSetToEmptyString);
        menuItems.add(setToEmptyString);
        FXMenuItem copyAsInsertScript = MenuItemHelper.copyAsInsertScript_no_graphic(property::vCopyAsInsertSql);
        menuItems.add(copyAsInsertScript);
        FXMenuItem copyAsUpdateScript = MenuItemHelper.copyAsUpdateScript_no_graphic(property::vCopyAsUpdateSql);
        menuItems.add(copyAsUpdateScript);
        return menuItems;
    }

    /**
     * 文档转换为记录
     *
     * @param doc            文档
     * @param dbName         数据库名称
     * @param collectionName 集合名称
     * @return 结果
     */
    public static MongoRecord docToRecord(String doc, String dbName, String collectionName) {
        JSONObject object = JSONObject.parseObject(doc);
        MongoColumns columns = new MongoColumns();
        for (String col : object.keySet()) {
            MongoColumn column = new MongoColumn(col);
            column.setDbName(dbName);
            column.setCollectionName(collectionName);
            columns.add(column);
        }
        MongoRecord record = new MongoRecord(columns);
        for (MongoColumn column : columns) {
            Object value = object.get(column.getName());
            record.putValue(column, value);
            column.setType(MongoUtil.getType(value));
        }
        return record;
    }

    /**
     * 判断是否集合
     *
     * @param name 名称
     * @return 结果
     */
    public static boolean isCollection(String name) {
        return !StringUtil.endWithAny(name, ".files", ".chunks") && StringUtil.notEquals(name, MongoUtil.SYSTEM_JS);
    }

    /**
     * 判断是否存储桶
     *
     * @param name 名称
     * @return 结果
     */
    public static boolean isBucket(String name) {
        return StringUtil.endsWith(name, ".files");
    }

    /**
     * 获取id的值
     *
     * @param value 值
     * @return 结果
     */
    public static Object idValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof ObjectId id) {
            return id.toHexString();
        }
        if (value instanceof BsonValue bsonValue) {
            if (bsonValue.isString()) {
                return bsonValue.asString().getValue();
            }
            if (bsonValue instanceof BsonObjectId) {
                return idValue(bsonValue.asObjectId().getValue());
            }
            if (bsonValue instanceof BsonDbPointer) {
                return idValue(bsonValue.asDBPointer().getId());
            }
            if (bsonValue instanceof BsonBinary) {
                return TextUtil.byteToBitStr(bsonValue.asBinary().getData());
            }
            if (bsonValue instanceof BsonBoolean) {
                return Boolean.toString(bsonValue.asBoolean().getValue());
            }
            if (bsonValue instanceof BsonNull) {
                return "null";
            }
            if (bsonValue.isDouble()) {
                return bsonValue.asDouble().getValue();
            }
            if (bsonValue.isInt32()) {
                return bsonValue.asInt32().getValue();
            }
            if (bsonValue.isInt64()) {
                return bsonValue.asInt64().getValue();
            }
            if (bsonValue.isDecimal128()) {
                return bsonValue.asDecimal128().getValue();
            }
            if (bsonValue.isTimestamp()) {
                return bsonValue.asTimestamp().getValue();
            }
            if (bsonValue.isDateTime()) {
                return bsonValue.asDateTime().getValue();
            }
        }
        return value.toString();
    }

    /**
     * 根据记录，获取字段列表
     *
     * @param records 记录
     * @return 字段列表
     */
    public static MongoColumns columns(List<MongoRecord> records) {
        MongoColumns columns = new MongoColumns();
        for (MongoRecord record : records) {
            for (String column : record.columns()) {
                if (columns.column(column) == null) {
                    columns.add(record.column(column));
                }
            }
        }
        return columns;
    }

    /**
     * 文档转为记录
     *
     * @param dbName         数据库名称
     * @param collectionName 集合名称
     * @param iterable       迭代器
     * @return 结果
     */
    public static List<MongoRecord> docToRecord(String dbName, String collectionName, FindIterable<Document> iterable) {
        List<MongoRecord> records = new ArrayList<>();
        for (Document document : iterable) {
            MongoRecord record = docToRecord(dbName, collectionName, document);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    /**
     * 文档转为记录
     *
     * @param dbName         数据库名称
     * @param collectionName 集合名称
     * @param document       文档
     * @return 结果
     */
    public static MongoRecord docToRecord(String dbName, String collectionName, Document document) {
        Set<String> cols = document.keySet();
        if (!cols.isEmpty()) {
            MongoColumns columns = new MongoColumns();
            MongoRecord record = new MongoRecord(columns);
            for (String col : cols) {
                Object val = document.get(col);
                MongoColumn column = new MongoColumn();
                column.setName(col);
                column.setDbName(dbName);
                column.setCollectionName(collectionName);
                column.setType(MongoUtil.getType(val));
                columns.add(column);
                record.putValue(column, val);
            }
            return record;
        }
        return null;
    }

}
