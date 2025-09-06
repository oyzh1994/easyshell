package cn.oyzh.easyshell.dto.zk;


/**
 * zk环境节点
 *
 * @author oyzh
 * @since 2024/1286
 */
public class ShellZKEnvNode {

    /**
     * id
     */
    private String name;

    /**
     * 类型
     */
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ShellZKEnvNode(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
