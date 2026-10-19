package cn.oyzh.easyshell.dto;

/**
 * 端口扫描结果
 *
 * @author oyzh
 * @since 2025-05-26
 */
public class ShellPortScanResult {

    /**
     * 端口
     */
    private int port;

    /**
     * 描述
     */
    private String desc;

    /** 获取端口 */
    public int getPort() {
        return port;
    }

    /** 设置端口 */
    public void setPort(int port) {
        this.port = port;
    }

    /** 获取描述 */
    public String getDesc() {
        return desc;
    }

    /** 设置描述 */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
