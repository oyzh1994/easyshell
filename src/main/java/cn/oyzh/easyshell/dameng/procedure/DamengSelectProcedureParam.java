package cn.oyzh.easyshell.dameng.procedure;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectProcedureParam {

    private boolean full;

    private String schema;

    private String procedureName;

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

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
}
