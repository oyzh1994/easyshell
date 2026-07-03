package cn.oyzh.easyshell.test.mosh;

import com.jcraft.jsch.*;
import org.mosh4j.core.MoshClientSession;
import org.mosh4j.core.MoshTerminalFrontend;
import org.mosh4j.crypto.MoshKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class MoshAutoConnector {

    /**
     * 自动连接 Mosh 服务
     * @param host 服务器 IP 或域名
     * @param port SSH 端口（默认 22）
     * @param username SSH 用户名
     * @param password SSH 密码（或使用私钥，见下文）
     * @return 已启动的 MoshTerminalFrontend
     */
    public static MoshTerminalFrontend connect(String host, int port, String username, String password) throws Exception {
        // 1. SSH 连接
        JSch jsch = new JSch();
        Session sshSession = jsch.getSession(username, host, port);
        sshSession.setPassword(password);
        sshSession.setConfig("StrictHostKeyChecking", "no");  // 测试环境跳过 known_hosts
        sshSession.connect(10000);  // 10秒超时

        // 2. 执行 mosh-server new 并读取输出
        ChannelExec channel = (ChannelExec) sshSession.openChannel("exec");
        // 关键：必须请求伪终端（-s 参数），否则 mosh-server 不会输出密钥
        channel.setCommand("mosh-server new -s -c 256");
        channel.setPty(true);
        channel.connect();

        // 读取 stdout（mosh-server 的输出）
        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        String line;
        String key = null;
        int moshPort = -1;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("MOSH CONNECT")) {
                // 格式: "MOSH CONNECT 60001 4kYMa9v+P1lOQ0Uy7A=="
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    moshPort = Integer.parseInt(parts[2]);
                    key = parts[3];
                    break;
                }
            }
        }

        // 3. 关闭 SSH 通道（UDP 连接成功后 SSH 不再需要）
        channel.disconnect();
        sshSession.disconnect();

        if (key == null || moshPort == -1) {
            throw new RuntimeException("无法解析 mosh-server 返回的端口和密钥");
        }

        // 4. 创建 Mosh 会话
        InetSocketAddress moshAddress = new InetSocketAddress(host, moshPort);
        MoshKey moshKey = MoshKey.fromBase64(key);
        MoshClientSession session = new MoshClientSession(moshAddress, moshKey, 80, 24);
        MoshTerminalFrontend frontend = new MoshTerminalFrontend(session);
        frontend.sendInitialWakeUp();
        // 不自动启动 receiveThread，由调用方自行驱动 pollOnce()
        frontend.start();

        return frontend;
    }
}