package cn.oyzh.easyshell.dameng.routine;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.fx.dameng.routine.DamengParamModeComboBox;
import cn.oyzh.easyshell.fx.dameng.table.DBEnumTextFiled;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjectStatus;
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
 * @author oyzh
 * @since 2024/7/1
 */
public class DamengRoutineParam extends DBObjectStatus {

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

    public String getType() {
        return this.typeProperty.get();
    }

    public void setType(String type) {
        this.typeProperty.set(type);
        this.putOriginalData("type", type);
    }

    public Integer getPosition() {
        return this.position;
    }

    public void setPosition(Integer position) {
        this.position = position;
        this.putOriginalData("position", position);
    }

    public String getCharset() {
        return this.charsetProperty.get();
    }

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

    private DBEnumTextFiled valueControl;

    /**
     * 获取值组件
     *
     * @return 值组件
     */
    public DBEnumTextFiled getValueControl() {
        if (this.valueControl != null) {
            return this.valueControl;
        }
        DBEnumTextFiled textField = new DBEnumTextFiled();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.putOriginalData("name", name);
    }

    public StringProperty typeProperty() {
        return typeProperty;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        this.putOriginalData("mode", mode);
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
        this.putOriginalData("size", size);
    }

    public Integer getDigits() {
        return digits;
    }

    public void setDigits(Integer digits) {
        this.digits = digits;
        this.putOriginalData("digits", digits);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.putOriginalData("value", value);
    }

    public StringProperty charsetProperty() {
        return charsetProperty;
    }

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
