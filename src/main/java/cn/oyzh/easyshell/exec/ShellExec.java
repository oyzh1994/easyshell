package cn.oyzh.easyshell.exec;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.shell.ShellClient;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class ShellExec {

    private final ShellClient client;

    public ShellExec(ShellClient client) {
        this.client = client;
    }

    public String lscpu() {
        return this.client.exec("/usr/bin/lscpu");
    }

    public String df_h() {
        String output = this.client.exec("/usr/bin/df -h");
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("/bin/df -h");
        }
        return output;
    }
}
