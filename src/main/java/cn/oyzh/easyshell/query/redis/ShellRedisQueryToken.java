package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.util.StringUtil;
import redis.clients.jedis.Protocol;

import java.util.List;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellRedisQueryToken {
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

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
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

    /**
     * 结束位置
     */
    private int endIndex;

    /**
     * 开始位置
     */
    private int startIndex;

    /**
     * 输入
     */
    private String input;

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
        return this.token == null || this.token == ' ';
    }

    public boolean isPossibilityParam() {
        return this.token != null && this.token == ' ';
    }

    public boolean isPossibilityKey() {
        if (this.token != null && this.token == ' ') {
            List<Protocol.Command> commands = ShellRedisQueryUtil.keyCommands();
            for (Protocol.Command command : commands) {
                if (StringUtil.startWithIgnoreCase(this.input, command.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
