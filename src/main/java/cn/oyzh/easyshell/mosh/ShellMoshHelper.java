package cn.oyzh.easyshell.mosh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import javafx.scene.input.KeyEvent;
import org.mosh4j.core.MoshClientSession;
import org.mosh4j.core.MoshTerminalFrontend;
import org.mosh4j.crypto.MoshKey;

import java.net.InetSocketAddress;

public class ShellMoshHelper {

    /**
     * 自动连接 Mosh 服务
     *
     * @param connect 连接
     * @param timeout 超时时间
     * @return 已启动的 MoshTerminalFrontend
     */
    public static MoshTerminalFrontend connect(ShellConnect connect, int timeout) throws Exception {
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            client.start(timeout);
            String result = client.exec("mosh-server new -s -c 256");

            String key = null;
            int moshPort = -1;
            for (String line : result.lines().toList()) {
                if (line.startsWith("MOSH CONNECT")) {
                    // 格式: "MOSH CONNECT 60001 4kYMa9v+P1lOQ0Uy7A=="
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        key = parts[3];
                        moshPort = Integer.parseInt(parts[2]);
                        break;
                    }
                }
            }

            if (key == null || moshPort == -1) {
                throw new ShellException("mosh-server start fail!");
            }

            String host = connect.hostIp();
            InetSocketAddress moshAddress = new InetSocketAddress(host, moshPort);
            MoshKey moshKey = MoshKey.fromBase64(key);
            MoshClientSession session = new MoshClientSession(moshAddress, moshKey, 80, 24);
            MoshTerminalFrontend frontend = new MoshTerminalFrontend(session);
            frontend.sendInitialWakeUp();
            frontend.start();
            return frontend;
        } catch (Throwable ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 连接 Mosh 服务
     *
     * @param connect 连接
     * @param key     moshKey
     * @return 已启动的 MoshTerminalFrontend
     */
    public static MoshTerminalFrontend connect(ShellConnect connect, String key) throws Exception {
        String host = connect.hostIp();
        int moshPort = connect.hostPort();
        InetSocketAddress moshAddress = new InetSocketAddress(host, moshPort);
        MoshKey moshKey = MoshKey.fromBase64(key);
        MoshClientSession session = new MoshClientSession(moshAddress, moshKey, 80, 24);
        MoshTerminalFrontend frontend = new MoshTerminalFrontend(session);
        frontend.sendInitialWakeUp();
        frontend.start();
        return frontend;
    }

    /**
     * 转换为ansi序列
     *
     * @param event 事件
     * @return 结果
     */
    public static byte[] mapKeyToAnsiSequence(KeyEvent event) {
        return switch (event.getCode()) {
//            case ENTER -> new byte[]{'\r'};
            case BACK_SPACE -> new byte[]{0x7f};
            case TAB -> new byte[]{'\t'};
            case ESCAPE -> new byte[]{0x1b};
            case UP -> new byte[]{0x1b, '[', 'A'};
            case DOWN -> new byte[]{0x1b, '[', 'B'};
            case RIGHT -> new byte[]{0x1b, '[', 'C'};
            case LEFT -> new byte[]{0x1b, '[', 'D'};
            case HOME -> new byte[]{0x1b, '[', 'H'};
            case END -> new byte[]{0x1b, '[', 'F'};
            case PAGE_UP -> new byte[]{0x1b, '[', '5', '~'};
            case PAGE_DOWN -> new byte[]{0x1b, '[', '6', '~'};
            case DELETE -> new byte[]{0x1b, '[', '3', '~'};
            case INSERT -> new byte[]{0x1b, '[', '2', '~'};
            default -> null;
        };
    }


}