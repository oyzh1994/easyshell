package cn.oyzh.easyshell.dameng.function;

/**
 * 达梦修改函数参数
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengAlertFunctionParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 函数
     */
    private DamengFunction function;

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式名称
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取函数
     *
     * @return 函数
     */
    public DamengFunction getFunction() {
        return function;
    }

    /**
     * 设置函数
     *
     * @param function 函数
     */
    public void setFunction(DamengFunction function) {
        this.function = function;
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String getFunctionName() {
        return this.function.getName();
    }

    /**
     * 设置函数名称
     *
     * @param functionName 函数名称
     */
    public void setFunctionName(String functionName) {
        this.function.setName(functionName);
    }
}
