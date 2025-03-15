package cn.oyzh.easyshell.util;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.ShellConnectInfo;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import lombok.experimental.UtilityClass;

/**
 * ssh连接工具类
 *
 * @author oyzh
 * @since 2023/07/01
 */
@UtilityClass
public class ShellConnectUtil {

//    /**
//     * 测试连接
//     *
//     * @param view     页面
//     * @param user     用户
//     * @param host     地址
//     * @param password 密码
//     * @param timeout  超时时间
//     */
//    public static void testConnect(StageAdapter view, String user, String host, String password, int timeout) {
//        ThreadUtil.start(() -> {
//            try {
//                view.disable();
//                view.waitCursor();
//                view.appendTitle("==连接测试中...");
//                // 创建ssh信息
//                SSHConnect sshInfo = new SSHConnect();
//                sshInfo.setUser(user);
//                sshInfo.setHost(host);
//                sshInfo.setPassword(password);
//                sshInfo.setConnectTimeOut(timeout);
//                SSHClient client = new SSHClient(sshInfo);
//                // 开始连接
//                client.start();
//                if (client.isConnected()) {
//                    client.close();
//                    MessageBox.okToast("连接成功！");
//                } else {
//                    MessageBox.warn("连接失败，请检查地址是否有效！");
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                MessageBox.exception(ex);
//            } finally {
//                view.enable();
//                view.defaultCursor();
//                view.restoreTitle();
//            }
//        });
//    }

    /**
     * 关闭连接
     *
     * @param client ssh客户端
     * @param async  是否异步
     */
    public static void close(ShellClient client, boolean async) {
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
    public static ShellConnectInfo parse(String input) {
        return null;
    }

    /**
     * 测试连接
     *
     * @param adapter    页面
     * @param shellConnect 连接信息
     */
    public static void testConnect(StageAdapter adapter, ShellConnect shellConnect) {
        StageManager.showMask(() -> {
            try {
//                adapter.disable();
//                adapter.waitCursor();
//                adapter.appendTitle("==" + I18nHelper.connectTesting());
                if (shellConnect.getName() == null) {
                    shellConnect.setName(I18nHelper.testConnection());
                }
                ShellClient client = new ShellClient(shellConnect);
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
//            } finally {
//                adapter.enable();
//                adapter.defaultCursor();
//                adapter.restoreTitle();
            }
        });
    }
}
