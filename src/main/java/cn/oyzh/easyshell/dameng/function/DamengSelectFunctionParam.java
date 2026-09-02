package cn.oyzh.easyshell.dameng.function;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectFunctionParam {

    private boolean full;

    private String schema;

    private String functionName;

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
