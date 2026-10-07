package cn.oyzh.easyshell.dameng.routine;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.fx.dameng.routine.DamengParamModeComboBox;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.ui.DBFiledTypeComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 达梦存储程序参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengRoutineParam extends DBObject {

    /**
     * 数据库客户端
     */
    private ShellDamengClient dbClient;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private final StringProperty typeProperty = new SimpleStringProperty();

    /**
     * 模式
     */
    private String mode;

    /**
     * 长度
     */
    private Integer size;

    /**
     * 小数位
     */
    private Integer digits;

    /**
     * 位置
     */
    private Integer position;

    /**
     * 值
     */
    private String value;

    /**
     * 字符集
     */
    private final StringProperty charsetProperty = new SimpleStringProperty();

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return this.typeProperty.get();
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.typeProperty.set(type);
        this.putOriginalData("type", type);
    }

    /**
     * 获取位置
     *
     * @return 位置
     */
    public Integer getPosition() {
        return this.position;
    }

    /**
     * 设置位置
     *
     * @param position 位置
     */
    public void setPosition(Integer position) {
        this.position = position;
        this.putOriginalData("position", position);
    }

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    public String getCharset() {
        return this.charsetProperty.get();
    }

    /**
     * 设置字符集
     *
     * @param charset 字符集
     */
    public void setCharset(String charset) {
        if (charset != null) {
            charset = charset.toUpperCase();
        }
        this.charsetProperty.set(charset);
        this.putOriginalData("charset", charset);
    }

    /**
     * 获取名称组件
     *
     * @return 名称组件
     */
    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setFlexWidth("100% - 10");
        textField.setPromptText(I18nHelper.pleaseInputContent());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
        textField.setText(this.getName());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取类型组件
     *
     * @return 类型组件
     */
    public DBFiledTypeComboBox getTypeControl() {
        DBFiledTypeComboBox comboBox = new DBFiledTypeComboBox();
        comboBox.setDialect(DBDialect.DAMENG);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setType(newValue));
        comboBox.selectFirstIfNull(this.getType());
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }
//
//    private DBCharsetComboBox charsetControl;
//
//    /**
//     * 获取字符集组件
//     *
//     * @return 字符集组件
//     */
//    public DBCharsetComboBox getCharsetControl() {
//        if (this.charsetControl != null) {
//            return this.charsetControl;
//        }
//        //ShellDamengClient dbClient = CacheHelper.get("dameng:dbClient");
//        DBCharsetComboBox comboBox = new DBCharsetComboBox();
//        this.charsetControl = comboBox;
//        comboBox.init(this.dbClient);
//        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setCharset(newValue));
//        comboBox.select(this.getCharset());
//        TableViewUtil.selectRowOnMouseClicked(comboBox);
//        return comboBox;
//    }

    private NumberTextField digitsControl;

    /**
     * 获取小数位组件
     *
     * @return 小数位组件
     */
    public NumberTextField getDigitsControl() {
        if (this.digitsControl != null) {
            return this.digitsControl;
        }
        NumberTextField textField = new NumberTextField();
        this.digitsControl = textField;
        textField.setFlexWidth("100% - 12");
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setDigits(textField.getIntValue()));
        textField.setValue(this.getDigits());
        // Runnable func = () -> {
        //     if (DamengColumnUtil.supportDigits(this.getType())) {
        //         textField.enable();
        //     } else {
        //         textField.disable();
        //         textField.clear();
        //     }
        // };
        // this.typeProperty.addListener((observable, oldValue, newValue) -> func.run());
        // func.run();
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    private NumberTextField sizeControl;

    /**
     * 获取字段长度组件
     *
     * @return 字段长度组件
     */
    public NumberTextField getSizeControl() {
        if (this.sizeControl != null) {
            return this.sizeControl;
        }
        NumberTextField textField = new NumberTextField();
        this.sizeControl = textField;
        textField.setFlexWidth("100% - 12");
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setSize(textField.getIntValue()));
        textField.setValue(this.getSize());
        // Runnable func = () -> {
        //     if (DamengColumnUtil.supportSize(this.getType())) {
        //         textField.enable();
        //     } else {
        //         textField.disable();
        //         textField.clear();
        //     }
        // };
        // this.typeProperty.addListener((observable, oldValue, newValue) -> func.run());
        // func.run();
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public List<String> getValueList() {
        List<String> valueList = new ArrayList<>();
        if (this.getValue() != null) {
            List<String> list = StringUtil.split(this.getValue(), ",");
            for (String s : list) {
                if (s.startsWith("'") && s.endsWith("'")) {
                    valueList.add(s.substring(1, s.length() - 1));
                } else {
                    valueList.add(s);
                }
            }
        }
        return valueList;
    }

    private ShellDBEnumTextFiled valueControl;

    /**
     * 获取值组件
     *
     * @return 值组件
     */
    public ShellDBEnumTextFiled getValueControl() {
        if (this.valueControl != null) {
            return this.valueControl;
        }
        ShellDBEnumTextFiled textField = new ShellDBEnumTextFiled();
        this.valueControl = textField;
        textField.setFlexWidth("100% - 12");
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setValue(textField.getTextTrim()));
        textField.setValues(this.getValueList());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

//    private DBCollationComboBox collationControl;

    //    /**
    //     * 获取排序组件
    //     *
    //     * @return 排序组件
    //     */
    //    public DBCollationComboBox getCollationControl() {
    //        if (this.collationControl != null) {
    //            return collationControl;
    //        }
    //        //ShellDamengClient dbClient = CacheHelper.get("dameng:dbClient");
    //        DBCollationComboBox comboBox = new DBCollationComboBox();
    //        this.collationControl = comboBox;
    //        comboBox.init(this.getCharset(), this.dbClient);
    //        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setCollation(newValue));
    //        comboBox.select(this.getCollation());
    //        // this.charsetProperty.addListener((observable, oldValue, newValue) -> {
    //        //     comboBox.init(newValue, dbClient);
    //        //     comboBox.selectFirst();
    //        // });
    //        TableViewUtil.selectRowOnMouseClicked(comboBox);
    //        return comboBox;
    //    }

    /**
     * 获取模式组件
     *
     * @return 模式组件
     */
    public DamengParamModeComboBox getModeControl() {
        DamengParamModeComboBox comboBox = new DamengParamModeComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.mode = newValue);
        comboBox.selectFirstIfNull(this.mode);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 是否为返回值参数
     *
     * @return 结果
     */
    public boolean isReturnParam() {
        return StringUtil.equals(this.getName(), "V_RET") && StringUtil.equalsIgnoreCase(this.getMode(), "OUT") && Objects.equals(this.getPosition(), 0);
    }

    /**
     * 获取字段定义
     *
     * @return 字段定义
     */
    public String getDefinition() {
        String definition = "";
        if (StringUtil.isNotBlank(this.getName())) {
            definition += DBUtil.wrap(this.getName(), DBDialect.DAMENG);
        }
        if (StringUtil.isNotBlank(this.getMode())) {
            if (StringUtil.containsIgnoreCase(this.getMode(), "IN") && StringUtil.containsIgnoreCase(this.getMode(), "OUT")) {
                definition += " IN OUT ";
            } else if (StringUtil.containsIgnoreCase(this.getMode(), "IN")) {
                definition += " IN ";
            } else if (StringUtil.containsIgnoreCase(this.getMode(), "OUT")) {
                definition += " OUT ";
            }
        }
        definition += " " + this.getType();
        definition += " (";
        if (DBColumnFieldManager.supportSize(DBDialect.DAMENG, this.getType()) && this.getSize() != null) {
            definition += this.getSize();
            if (DBColumnFieldManager.supportDigits(DBDialect.DAMENG, this.getType()) && this.getDigits() != null) {
                definition += "," + this.getDigits();
            }
        }
        if (DBColumnFieldManager.supportValue(DBDialect.DAMENG, this.getType()) && this.getValue() != null) {
            definition += this.getValue();
        }
        definition += ")";
        definition = definition.replaceFirst("\\(\\)", "");
        return definition;
    }

    /**
     * 设置字段定义标识
     *
     * @param dtdIdentifier 字段定义标识
     */
    public void setDtdIdentifier(String dtdIdentifier) {
        String type;
        if (!dtdIdentifier.contains("(") && !dtdIdentifier.contains(" ")) {
            type = dtdIdentifier;
        } else if (!dtdIdentifier.contains("(")) {
            type = dtdIdentifier;
        } else {
            type = dtdIdentifier.substring(0, dtdIdentifier.indexOf("("));
            String sub1 = dtdIdentifier.substring(dtdIdentifier.indexOf("(") + 1, dtdIdentifier.lastIndexOf(")"));
            //            if (DamengColumnUtil.supportEnum(type)) {
            //                this.setValue(sub1);
            //            } else if (DamengColumnUtil.supportDigits(type) && sub1.contains(",")) {
            if (DBColumnFieldManager.supportDigits(DBDialect.DAMENG, type) && sub1.contains(",")) {
                String[] arr = sub1.split(",");
                this.setSize(Integer.parseInt(arr[0]));
                this.setDigits(Integer.parseInt(arr[1]));
            } else {
                this.setSize(Integer.parseInt(sub1));
            }
        }
        this.setType(type.toUpperCase());
    }

    // public boolean supportDigits() {
    //     return DamengColumnUtil.supportDigits(this.getType());
    // }
    //
    // public boolean supportEnum() {
    //     return DamengColumnUtil.supportEnum(this.getType());
    // }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
        this.putOriginalData("name", name);
    }

    /**
     * 获取类型属性
     *
     * @return 类型属性
     */
    public StringProperty typeProperty() {
        return typeProperty;
    }

    /**
     * 获取模式
     *
     * @return 模式
     */
    public String getMode() {
        return mode;
    }

    /**
     * 设置模式
     *
     * @param mode 模式
     */
    public void setMode(String mode) {
        this.mode = mode;
        this.putOriginalData("mode", mode);
    }

    /**
     * 获取长度
     *
     * @return 长度
     */
    public Integer getSize() {
        return size;
    }

    /**
     * 设置长度
     *
     * @param size 长度
     */
    public void setSize(Integer size) {
        this.size = size;
        this.putOriginalData("size", size);
    }

    /**
     * 获取小数位
     *
     * @return 小数位
     */
    public Integer getDigits() {
        return digits;
    }

    /**
     * 设置小数位
     *
     * @param digits 小数位
     */
    public void setDigits(Integer digits) {
        this.digits = digits;
        this.putOriginalData("digits", digits);
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(String value) {
        this.value = value;
        this.putOriginalData("value", value);
    }

    /**
     * 获取字符集属性
     *
     * @return 字符集属性
     */
    public StringProperty charsetProperty() {
        return charsetProperty;
    }

    /**
     * 设置数据库客户端
     *
     * @param dbClient 数据库客户端
     */
    public void setDbClient(ShellDamengClient dbClient) {
        if (this.dbClient != null) {
            return;
        }
        this.dbClient = dbClient;
        //ShellDamengClient dbClient = CacheHelper.get("dameng:dbClient");
        if (dbClient != null) {
            // 类型变更
            this.typeProperty.addListener((observable, oldValue, newValue) -> {
                //                if (DamengColumnUtil.supportCharset(this.getType())) {
                //                    this.getCharsetControl().enable();
                //                    this.getCollationControl().enable();
                //                } else {
                //                    this.getCharsetControl().disable();
                //                    this.getCharsetControl().clearSelection();
                //                    this.getCollationControl().disable();
                //                    this.getCollationControl().clearSelection();
                //                }
                if (DBColumnFieldManager.supportDigits(DBDialect.DAMENG, this.getType())) {
                    this.getDigitsControl().enable();
                } else {
                    this.getDigitsControl().disable();
                    this.getDigitsControl().clear();
                }
                if (DBColumnFieldManager.supportSize(DBDialect.DAMENG, this.getType())) {
                    this.getSizeControl().enable();
                } else {
                    this.getSizeControl().disable();
                    this.getSizeControl().clear();
                }
                if (DBColumnFieldManager.supportValue(DBDialect.DAMENG, this.getType())) {
                    this.getValueControl().enable();
                } else {
                    this.getValueControl().disable();
                    this.getValueControl().clear();
                }
            });

            //            // 字符集变更
            //            this.charsetProperty.addListener((observable, oldValue, newValue) -> {
            //                this.getCollationControl().init(newValue, dbClient);
            //                this.getCollationControl().select(this.getCollation());
            //            });
        }
    }
}
