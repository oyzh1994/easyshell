package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.fx.db.query.DBSqlQueryToken;

/**
 * SqlServer查询token
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellSqlServerQueryToken extends DBSqlQueryToken {

    /**
     * 是否处于SQL Server方括号标识符上下文
     *
     * @return 结果
     */
    public boolean isOpenBracketContext() {
        return '[' == this.tokenChar();
    }
}
