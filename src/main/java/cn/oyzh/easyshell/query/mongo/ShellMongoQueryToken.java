package cn.oyzh.easyshell.query.mongo;


import cn.oyzh.fx.db.query.DBQueryToken;

import java.util.Objects;

/**
 * mongo查询token
 *
 * @author oyzh
 * @since 2024/8/15
 */
public class ShellMongoQueryToken extends DBQueryToken {

    //    /**
    //     * 结束位置
    //     */
    //    private int endIndex;
    //
    //    /**
    //     * 开始位置
    //     */
    //    private int startIndex;
    //
    //    /**
    //     * 内容
    //     */
    //    private String content;
    //
    //    /**
    //     * 1 空格
    //     * 2 .
    //     * 3 "
    //     */
    //    private Character token;
    //
    //    public boolean isEmpty() {
    //        return StringUtil.isEmpty(this.content);
    //    }
    //
    //    public boolean isNotEmpty() {
    //        return StringUtil.isNotEmpty(this.content);
    //    }

    /**
     * 是否可能是关键字
     *
     * @return 结果
     */
    public boolean isPossibilityKeyword() {
        return Objects.equals(' ', this.getToken()) || Character.isWhitespace(this.getToken()) || Objects.equals('\n', this.getToken());
    }

    /**
     * 是否可能是函数
     *
     * @return 结果
     */
    public boolean isPossibilityFunction() {
        return Objects.equals('.', this.getToken());
    }

    /**
     * 是否可能是集合
     *
     * @return 结果
     */
    public boolean isPossibilityCollection() {
        return Objects.equals('"', this.getToken()) || Objects.equals('\'', this.getToken());
    }

    //    public int getEndIndex() {
    //        return endIndex;
    //    }
    //
    //    public void setEndIndex(int endIndex) {
    //        this.endIndex = endIndex;
    //    }
    //
    //    public int getStartIndex() {
    //        return startIndex;
    //    }
    //
    //    public void setStartIndex(int startIndex) {
    //        this.startIndex = startIndex;
    //    }
    //
    //    public String getContent() {
    //        return content;
    //    }
    //
    //    public void setContent(String content) {
    //        this.content = content;
    //    }
    //
    //    public Character getToken() {
    //        return token;
    //    }
    //
    //    public void setToken(Character token) {
    //        this.token = token;
    //    }
}
