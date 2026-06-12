package cn.oyzh.easyshell.query;


/**
 * 查询提示内容
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class ShellQueryPromptItem {

    /**
     * 类型
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
}
