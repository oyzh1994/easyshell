package cn.oyzh.easyshell.db.type;

/**
 * 类型查询参数
 * <p>各数据库统一使用此参数查询自定义类型列表，与 {@link cn.oyzh.easyshell.db.index.ShellSelectIndexParam} 保持一致。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellSelectTypeParam {

    /**
     * 作用域名称（模式名称）
     */
    private String scope;

    /**
     * 类型名称，为空表示查询全部
     */
    private String typeName;

    /**
     * 构造类型查询参数
     */
    public ShellSelectTypeParam() {
    }

    /**
     * 构造类型查询参数
     *
     * @param scope 作用域名称（模式名称）
     */
    public ShellSelectTypeParam(String scope) {
        this(scope, null);
    }

    /**
     * 构造类型查询参数
     *
     * @param scope    作用域名称（模式名称）
     * @param typeName 类型名称，为空表示查询全部
     */
    public ShellSelectTypeParam(String scope, String typeName) {
        this.scope = scope;
        this.typeName = typeName;
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
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 设置类型名称
     *
     * @param typeName 类型名称
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
