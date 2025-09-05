package cn.oyzh.easyshell.util.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.zk.ZKConnectInfo;

/**
 * zk连接工具类
 *
 * @author oyzh
 * @since 2022/8/26
 */

public class ShellZKConnectUtil {

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
    //
    // /**
    //  * 测试连接
    //  *
    //  * @param adapter   页面
    //  * @param zkConnect zk信息
    //  */
    // public static void testConnect(StageAdapter adapter, ShellConnect zkConnect) {
    //     StageManager.showMask(adapter, () -> {
    //         try {
    //             if (zkConnect.getName() == null) {
    //                 zkConnect.setName(I18nHelper.testConnection());
    //             }
    //             ZKClient client = new ZKClient(zkConnect);
    //             // 开始连接
    //             client.start(3_000);
    //             if (client.isConnected()) {
    //                 client.close();
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

    // /**
    //  * 关闭客户端
    //  *
    //  * @param client zk客户端
    //  * @param async  是否异步
    //  * @param quiet  是否静默
    //  */
    // public static void close(ZKClient client, boolean async, boolean quiet) {
    //     try {
    //         if (client != null && client.isConnected()) {
    //             if (async && quiet) {
    //                 ThreadUtil.startVirtual(client::closeQuiet);
    //             } else if (async) {
    //                 ThreadUtil.startVirtual(client::close);
    //             } else if (quiet) {
    //                 client.closeQuiet();
    //             } else {
    //                 client.close();
    //             }
    //         }
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }

    /**
     * 解析连接
     *
     * @param input 输入内容
     * @return 连接
     */
    public static ZKConnectInfo parse(String input) {
        if (input != null) {
            try {
                String[] words = input.split(" ");
                ZKConnectInfo connect = new ZKConnectInfo();
                connect.setInput(input);
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    int type;
                    if (word.equalsIgnoreCase("-server")) {
                        type = 0;
                    } else if (word.equalsIgnoreCase("-timeout")) {
                        type = 1;
                    } else if (word.equalsIgnoreCase("-r")) {
                        type = 2;
                    } else {
                        type = -1;
                    }
                    if (type == 0) {
                        String[] strings = words[i + 1].trim().split(":");
                        if (strings.length > 0) {
                            connect.setHost(strings[0]);
                        }
                        if (strings.length > 1) {
                            connect.setPort(Integer.parseInt(strings[1]));
                        }
                    } else if (type == 1) {
                        connect.setTimeout(Integer.parseInt(words[i + 1]) / 1000);
                    } else if (type == 2) {
                        connect.setReadonly(true);
                    }
                }
                return connect;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 复制连接
     *
     * @param connectInfo 连接信息
     * @param connect     连接
     */
    public static void copyConnect(ZKConnectInfo connectInfo, ShellConnect connect) {
        if (connectInfo != null && connect != null) {
            connect.setReadonly(connectInfo.isReadonly());
            connect.setConnectTimeOut(connectInfo.getTimeout());
            connect.setHost(connectInfo.getHost() + ":" + connectInfo.getPort());
        }
    }
}
