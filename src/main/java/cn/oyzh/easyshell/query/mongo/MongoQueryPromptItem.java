package cn.oyzh.easyshell.query.mongo;


/**
 * 查询提示内容
 *
 * @author oyzh
 * @since 2024/02/21
 */
public class MongoQueryPromptItem {

    /**
     * 类型
     * 1 collection
     * 2 function
     * 4 keyword
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

    /**
     * 是否函数类型
     *
     * @return 结果
     */
    public boolean isFunctionType() {
        return 2 == this.type;
    }

    /**
     * 是否集合类型
     *
     * @return 结果
     */
    public boolean isCollectionType() {
        return 1 == this.type;
    }

    /**
     * 是否关键字类型
     *
     * @return 结果
     */
    public boolean isKeywordType() {
        return 4 == this.type;
    }

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
