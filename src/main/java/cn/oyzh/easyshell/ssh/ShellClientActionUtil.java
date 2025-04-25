package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.event.ShellEventUtil;

/**
 * @author oyzh
 * @since 2025-04-21
 */
public class ShellClientActionUtil {

    public static void forAction(String connectName, String action) {
        ShellEventUtil.clientAction(connectName, action);
    }

}
