package cn.oyzh.easyshell.db.index;

/**
 * 索引查询参数
 * <p>各数据库统一使用此参数查询索引列表，便于索引列表、索引定义等逻辑复用。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellSelectIndexParam {

    /**
     * 作用域名称（数据库名称或模式名称）
     */
    private String scope;

    /**
     * 索引名称，为空表示查询全部
     */
    private String indexName;

    /**
     * 构造索引查询参数
     */
    public ShellSelectIndexParam() {
    }

    /**
     * 构造索引查询参数
     *
     * @param scope 作用域名称（数据库名称或模式名称）
     */
    public ShellSelectIndexParam(String scope) {
        this(scope, null);
    }

    /**
     * 构造索引查询参数
     *
     * @param scope     作用域名称（数据库名称或模式名称）
     * @param indexName 索引名称，为空表示查询全部
     */
    public ShellSelectIndexParam(String scope, String indexName) {
        this.scope = scope;
        this.indexName = indexName;
    }

    /**
     * 获取作用域名称
     *
     * @return 作用域名称
     */
    public String getScope() {
        return scope;
    }

    /**
     * 设置作用域名称
     *
     * @param scope 作用域名称
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * 获取索引名称
     *
     * @return 索引名称
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * 设置索引名称
     *
     * @param indexName 索引名称
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
