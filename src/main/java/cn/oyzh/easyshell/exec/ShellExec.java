package cn.oyzh.easyshell.exec;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.util.ShellUtil;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class ShellExec implements AutoCloseable {

    private ShellClient client;

    public ShellExec(ShellClient client) {
        this.client = client;
    }

    public String cpu_info() {
        if (this.client.isMacos()) {
            return this.client.exec("sysctl machdep.cpu");
        }
        if (this.client.isUnix()) {
            return this.client.exec("sysctl hw.model hw.machine hw.ncpu hw.clockrate");
        }
        return this.client.exec("lscpu");
    }

    public String disk_info() {
        return this.client.exec("df -h");
    }

    public String network_interface_info() {
        String output = this.client.exec("ifconfig");
        if (ShellUtil.isCommandNotFound(output)) {
            output = this.client.exec("ip addr");
        }
        return output;
    }

    public String memory_info() {
        if (this.client.isMacos()) {
            return this.client.exec("system_profiler SPMemoryDataType");
        }
        if (this.client.isUnix()) {
            return this.client.exec("dmesg | grep -i memory");
        }
        String output = this.client.exec("lshw -C memory");
        if (ShellUtil.isCommandNotFound(output)) {
            output = this.client.exec("dmidecode -t memory");
        }
        return output;
    }

    public String gpu_info() {
        if (this.client.isMacos()) {
            return this.client.exec("system_profiler SPDisplaysDataType");
        }
        if (this.client.isUnix()) {
            return this.client.exec("pciconf -lv | grep -i vga");
        }
        String output = this.client.exec("nvidia-smi");
        if (ShellUtil.isCommandNotFound(output)) {
            output = this.client.exec("lspci | grep -i '3d'");
        }
        if (StringUtil.isBlank(output)) {
            output = this.client.exec("lspci | grep -i vga");
        }
        return output;
    }

    public String cat_profile() {
        return this.client.exec("cat /etc/profile");
    }

    public String cat_environment() {
        return this.client.exec("cat /etc/environment");
    }

    public String cat_resolv() {
        return this.client.exec("cat /etc/resolv.conf");
    }

    public String cat_hosts() {
        return this.client.exec("cat /etc/hosts");
    }

    public String cat_sshd_config() {
        return this.client.exec("cat /etc/ssh/sshd_config");
    }

    public String cat_bash_bashrc() {
        return this.client.exec("cat /etc/bash.bashrc");
    }

    public String cat_user_profile() {
        return this.client.exec("cat ~/.profile");
    }

    public String cat_user_bash_profile() {
        return this.client.exec("cat ~/.bash_profile");
    }

    public String cat_user_bashrc() {
        return this.client.exec("cat ~/.bashrc");
    }

    public String cat_user_zshrc() {
        return this.client.exec("cat ~/.zshrc");
    }

    public String source(String file) {
        return this.client.exec("source " + file);
    }

    public String cat_docker_daemon(String filePath) {
        return this.client.exec("cat " + filePath);
    }

    public String echo(String text) {
        return this.client.exec("echo " + text);
    }

    public String echo(String text, String file) {
//        String str = text.replaceAll("\"", "\"\"\"");
//        str = str.replaceAll("'", "'\\''");
        return this.client.exec("echo \"" + text + "\" > " + file);
    }

    public String cat_file(String sourceFile, String targetFile) {
        if (this.client.isMacos() || this.client.isLinux()) {
            return this.echo("$(cat " + sourceFile + ")", targetFile);
        }
        return this.client.exec("cat " + sourceFile + " > " + targetFile);
    }

    public String whoami() {
        return this.client.exec("whoami");
    }

    @Override
    public void close() throws Exception {
        this.client = null;
    }

    public String chmod(String permission, String filePath) {
       return this.client.exec("chmod " + permission + " " + filePath);
    }
}
