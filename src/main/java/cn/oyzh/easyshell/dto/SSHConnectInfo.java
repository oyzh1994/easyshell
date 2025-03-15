package cn.oyzh.easyshell.dto;

import lombok.Data;

/**
 * ssh连接
 *
 * @author oyzh
 * @since 2023/8/10
 */
@Data
public class SSHConnectInfo {

    /**
     * 地址
     */
    private String host = "127.0.0.1";

    /**
     * 端口
     */
    private int port = 22;

    /**
     * 用户
     */
    private String user;

    /**
     * 密码
     */
    private String password;

}
