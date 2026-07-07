package cn.oyzh.easyshell.mosh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import javafx.scene.input.KeyCode;
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
    public static MoshTerminalFrontend connectWithSSH(ShellConnect connect, int timeout) throws Exception {
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
     * @param moshKey mosh key
     * @return 已启动的 MoshTerminalFrontend
     */
    public static MoshTerminalFrontend connectWithMoshKey(ShellConnect connect, String moshKey) throws Exception {
        String host = connect.hostIp();
        int moshPort = connect.hostPort();
        InetSocketAddress moshAddress = new InetSocketAddress(host, moshPort);
        MoshKey moshKey1 = MoshKey.fromBase64(moshKey);
        MoshClientSession session = new MoshClientSession(moshAddress, moshKey1, 80, 24);
        MoshTerminalFrontend frontend = new MoshTerminalFrontend(session);
        frontend.sendInitialWakeUp();
        frontend.start();
        return frontend;
    }

    /**
     * 将 JavaFX KeyEvent 转换为 ANSI 转义序列。
     * <p>
     * 使用 application cursor mode 序列（SS3: ESC O）而非 normal mode（CSI: ESC [），
     * 因为 mosh 的 StatefulAnsiRenderer 不转发 terminal mode 切换序列（DECCKM），
     * 导致 jediterm 的 mode 状态与 mosh-server 不同步。
     * 交互式程序（top/vim/less 等）运行时均处于 application mode，使用 SS3 序列。
     */
    public static byte[] mapKeyToAnsiSequence(KeyEvent event) {
        return switch (event.getCode()) {
            //            case ENTER -> new byte[]{'\r'};
            case BACK_SPACE -> new byte[]{0x7f};
            case TAB -> new byte[]{'\t'};
            case ESCAPE -> new byte[]{0x1b};
//            case UP -> new byte[]{0x1b, '[', 'A'};
//            case DOWN -> new byte[]{0x1b, '[', 'B'};
//            case RIGHT -> new byte[]{0x1b, '[', 'C'};
//            case LEFT -> new byte[]{0x1b, '[', 'D'};
//            case HOME -> new byte[]{0x1b, '[', 'H'};
//            case END -> new byte[]{0x1b, '[', 'F'};
            case UP -> new byte[]{0x1b, 'O', 'A'};
            case DOWN -> new byte[]{0x1b, 'O', 'B'};
            case RIGHT -> new byte[]{0x1b, 'O', 'C'};
            case LEFT -> new byte[]{0x1b, 'O', 'D'};
            case HOME -> new byte[]{0x1b, 'O', 'H'};
            case END -> new byte[]{0x1b, 'O', 'F'};
            case PAGE_UP -> new byte[]{0x1b, '[', '5', '~'};
            case PAGE_DOWN -> new byte[]{0x1b, '[', '6', '~'};
            case DELETE -> new byte[]{0x1b, '[', '3', '~'};
            case INSERT -> new byte[]{0x1b, '[', '2', '~'};
            default -> null;
        };
    }

//    /**
//     * 转换为ansi序列
//     *
//     * @param code 编码
//     * @return 结果
//     */
//    public static byte[] mapKeyToAnsiSequence(int code) {
//        if (code == KeyCode.ENTER.getCode()) {
//            return new byte[]{'\r'};
//        }
//        if (code == KeyCode.BACK_SPACE.getCode()) {
//            return new byte[]{0x7f};
//        }
//        if (code == KeyCode.TAB.getCode()) {
//            return new byte[]{'\t'};
//        }
//        if (code == KeyCode.ESCAPE.getCode()) {
//            return new byte[]{0x1b};
//        }
//        if (code == KeyCode.UP.getCode()) {
//            return new byte[]{0x1b, '0', 'A'};
//        }
//        if (code == KeyCode.DOWN.getCode()) {
//            return new byte[]{0x1b, '0', 'B'};
//        }
//        if (code == KeyCode.RIGHT.getCode()) {
//            return new byte[]{0x1b, '0', 'C'};
//        }
//        if (code == KeyCode.LEFT.getCode()) {
//            return new byte[]{0x1b, '0', 'D'};
//        }
//        if (code == KeyCode.HOME.getCode()) {
//            return new byte[]{0x1b, '0', 'H'};
//        }
//        if (code == KeyCode.END.getCode()) {
//            return new byte[]{0x1b, '0', 'F'};
//        }
//        if (code == KeyCode.PAGE_UP.getCode()) {
//            return new byte[]{0x1b, '[', '5', '~'};
//        }
//        if (code == KeyCode.PAGE_DOWN.getCode()) {
//            return new byte[]{0x1b, '[', '6', '~'};
//        }
//        if (code == KeyCode.DELETE.getCode()) {
//            return new byte[]{0x1b, '[', '3', '~'};
//        }
//        if (code == KeyCode.INSERT.getCode()) {
//            return new byte[]{0x1b, '[', '2', '~'};
//        }
//        return null;
//    }

    //    /**
    //     * 转换为ansi序列
    //     *
    //     * @param code 编码
    //     * @return 结果
    //     */
    //    public static byte[] mapKeyToAnsiSequence(int code) {
    //        return switch (code) {
    //            //            case ENTER -> new byte[]{'\r'};
    //            case java.awt.event.KeyEvent.VK_BACK_SPACE -> new byte[]{0x7f};
    //            case java.awt.event.KeyEvent.VK_TAB -> new byte[]{'\t'};
    //            case java.awt.event.KeyEvent.VK_ESCAPE -> new byte[]{0x1b};
    //            case java.awt.event.KeyEvent.VK_UP -> new byte[]{0x1b, '[', 'A'};
    //            case java.awt.event.KeyEvent.VK_DOWN -> new byte[]{0x1b, '[', 'B'};
    //            case java.awt.event.KeyEvent.VK_RIGHT -> new byte[]{0x1b, '[', 'C'};
    //            case java.awt.event.KeyEvent.VK_LEFT -> new byte[]{0x1b, '[', 'D'};
    //            case java.awt.event.KeyEvent.VK_HOME -> new byte[]{0x1b, '[', 'H'};
    //            case java.awt.event.KeyEvent.VK_END -> new byte[]{0x1b, '[', 'F'};
    //            case java.awt.event.KeyEvent.VK_PAGE_UP -> new byte[]{0x1b, '[', '5', '~'};
    //            case java.awt.event.KeyEvent.VK_PAGE_DOWN -> new byte[]{0x1b, '[', '6', '~'};
    //            case java.awt.event.KeyEvent.VK_DELETE -> new byte[]{0x1b, '[', '3', '~'};
    //            case java.awt.event.KeyEvent.VK_INSERT -> new byte[]{0x1b, '[', '2', '~'};
    //            default -> null;
    //        };
    //    }


}