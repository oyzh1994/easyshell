package cn.oyzh.easyssh.util;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.domain.SSHInfo;
import cn.oyzh.easyssh.dto.SSHConnect;
import cn.oyzh.easyssh.parser.SSHExceptionParser;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
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
    public static void testConnect(StageAdapter view, String user, String host, String password, int timeout) {
        ThreadUtil.start(() -> {
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
                    MessageBox.okToast("连接成功！");
                } else {
                    MessageBox.warn("连接失败，请检查地址是否有效！");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
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
                    ThreadUtil.start(func);
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
}
