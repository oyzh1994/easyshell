package cn.oyzh.easyshell.shell;

import cn.oyzh.easyshell.domain.ShellConnect;

/**
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellClientUtil {

    public static ShellClient newClient(ShellConnect connect) {
        return new ShellClient(connect);
    }
}
