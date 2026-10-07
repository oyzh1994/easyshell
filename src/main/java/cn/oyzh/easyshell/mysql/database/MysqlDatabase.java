package cn.oyzh.easyshell.mysql.database;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDatabse;
import javafx.beans.property.SimpleStringProperty;

/**
 * MySQL数据库
 *
 * @author oyzh
 * @since 2024/1/30
 */
public class MysqlDatabase implements DBDatabse {

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

    /**
     * 获取字符集属性
     *
     * @return 字符集属性
     */
    public SimpleStringProperty charsetProperty() {
        if (this.charsetProperty == null) {
            this.charsetProperty = new SimpleStringProperty();
        }
        return this.charsetProperty;
    }

    /**
     * 设置字符集
     *
     * @param charset 字符集
     */
    public void setCharset(String charset) {
        this.charsetProperty().setValue(charset);
    }

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    public String getCharset() {
        return this.charsetProperty == null ? null : this.charsetProperty.get();
    }

    /**
     * 获取排序规则属性
     *
     * @return 排序规则属性
     */
    public SimpleStringProperty collationProperty() {
        if (this.collationProperty == null) {
            this.collationProperty = new SimpleStringProperty();
        }
        return this.collationProperty;
    }

    /**
     * 设置排序规则
     *
     * @param collation 排序规则
     */
    public void setCollation(String collation) {
        this.collationProperty().setValue(collation);
    }

    /**
     * 获取排序规则
     *
     * @return 排序规则
     */
    public String getCollation() {
        return this.collationProperty == null ? null : this.collationProperty.get();
    }

    /**
     * 根据排序规则设置字符集与排序规则
     *
     * @param collation 排序规则
     */
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
