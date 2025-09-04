package cn.oyzh.easyshell.query.redis;


import java.util.ArrayList;
import java.util.List;

/**
 * zk查询参数
 *
 * @author oyzh
 * @since 2025/01/20
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

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public String getContent() {
        return content;
    }

    public List<String> getParams() {
        return params;
    }

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

    public String getCommand() {
        return this.params.getFirst();
    }
}
