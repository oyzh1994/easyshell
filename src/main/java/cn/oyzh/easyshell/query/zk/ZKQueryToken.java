package cn.oyzh.easyshell.query.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.zk.ZKNodeUtil;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ZKQueryToken {

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
     * 1 null
     * 2 空格
     */
    private Character token;

    public boolean isEmpty() {
        return StringUtil.isEmpty(this.content);
    }

    public boolean isNotEmpty() {
        return StringUtil.isNotEmpty(this.content);
    }

    public boolean isPossibilityKeyword() {
        return this.token == null;
    }

    public boolean isPossibilityNode() {
        return this.token != null && this.isNotEmpty() && this.token == ' ';
    }

    public boolean isPossibilityParam() {
        return this.token != null && this.token == '-';
    }

    public String getPath() {
        if (this.content.startsWith("/")) {
            return ZKNodeUtil.getParentPath(this.content);
        }
        return null;
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
