package cn.oyzh.easyshell.dto.zk;


/**
 * zk环境节点
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKEnvNode {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

    /** 获取名称 */
    public String getName() {
        return name;
    }

    /** 设置名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取值 */
    public String getValue() {
        return value;
    }

    /** 设置值 */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 构造函数
     *
     * @param name  名称
     * @param value 值
     */
    public ShellZKEnvNode(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
