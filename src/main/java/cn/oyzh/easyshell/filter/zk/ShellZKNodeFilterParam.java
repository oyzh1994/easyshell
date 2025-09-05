package cn.oyzh.easyshell.filter.zk;


/**
 * zk节点过滤参数
 *
 * @author oyzh
 * @since 2025/01/22
 */
public class ShellZKNodeFilterParam {

    /**
     * 匹配大小写
     */
    private boolean matchCase;

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

    public boolean isSearchPath() {
        return searchPath;
    }

    public void setSearchPath(boolean searchPath) {
        this.searchPath = searchPath;
    }

    public boolean isSearchData() {
        return searchData;
    }

    public void setSearchData(boolean searchData) {
        this.searchData = searchData;
    }

    /**
     * 匹配全文
     */
    private boolean matchFull;

    /**
     * 搜索路径
     */
    private boolean searchPath = true;

    /**
     * 搜索数据
     */
    private boolean searchData = true;

    @Override
    public boolean equals(Object param) {
        if (param == this) {
            return true;
        }
        if (param instanceof ShellZKNodeFilterParam searchParam) {
            if (searchParam.matchCase && !this.matchCase) {
                return false;
            }
            if (searchParam.matchFull && !this.matchFull) {
                return false;
            }
            if (searchParam.searchData && !this.searchData) {
                return false;
            }
            return !searchParam.searchPath || this.searchPath;
        }
        return false;
    }
}
