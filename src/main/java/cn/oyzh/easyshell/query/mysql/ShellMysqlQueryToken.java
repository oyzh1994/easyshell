package cn.oyzh.easyshell.query.mysql;


import cn.oyzh.easyshell.query.ShellQueryToken;

/**
 * @author oyzh
 * @since 2024/8/15
 */
public class ShellMysqlQueryToken extends ShellQueryToken {

    public boolean isPossibilityKeyword() {
        return ' ' == this.getToken() || '\n' == this.getToken() || '\0' == this.getToken();
    }

    public boolean isPossibilityTable() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    public boolean isPossibilityView() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    public boolean isPossibilityFunction() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    public boolean isPossibilityProcedure() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    public boolean isPossibilityColumn() {
        return ' ' == this.getToken() || '`' == this.getToken() || ',' == this.getToken() || '.' == this.getToken();
    }

    public boolean isPossibilityDatabase() {
        return '`' == this.getToken() || ' ' == this.getToken();
    }
}
