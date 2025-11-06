package cn.oyzh.easyshell.query.mysql;


import cn.oyzh.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2024/8/15
 */
public class MysqlQueryToken {

    /**
     * 结束位置
     */
    private int endIndex;

    /**
     * 开始位置
     */
    private int startIndex;

    /**
     * 内容
     */
    private String content;

    /**
     * 1 空格
     * 2 .
     * 3 `
     */
    private Character token;

    public boolean isEmpty() {
        return StringUtil.isEmpty(this.content);
    }

    public boolean isNotEmpty() {
        return StringUtil.isNotEmpty(this.content);
    }

    public boolean isPossibilityKeyword() {
        return ' ' == this.token || '\n' == this.token || '\0' == this.token;
    }

    public boolean isPossibilityTable() {
        return true;
    }

    public boolean isPossibilityView() {
        return true;
    }

    public boolean isPossibilityFunction() {
        return true;
    }

    public boolean isPossibilityProcedure() {
        return true;
    }

    public boolean isPossibilityColumn() {
        return true;
        // return '`' == this.token || '.' == this.token;
    }

    public boolean isPossibilityDatabase() {
        return '`' == this.token || ' ' == this.token;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Character getToken() {
        return token;
    }

    public void setToken(Character token) {
        this.token = token;
    }
}
