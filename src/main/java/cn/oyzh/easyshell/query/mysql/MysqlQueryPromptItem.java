package cn.oyzh.easyshell.query.mysql;

import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

/**
 * 查询提示内容
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class MysqlQueryPromptItem {

    /**
     * 类型
     * 1 database
     * 2 table
     * 3 column
     * 4 keyword
     * 5 view
     * 6 function
     * 7 procedure
     */
    private byte type;

    /**
     * 内容
     */
    private String content;

    /**
     * 相关度
     */
    private double correlation;

    /**
     * 额外内容
     */
    private String extContent;

    /**
     * 是否数据库类型
     *
     * @return 结果
     */
    public boolean isDatabaseType() {
        return 1 == this.type;
    }

    /**
     * 是否表类型
     *
     * @return 结果
     */
    public boolean isTableType() {
        return 2 == this.type;
    }

    /**
     * 是否字段类型
     *
     * @return 结果
     */
    public boolean isColumnType() {
        return 3 == this.type;
    }

    /**
     * 是否关键字类型
     *
     * @return 结果
     */
    public boolean isKeywordType() {
        return 4 == this.type;
    }

    /**
     * 是否视图类型
     *
     * @return 结果
     */
    public boolean isViewType() {
        return 5 == this.type;
    }

    /**
     * 是否函数类型
     *
     * @return 结果
     */
    public boolean isFunctionType() {
        return 6 == this.type;
    }

    /**
     * 是否过程类型
     *
     * @return 结果
     */
    public boolean isProcedureType() {
        return 7 == this.type;
    }

    public String wrapContent( ) {
        if(this.isColumnType()){
            return ShellMysqlUtil.wrap(this.content, DBDialect.MYSQL);
        }
        return this.content;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(double correlation) {
        this.correlation = correlation;
    }

    public String getExtContent() {
        return extContent;
    }

    public void setExtContent(String extContent) {
        this.extContent = extContent;
    }
}
