package cn.oyzh.easyshell.dto.mysql;

import cn.oyzh.common.util.StringUtil;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author oyzh
 * @since 2024/1/30
 */
public class MysqlDatabase {

    /**
     * 库名称
     */
    private String name;

    /**
     * 库字符集
     */
    private SimpleStringProperty charsetProperty;

    /**
     * 库排序规则
     */
    private SimpleStringProperty collationProperty;

    public SimpleStringProperty charsetProperty() {
        if (this.charsetProperty == null) {
            this.charsetProperty = new SimpleStringProperty();
        }
        return this.charsetProperty;
    }

    public void setCharset(String charset) {
        this.charsetProperty().setValue(charset);
    }

    public String getCharset() {
        return this.charsetProperty == null ? null : this.charsetProperty.get();
    }

    public SimpleStringProperty collationProperty() {
        if (this.collationProperty == null) {
            this.collationProperty = new SimpleStringProperty();
        }
        return this.collationProperty;
    }

    public void setCollation(String collation) {
        this.collationProperty().setValue(collation);
    }

    public String getCollation() {
        return this.collationProperty == null ? null : this.collationProperty.get();
    }

    public void setCharsetAndCollation(String collation) {
        if (StringUtil.isNotBlank(collation)) {
            String charset = collation.split("_")[0];
            this.setCharset(charset);
            if (collation.contains("_")) {
                this.setCollation(collation);
            } else {
                this.setCollation(null);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
