package cn.oyzh.easyshell.redis;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.redis.ShellRedisConnectInfo;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis连接工具类
 *
 * @author oyzh
 * @since 2023/07/01
 */

public class ShellRedisConnectUtil {

    // /**
    //  * 测试连接
    //  *
    //  * @param adapter    页面
    //  * @param sshConnect 连接信息
    //  */
    // public static void testSSHConnect(StageAdapter adapter, SSHConnect sshConnect) {
    //     StageManager.showMask(adapter, () -> {
    //         try {
    //             SSHJumpForwarder2 forwarder = new SSHJumpForwarder2();
    //             ClientSession session = forwarder.initSession(sshConnect);
    //             // 判断是否成功
    //             if (session != null && session.isOpen()) {
    //                 session.close();
    //                 MessageBox.okToast(I18nHelper.connectSuccess());
    //             } else {
    //                 MessageBox.warn(I18nHelper.connectFail());
    //             }
    //         } catch (Exception ex) {
    //             ex.printStackTrace();
    //             MessageBox.exception(ex);
    //         }
    //     });
    // }

    /**
     * 测试连接
     *
     * @param adapter         页面
     * @param redisConnect redis信息
     */
    public static void testConnect(StageAdapter adapter, ShellConnect redisConnect) {
        StageManager.showMask(adapter,() -> {
            try {
                if (redisConnect.getName() == null) {
                    redisConnect.setName(I18nHelper.testConnection());
                }
                ShellRedisClient client = new ShellRedisClient(redisConnect);
                // 开始连接
                client.start(3_000);
                if (client.isConnected()) {
                    client.close();
                    MessageBox.okToast(I18nHelper.connectSuccess());
                } else {
                    MessageBox.warn(I18nHelper.connectFail());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 关闭客户端
     *
     * @param client redis客户端
     * @param async  是否异步
     * @param quiet  是否静默
     */
    public static void close(ShellRedisClient client, boolean async, boolean quiet) {
        try {
            if (client != null && client.isConnected()) {
                if (async && quiet) {
                    ThreadUtil.start(client::closeQuiet);
                } else if (async) {
                    ThreadUtil.start(client::close);
                } else if (quiet) {
                    client.closeQuiet();
                } else {
                    client.close();
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
    public static ShellRedisConnectInfo parse(String input) {
        if (input == null) {
            return null;
        }
        try {
            String[] words = input.split(" ");
            ShellRedisConnectInfo connect = new ShellRedisConnectInfo();
            connect.setInput(input);
            int type = -1;
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.equalsIgnoreCase("-h")) {
                    type = 0;
                } else if (word.equalsIgnoreCase("-p")) {
                    type = 1;
                } else if (word.equalsIgnoreCase("-a")) {
                    type = 2;
                } else if (word.equalsIgnoreCase("-n")) {
                    type = 3;
                } else if (word.equalsIgnoreCase("-r")) {
                    type = 4;
                } else if (word.equalsIgnoreCase("-u")) {
                    type = 5;
                } else if (word.equalsIgnoreCase("-timeout")) {
                    type = 6;
                } else {
                    type = -1;
                }
                if (type == 0) {
                    connect.setHost(words[i + 1].trim());
                } else if (type == 1) {
                    connect.setPort(Integer.parseInt(words[i + 1].trim()));
                } else if (type == 2) {
                    connect.setPassword(words[i + 1].trim());
                } else if (type == 3) {
                    connect.setDb(Integer.parseInt(words[i + 1].trim()));
                } else if (type == 4) {
                    connect.setReadonly(true);
                } else if (type == 5) {
                    connect.setUser(words[i + 1].trim());
                } else if (type == 6) {
                    connect.setTimeout(Integer.parseInt(words[i + 1].trim()) / 1000);
                }
            }
            return connect;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 复制连接
     *
     * @param connectInfo  连接信息
     * @param redisConnect redis对象
     */
    public static void copyConnect(ShellRedisConnectInfo connectInfo, ShellConnect redisConnect) {
        if (connectInfo != null && redisConnect != null) {
            redisConnect.setUser(connectInfo.getUser());
            redisConnect.setReadonly(connectInfo.isReadonly());
            redisConnect.setPassword(connectInfo.getPassword());
            redisConnect.setConnectTimeOut(connectInfo.getTimeout());
            redisConnect.setExecuteTimeOut(connectInfo.getTimeout());
            redisConnect.setHost(connectInfo.getHost() + ":" + connectInfo.getPort());
        }
    }
}
