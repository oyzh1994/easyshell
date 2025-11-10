package cn.oyzh.easyshell.mysql.routine;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.fx.mysql.DBCharsetComboBox;
import cn.oyzh.easyshell.fx.mysql.DBCollationComboBox;
import cn.oyzh.easyshell.fx.mysql.routine.MysqlParamModeComboBox;
import cn.oyzh.easyshell.fx.mysql.table.DBEnumTextFiled;
import cn.oyzh.easyshell.fx.mysql.table.MysqlFiledTypeComboBox;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/1
 */
public class MysqlRoutineParam extends DBObjectStatus {

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
     * 值
     */
    private String value;

    /**
     * 字符集
     */
    private final StringProperty charsetProperty = new SimpleStringProperty();

    /**
     * 排序
     */
    private String collation;

    public String getType() {
        return this.typeProperty.get();
    }

    public void setType(String type) {
        this.typeProperty.set(type);
        this.putOriginalData("type", type);
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
    public MysqlFiledTypeComboBox getTypeControl() {
        MysqlFiledTypeComboBox comboBox = new MysqlFiledTypeComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setType(newValue));
        comboBox.selectFirstIfNull(this.getType());
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    private DBCharsetComboBox charsetControl;

    /**
     * 获取字符集组件
     *
     * @return 字符集组件
     */
    public DBCharsetComboBox getCharsetControl() {
        if (this.charsetControl != null) {
            return this.charsetControl;
        }
        ShellMysqlClient dbClient = CacheHelper.get("dbClient");
        DBCharsetComboBox comboBox = new DBCharsetComboBox();
        this.charsetControl = comboBox;
        comboBox.init(dbClient);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setCharset(newValue));
        comboBox.select(this.getCharset());
        // Runnable func = () -> {
        //     if (ShellMysqlColumnUtil.supportCharset(this.getType())) {
        //         comboBox.enable();
        //     } else {
        //         comboBox.disable();
        //         comboBox.clearSelection();
        //     }
        // };
        // this.typeProperty.addListener((observable, oldValue, newValue) -> func.run());
        // func.run();
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

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
        //     if (ShellMysqlColumnUtil.supportDigits(this.getType())) {
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
        //     if (ShellMysqlColumnUtil.supportSize(this.getType())) {
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

    private DBCollationComboBox collationControl;

    /**
     * 获取排序组件
     *
     * @return 排序组件
     */
    public DBCollationComboBox getCollationControl() {
        if (this.collationControl != null) {
            return collationControl;
        }
        ShellMysqlClient dbClient = CacheHelper.get("dbClient");
        DBCollationComboBox comboBox = new DBCollationComboBox();
        this.collationControl = comboBox;
        comboBox.init(this.getCharset(), dbClient);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setCollation(newValue));
        comboBox.select(this.getCollation());
        // this.charsetProperty.addListener((observable, oldValue, newValue) -> {
        //     comboBox.init(newValue, dbClient);
        //     comboBox.selectFirst();
        // });
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取模式组件
     *
     * @return 模式组件
     */
    public MysqlParamModeComboBox getModeControl() {
        MysqlParamModeComboBox comboBox = new MysqlParamModeComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setMode(newValue));
        comboBox.selectFirstIfNull(this.getMode());
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public boolean isReturnParam() {
        return StringUtil.isBlank(this.name) && StringUtil.isBlank(this.mode);
    }

    /**
     * 获取字段定义
     *
     * @return 字段定义
     */
    public String getDefinition() {
        String definition = "";
        if (this.getMode() != null) {
            definition += this.getMode() + " ";
        }
        if (StringUtil.isNotBlank(this.getName())) {
            definition += ShellMysqlUtil.wrap(this.getName(), DBDialect.MYSQL);
        }
        definition += " " + this.getType();
        definition += " (";
        if (ShellMysqlColumnUtil.supportSize(this.getType()) && this.getSize() != null) {
            definition += this.getSize();
            if (ShellMysqlColumnUtil.supportDigits(this.getType()) && this.getDigits() != null) {
                definition += "," + this.getDigits();
            }
        }
        if (ShellMysqlColumnUtil.supportValue(this.getType()) && this.getValue() != null) {
            definition += this.getValue();
        }
        definition += ")";
        definition = definition.replaceFirst("\\(\\)", "");
        // 字符集、排序
        if (ShellMysqlColumnUtil.supportCharset(this.getType())) {
            if (StringUtil.isNotBlank(this.getCharset())) {
                definition += " CHARSET " + this.getCharset();
            }
            if (StringUtil.isNotBlank(this.getCollation())) {
                definition += " COLLATE " + this.getCollation();
            }
        }
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
            if (ShellMysqlColumnUtil.supportEnum(type)) {
                this.setValue(sub1);
            } else if (ShellMysqlColumnUtil.supportDigits(type) && sub1.contains(",")) {
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
    //     return ShellMysqlColumnUtil.supportDigits(this.getType());
    // }
    //
    // public boolean supportEnum() {
    //     return ShellMysqlColumnUtil.supportEnum(this.getType());
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

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        if (collation != null) {
            collation = collation.toUpperCase();
        }
        this.collation = collation;
        this.putOriginalData("collation", collation);
    }

    {
        ShellMysqlClient dbClient = CacheHelper.get("dbClient");
        if (dbClient != null) {
            // 类型变更
            this.typeProperty.addListener((observable, oldValue, newValue) -> {
                if (ShellMysqlColumnUtil.supportCharset(this.getType())) {
                    this.getCharsetControl().enable();
                    this.getCollationControl().enable();
                } else {
                    this.getCharsetControl().disable();
                    this.getCharsetControl().clearSelection();
                    this.getCollationControl().disable();
                    this.getCollationControl().clearSelection();
                }
                if (ShellMysqlColumnUtil.supportDigits(this.getType())) {
                    this.getDigitsControl().enable();
                } else {
                    this.getDigitsControl().disable();
                    this.getDigitsControl().clear();
                }
                if (ShellMysqlColumnUtil.supportSize(this.getType())) {
                    this.getSizeControl().enable();
                } else {
                    this.getSizeControl().disable();
                    this.getSizeControl().clear();
                }
                if (ShellMysqlColumnUtil.supportValue(this.getType())) {
                    this.getValueControl().enable();
                } else {
                    this.getValueControl().disable();
                    this.getValueControl().clear();
                }
            });

            // 字符集变更
            this.charsetProperty.addListener((observable, oldValue, newValue) -> {
                this.getCollationControl().init(newValue, dbClient);
                this.getCollationControl().select(this.getCollation());
            });
        }
    }
}
