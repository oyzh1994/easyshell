package cn.oyzh.easyshell.mysql.data;


import cn.oyzh.common.util.StringUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2024/09/02
 */
public class MysqlDataImportConfig {

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 导入模式
     * 1. 追加
     * 2. 复制
     */
    private String importMode = "2";

    /**
     * 字段索引
     */
    private int columnIndex = 0;

    /**
     * 数据起始索引
     */
    private int dataStartIndex = 1;

    /**
     * 字段标签
     */
    private String recordLabel;

    /**
     * 属性作为字段
     */
    private boolean attrToColumn;

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

    public boolean isAppendMode() {
        return StringUtil.equals(this.importMode, "1");
    }

    public boolean isCopyMode() {
        return StringUtil.equals(this.importMode, "2");
    }

    public char fieldSeparatorChar() {
        return this.fieldSeparator.charAt(0);
    }

    public char txtIdentifierChar() {
        return this.txtIdentifier.charAt(0);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getImportMode() {
        return importMode;
    }

    public void setImportMode(String importMode) {
        this.importMode = importMode;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getDataStartIndex() {
        return dataStartIndex;
    }

    public void setDataStartIndex(int dataStartIndex) {
        this.dataStartIndex = dataStartIndex;
    }

    public String getRecordLabel() {
        return recordLabel;
    }

    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
    }

    public boolean isAttrToColumn() {
        return attrToColumn;
    }

    public void setAttrToColumn(boolean attrToColumn) {
        this.attrToColumn = attrToColumn;
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
}
