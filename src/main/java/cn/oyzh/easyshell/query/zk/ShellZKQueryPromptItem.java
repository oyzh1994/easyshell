package cn.oyzh.easyshell.query.zk;


/**
 * 查询提示内容
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryPromptItem {

    /**
     * 类型
     * 1 关键字
     * 2 参数
     * 3 节点
     */
    private byte type;

    /**
     * 内容
     */
    private String content;

    /**
     * 相关度
     */
    private double correlation;

    /**
     * 额外内容
     */
    private String extContent;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(double correlation) {
        this.correlation = correlation;
    }

    public String getExtContent() {
        return extContent;
    }

    public void setExtContent(String extContent) {
        this.extContent = extContent;
    }

    /**
     * 是否关键字类型
     *
     * @return 结果
     */
    public boolean isKeywordType() {
        return 1 == this.type;
    }

    /**
     * 是否参数类型
     *
     * @return 结果
     */
    public boolean isParamType() {
        return 2 == this.type;
    }

    /**
     * 是否节点类型
     *
     * @return 结果
     */
    public boolean isNodeType() {
        return 3 == this.type;
    }
}
