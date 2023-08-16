package cn.oyzh.easyssh.util;

import cn.hutool.core.thread.ThreadUtil;
import cn.oyzh.common.thread.TimerUtil;
import cn.oyzh.easyfx.information.FXAlertUtil;
import cn.oyzh.easyfx.information.FXToastUtil;
import cn.oyzh.easyfx.view.FXView;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.dto.SSHConnect;
import cn.oyzh.easyssh.parser.SSHExceptionParser;
import cn.oyzh.easyssh.ssh.SSHClient;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * ssh连接工具类
 *
 * @author oyzh
 * @since 2023/07/01
 */
@Slf4j
@UtilityClass
public class SSHConnectUtil {

    /**
     * 测试连接
     *
     * @param view     页面
     * @param user     用户
     * @param host     地址
     * @param password 密码
     * @param timeout  超时时间
     */
    public static void testConnect(FXView view, String user, String host, String password, int timeout) {
        TimerUtil.start(() -> {
            try {
                view.disable();
                view.waitCursor();
                view.appendTitle("==连接测试中...");
                // 创建ssh信息
                SSHInfo sshInfo = new SSHInfo();
                sshInfo.setUser(user);
                sshInfo.setHost(host);
                sshInfo.setPassword(password);
                sshInfo.setConnectTimeOut(timeout);
                SSHClient client = new SSHClient(sshInfo);
                // 开始连接
                client.start();
                if (client.isConnected()) {
                    client.close();
                    FXToastUtil.ok("连接成功！");
                } else {
                    FXAlertUtil.warn("连接失败，请检查地址是否有效！");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                FXAlertUtil.warn(ex, SSHExceptionParser.INSTANCE);
            } finally {
                view.enable();
                view.defaultCursor();
                view.restoreTitle();
            }
        });
    }

    /**
     * 关闭连接
     *
     * @param client ssh客户端
     * @param async  是否异步
     */
    public static void close(SSHClient client, boolean async) {
        try {
            if (client != null && client.isConnected()) {
                Runnable func = client::close;
                if (async) {
                    TimerUtil.start(func);
                } else {
                    func.run();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 解析连接
     *
     * @param input 输入内容
     * @return 连接
     */
    public static SSHConnect parse(String input) {
        if (input == null) {
            return null;
        }
        try {
            String[] words = input.split(" ");
            SSHConnect connect = new SSHConnect();
            int type = -1;
            for (String word : words) {
                if (type == 0) {
                    connect.setHost(word.trim());
                } else if (type == 1) {
                    connect.setPort(Integer.parseInt(word.trim()));
                } else if (type == 2) {
                    connect.setPassword(word.trim());
                }
                if (word.equalsIgnoreCase("-h")) {
                    type = 0;
                } else if (word.equalsIgnoreCase("-p")) {
                    type = 1;
                } else if (word.equalsIgnoreCase("-a")) {
                    type = 2;
                } else {
                    type = -1;
                }
            }
            return connect;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 读取shell结果
     *
     * @param in 流
     * @return 结果
     */
    public static String readShellInput(InputStream in) throws IOException {
        // 获取命令执行结果
        StringBuilder builder = new StringBuilder();
        int maxCount = 0;
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                builder.append(new String(tmp, 0, i));
            }
            if (maxCount++ > 20) {
                break;
            }
            ThreadUtil.sleep(5);
        }
        return builder.toString();
    }

    //
    // /**
    //  * 读取exec结果
    //  *
    //  * @param exec exec通道
    //  * @return 连接
    //  */
    // public static SSHExecResult readExecInput(ChannelExec exec) throws IOException {
    //     // 获取命令执行结果
    //     InputStream in = exec.getInputStream();
    //     StringBuilder builder = new StringBuilder();
    //     byte[] tmp = new byte[1024];
    //     int exitStatus;
    //     while (true) {
    //         // 读取数据
    //         while (in.available() > 0) {
    //             int i = in.read(tmp, 0, 1024);
    //             if (i < 0) {
    //                 break;
    //             }
    //             builder.append(new String(tmp, 0, i));
    //         }
    //         // 从channel获取全部信息之后，channel会自动关闭
    //         if (exec.isClosed()) {
    //             if (in.available() > 0) {
    //                 continue;
    //             }
    //             exitStatus = exec.getExitStatus();
    //             break;
    //         }
    //         ThreadUtil.sleep(5);
    //     }
    //     return new SSHExecResult(builder.toString(), exitStatus);
    // }
}
