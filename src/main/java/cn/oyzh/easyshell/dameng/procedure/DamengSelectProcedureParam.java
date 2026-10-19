package cn.oyzh.easyshell.dameng.procedure;

/**
 * 达梦查询存储过程参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengSelectProcedureParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 存储过程名称
     */
    private String procedureName;

    /**
     * 是否查询完整信息
     *
     * @return 结果
     */
    public boolean isFull() {
        return full;
    }

    /**
     * 设置是否查询完整信息
     *
     * @param full 是否查询完整信息
     */
    public void setFull(boolean full) {
        this.full = full;
    }

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
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String getProcedureName() {
        return procedureName;
    }

    /**
     * 设置存储过程名称
     *
     * @param procedureName 存储过程名称
     */
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
}
