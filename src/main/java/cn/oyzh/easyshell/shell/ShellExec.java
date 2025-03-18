package cn.oyzh.easyshell.shell;


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
}
