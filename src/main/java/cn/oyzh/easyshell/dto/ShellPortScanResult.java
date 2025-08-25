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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
