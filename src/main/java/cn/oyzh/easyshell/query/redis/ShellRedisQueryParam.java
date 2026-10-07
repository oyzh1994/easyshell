package cn.oyzh.easyshell.query.redis;


import java.util.ArrayList;
import java.util.List;

/**
 * redis查询参数
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisQueryParam {

    /**
     * db索引
     */
    private int dbIndex;

    /**
     * 内容
     */
    private String content;

    /**
     * 参数
     */
    private List<String> params;

    /**
     * 获取db索引
     *
     * @return db索引
     */
    public int getDbIndex() {
        return dbIndex;
    }

    /**
     * 设置db索引
     *
     * @param dbIndex db索引
     */
    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public List<String> getParams() {
        return params;
    }

    /**
     * 设置参数列表
     *
     * @param params 参数列表
     */
    public void setParams(List<String> params) {
        this.params = params;
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
        String[] arr = this.content.trim().split(" ");
        this.params = new ArrayList<>();
        for (String s : arr) {
            if (!s.isBlank()) {
                this.params.add(s);
            }
        }
    }

    /**
     * 获取命令
     *
     * @return 命令
     */
    public String getCommand() {
        return this.params.getFirst();
    }
}
