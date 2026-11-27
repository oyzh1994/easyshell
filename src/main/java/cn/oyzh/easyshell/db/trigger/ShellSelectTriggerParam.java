package cn.oyzh.easyshell.db.trigger;

/**
 * 触发器查询参数
 * <p>各数据库统一使用此参数查询触发器列表，与 {@link cn.oyzh.easyshell.db.index.ShellSelectIndexParam} 保持一致。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellSelectTriggerParam {

    /**
     * 作用域名称（数据库名称或模式名称）
     */
    private String scope;

    /**
     * 触发器名称，为空表示查询全部
     */
    private String triggerName;

    /**
     * 构造触发器查询参数
     */
    public ShellSelectTriggerParam() {
    }

    /**
     * 构造触发器查询参数
     *
     * @param scope 作用域名称（数据库名称或模式名称）
     */
    public ShellSelectTriggerParam(String scope) {
        this(scope, null);
    }

    /**
     * 构造触发器查询参数
     *
     * @param scope       作用域名称（数据库名称或模式名称）
     * @param triggerName 触发器名称，为空表示查询全部
     */
    public ShellSelectTriggerParam(String scope, String triggerName) {
        this.scope = scope;
        this.triggerName = triggerName;
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
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    public String getTriggerName() {
        return triggerName;
    }

    /**
     * 设置触发器名称
     *
     * @param triggerName 触发器名称
     */
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
}
