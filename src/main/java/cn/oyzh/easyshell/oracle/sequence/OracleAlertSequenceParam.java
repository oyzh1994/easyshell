package cn.oyzh.easyshell.oracle.sequence;


/**
 * Oracle序列告警参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class OracleAlertSequenceParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 序列
     */
    private OracleSequence sequence;

    /**
     * 原序列
     */
    private OracleSequence originalSequence;

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

    public OracleSequence getSequence() {
        return sequence;
    }

    public void setSequence(OracleSequence sequence) {
        this.sequence = sequence;
    }

    public OracleSequence getOriginalSequence() {
        return originalSequence;
    }

    public void setOriginalSequence(OracleSequence originalSequence) {
        this.originalSequence = originalSequence;
    }
}
