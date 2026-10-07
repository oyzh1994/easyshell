package cn.oyzh.easyshell.internal;

import cn.oyzh.easyshell.event.ShellEventUtil;

/**
 * 客户端动作工具类
 *
 * @author oyzh
 * @since 2025-04-21
 */
public class ShellClientActionUtil {

    /**
     * 触发客户端动作事件
     *
     * @param connectName 连接名称
     * @param action      动作
     */
    public static void forAction(String connectName, String action) {
        ShellEventUtil.clientAction(connectName, action);
    }

}
