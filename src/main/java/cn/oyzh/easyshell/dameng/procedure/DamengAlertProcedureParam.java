package cn.oyzh.easyshell.dameng.procedure;

/**
 * 达梦修改存储过程参数
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengAlertProcedureParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 存储过程
     */
    private DamengProcedure procedure;

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
     * 获取存储过程
     *
     * @return 存储过程
     */
    public DamengProcedure getProcedure() {
        return procedure;
    }

    /**
     * 设置存储过程
     *
     * @param procedure 存储过程
     */
    public void setProcedure(DamengProcedure procedure) {
        this.procedure = procedure;
    }

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String getProcedureName() {
        return this.procedure.getName();
    }

    /**
     * 设置存储过程名称
     *
     * @param functionName 存储过程名称
     */
    public void setProcedureName(String functionName) {
        this.procedure.setName(functionName);
    }
}
