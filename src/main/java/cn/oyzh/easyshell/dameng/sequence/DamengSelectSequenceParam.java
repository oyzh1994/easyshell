package cn.oyzh.easyshell.dameng.sequence;

/**
 * 达梦序列查询参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectSequenceParam {

    /**
     * 是否查询完整定义
     */
    private boolean full;

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 序列名称
     */
    private String sequenceName;

    /**
     * 是否查询完整定义
     *
     * @return 结果
     */
    public boolean isFull() {
        return full;
    }

    /**
     * 设置是否查询完整定义
     *
     * @param full 是否查询完整定义
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

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }
}
