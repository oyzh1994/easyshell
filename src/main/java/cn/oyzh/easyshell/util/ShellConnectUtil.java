package cn.oyzh.easyshell.util;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;

/**
 * shell连接工具类
 *
 * @author oyzh
 * @since 2023/07/01
 */

public class ShellConnectUtil {

    /**
     * 关闭连接
     *
     * @param client shell客户端
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

//    /**
//     * 解析连接
//     *
//     * @param input 输入内容
//     * @return 连接
//     */
//    public static ShellConnectInfo parse(String input) {
//        return null;
//    }

    /**
     * 测试连接
     *
     * @param adapter      页面
     * @param shellConnect 连接信息
     */
    public static void testConnect(StageAdapter adapter, ShellConnect shellConnect) {
        StageManager.showMask(adapter, () -> {
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
