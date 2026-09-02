package cn.oyzh.easyshell.dameng.procedure;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengAlertProcedureParam {

    private String schema;

    private DamengProcedure procedure;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public DamengProcedure getProcedure() {
        return procedure;
    }

    public void setProcedure(DamengProcedure procedure) {
        this.procedure = procedure;
    }

    public String getProcedureName() {
        return this.procedure.getName();
    }

    public void setProcedureName(String functionName) {
        this.procedure.setName(functionName);
    }
}
