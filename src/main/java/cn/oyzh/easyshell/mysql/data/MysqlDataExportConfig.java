package cn.oyzh.easyshell.mysql.data;


import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2024/09/02
 */
public class MysqlDataExportConfig {

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 字段作为属性
     */
    private boolean fieldToAttr;

    /**
     * 包含列标题
     */
    private boolean includeFields = true;

    /**
     * 记录分割符号
     */
    private String recordSeparator = System.lineSeparator();

    /**
     * 字段分割符号
     */
    private String fieldSeparator = ";";

    /**
     * 文本识别符号
     */
    private String txtIdentifier = "\"";

    /**
     * 字符集
     */
    private String charset = StandardCharsets.UTF_8.displayName();

    /**
     * 早期版本
     */
    private boolean earlyVersion;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isFieldToAttr() {
        return fieldToAttr;
    }

    public void setFieldToAttr(boolean fieldToAttr) {
        this.fieldToAttr = fieldToAttr;
    }

    public boolean isIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(boolean includeFields) {
        this.includeFields = includeFields;
    }

    public String getRecordSeparator() {
        return recordSeparator;
    }

    public void setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getTxtIdentifier() {
        return txtIdentifier;
    }

    public void setTxtIdentifier(String txtIdentifier) {
        this.txtIdentifier = txtIdentifier;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isEarlyVersion() {
        return earlyVersion;
    }

    public void setEarlyVersion(boolean earlyVersion) {
        this.earlyVersion = earlyVersion;
    }
}
