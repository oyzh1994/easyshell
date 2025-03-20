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
        return this.client.exec("lscpu");
    }

    public String df_h() {
        String output = this.client.exec("df -h");
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("df -h");
        }
        return output;
    }

    public String ifconfig() {
        String output = this.client.exec("ifconfig");
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("ip addr");
        }
        return output;
    }

    public String dmidecode_t_memory() {
        String output = this.client.exec("lshw -C memory");
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("dmidecode -t memory");
        }
        return output;
    }

    public String gpu() {
        String output = this.client.exec("nvidia-smi");
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("lspci | grep -i '3d'");
        }
//        if (StringUtil.isBlank(output)) {
//            output = this.client.exec("lspci | grep -i '3d'");
//        }
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("lspci | grep -i vga");
        }
//        if (StringUtil.isBlank(output)) {
//            output = this.client.exec("lspci | grep -i vga");
//        }
        return output;
    }
}
