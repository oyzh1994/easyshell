package cn.oyzh.easyshell.dto;

/**
 * 端口扫描结果
 *
 * @author oyzh
 * @since 2025-05-26
 */
public class ShellPortScanResult {

    private int port;

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
