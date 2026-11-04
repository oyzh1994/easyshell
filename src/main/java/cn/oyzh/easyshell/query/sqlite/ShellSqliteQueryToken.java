package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.fx.db.query.DBQueryToken;

/**
 * SQLite 查询词元。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryToken extends DBQueryToken {

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
        return this.isPossibilityObject();
    }

    /**
     * 是否可能是视图
     *
     * @return 结果
     */
    public boolean isPossibilityView() {
        return this.isPossibilityObject();
    }

    /**
     * 是否可能是函数
     *
     * @return 结果
     */
    public boolean isPossibilityFunction() {
        return this.isPossibilityObject();
    }

    /**
     * 是否可能是存储过程
     *
     * @return 结果
     */
    public boolean isPossibilityProcedure() {
        return this.isPossibilityObject();
    }

    /**
     * 是否可能是字段
     *
     * @return 结果
     */
    public boolean isPossibilityColumn() {
        return this.isPossibilityObject();
    }

    /**
     * 是否可能是数据库
     *
     * @return 结果
     */
    public boolean isPossibilityDatabase() {
        return '"' == this.getToken() || '[' == this.getToken() || ' ' == this.getToken();
    }

    private boolean isPossibilityObject() {
        return ' ' == this.getToken() || '"' == this.getToken() || '`' == this.getToken()
                || '[' == this.getToken() || ']' == this.getToken()
                || ',' == this.getToken() || '.' == this.getToken();
    }
}
