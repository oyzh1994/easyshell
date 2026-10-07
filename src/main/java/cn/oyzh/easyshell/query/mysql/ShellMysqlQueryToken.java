package cn.oyzh.easyshell.query.mysql;


import cn.oyzh.fx.db.query.DBQueryToken;

/**
 * mysql查询token
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellMysqlQueryToken extends DBQueryToken {

    /**
     * 是否可能是关键字
     *
     * @return 结果
     */
    public boolean isPossibilityKeyword() {
        return ' ' == this.getToken() || '\n' == this.getToken() || '\0' == this.getToken();
    }

    /**
     * 是否可能是表
     *
     * @return 结果
     */
    public boolean isPossibilityTable() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    /**
     * 是否可能是视图
     *
     * @return 结果
     */
    public boolean isPossibilityView() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    /**
     * 是否可能是函数
     *
     * @return 结果
     */
    public boolean isPossibilityFunction() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    /**
     * 是否可能是存储过程
     *
     * @return 结果
     */
    public boolean isPossibilityProcedure() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    /**
     * 是否可能是字段
     *
     * @return 结果
     */
    public boolean isPossibilityColumn() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    /**
     * 是否可能是数据库
     *
     * @return 结果
     */
    public boolean isPossibilityDatabase() {
        return '`' == this.getToken() || ' ' == this.getToken();
    }
}
