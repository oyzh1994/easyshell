package cn.oyzh.easyshell.filter.mysql;


/**
 * mysql过滤参数
 *
 * @author oyzh
 * @since 2025/11/07
 */
public class ShellMysqlDataFilterParam {

    /**
     * 匹配大小写
     */
    private boolean matchCase;

    /**
     * 匹配全文
     */
    private boolean matchFull;

    @Override
    public boolean equals(Object param) {
        if (param == this) {
            return true;
        }
        if (param instanceof ShellMysqlDataFilterParam searchParam) {
            if (searchParam.matchCase && !this.matchCase) {
                return false;
            }
            if (searchParam.matchFull && !this.matchFull) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isMatchFull() {
        return matchFull;
    }

    public void setMatchFull(boolean matchFull) {
        this.matchFull = matchFull;
    }
}
