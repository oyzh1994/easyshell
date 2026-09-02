package cn.oyzh.easyshell.dameng.function;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengAlertFunctionParam {

    private String schema;

    private DamengFunction function;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public DamengFunction getFunction() {
        return function;
    }

    public void setFunction(DamengFunction function) {
        this.function = function;
    }

    public String getFunctionName() {
        return this.function.getName();
    }

    public void setFunctionName(String functionName) {
        this.function.setName(functionName);
    }
}
