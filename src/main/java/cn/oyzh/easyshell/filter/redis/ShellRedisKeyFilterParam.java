package cn.oyzh.easyshell.filter.redis;



/**
 * @author oyzh
 * @since 2025/01/24
 */
public class ShellRedisKeyFilterParam {
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

    public boolean isSearchKey() {
        return searchKey;
    }

    public void setSearchKey(boolean searchKey) {
        this.searchKey = searchKey;
    }

    public boolean isSearchData() {
        return searchData;
    }

    public void setSearchData(boolean searchData) {
        this.searchData = searchData;
    }

    /**
     * 匹配大小写
     */
    private boolean matchCase;

    /**
     * 匹配全文
     */
    private boolean matchFull;

    /**
     * 搜索键
     */
    private boolean searchKey = true;

    /**
     * 搜索数据
     */
    private boolean searchData = true;

    @Override
    public boolean equals(Object param) {
        if (param == this) {
            return true;
        }
        if (param instanceof ShellRedisKeyFilterParam searchParam) {
            if (searchParam.matchCase && !this.matchCase) {
                return false;
            }
            if (searchParam.matchFull && !this.matchFull) {
                return false;
            }
            if (searchParam.searchData && !this.searchData) {
                return false;
            }
            return !searchParam.searchKey || this.searchKey;
        }
        return false;
    }
}
