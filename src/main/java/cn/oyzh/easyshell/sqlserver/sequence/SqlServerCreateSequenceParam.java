package cn.oyzh.easyshell.sqlserver.sequence;

/**
 * SqlServer序列创建参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class SqlServerCreateSequenceParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 序列
     */
    private SqlServerSequence sequence;

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
     * 获取序列名称
     *
     * @return 序列名称
     */
    public String getSequenceName() {
        return this.sequence.getName();
    }

    /**
     * 设置序列名称
     *
     * @param sequenceName 序列名称
     */
    public void setSequenceName(String sequenceName) {
        this.sequence.setName(sequenceName);
    }

    public SqlServerSequence getSequence() {
        return sequence;
    }

    public void setSequence(SqlServerSequence sequence) {
        this.sequence = sequence;
    }
}
