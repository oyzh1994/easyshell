package cn.oyzh.easyshell.sshj.exec;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.ssh.sftp.ShellSFTPChannel;
import cn.oyzh.easyshell.sshj.ShellBaseSSHClient;
import cn.oyzh.easyshell.util.ShellUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class ShellSSHExec implements AutoCloseable {

    private ShellBaseSSHClient client;

    public ShellSSHExec(ShellBaseSSHClient client) {
        this.client = client;
    }

    public String cpu_info() {
        if (this.client.isMacos()) {
            return this.client.exec("sysctl machdep.cpu");
        }
        if (this.client.isWindows()) {
            String output = this.client.exec("wmic cpu");
            String[] lines = output.split("\n");
            String[] cols1 = lines[0].split("\\s+");
            String[] cols2 = lines[1].splitWithDelimiters("\\s+", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cols1.length; i++) {
                sb.append(cols1[i]).append(" : ").append(cols2[i]).append("\n");
            }
            return sb.toString();
        }
        if (this.client.isUnix()) {
            return this.client.exec("sysctl hw.model hw.machine hw.ncpu hw.clockrate");
        }
        return this.client.exec("lscpu");
    }

    public List<ShellSSHDiskInfo> disk_info() {
        if (this.client.isWindows()) {
            String output = this.client.exec("wmic logicaldisk  get name, size, freespace, volumeName");
            return ShellSSHExecParser.diskForWindows(output);
        } else {
            String output = this.client.exec("df -h");
            if (this.client.isMacos()) {
                return ShellSSHExecParser.diskForMacos(output);
            }
            return ShellSSHExecParser.diskForLinux(output);
        }
    }

    public String network_interface_info() {
        if (this.client.isWindows()) {
            return this.client.exec("ipconfig /all");
        }
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
        if (this.client.isWindows()) {
            String output = this.client.exec("wmic memorychip");
            String[] lines = output.split("\n");
            String[] cols1 = lines[0].split("\\s+");
            String[] cols2 = lines[1].splitWithDelimiters("\\s+", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cols1.length; i++) {
                sb.append(cols1[i]).append(" : ").append(cols2[i]).append("\n");
            }
            return sb.toString();
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
        if (this.client.isWindows()) {
            String output = this.client.exec("nvidia-smi");
            if (!ShellUtil.isCommandNotFound(output)) {
                return output;
            }
            output = this.client.exec("wmic path win32_VideoController");
            String[] lines = output.split("\n");
            String[] cols1 = lines[0].split("\\s+");
            String[] cols2 = lines[1].splitWithDelimiters("\\s+", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cols1.length; i++) {
                sb.append(cols1[i]).append(" : ").append(cols2[i]).append("\n");
            }
            return sb.toString();
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
        if (this.client.isWindows()) {
            return this.client.exec("set");
        }
        return this.client.exec("cat /etc/environment");
    }

    public String cat_resolv() {
        return this.client.exec("cat /etc/resolv.conf");
    }

    public String cat_hosts() {
        if (this.client.isWindows()) {
            return this.client.exec("type C:\\Windows\\System32\\drivers\\etc\\HOSTS");
        }
        return this.client.exec("cat /etc/hosts");
    }

    public String cat_sshd_config() {
        if (this.client.isWindows()) {
            return this.client.exec("type C:\\ProgramData\\ssh\\sshd_config");
        }
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
        if (this.client.isWindows()) {
            filePath = ShellFileUtil.fixWindowsFilePath(filePath);
            return this.client.exec("type \"" + filePath + "\"");
        }
        return this.client.exec("cat " + filePath);
    }

    public String echo(String text) {
        return this.client.exec("echo " + text);
    }

    public String echo(String text, String file) {
        return this.client.exec("echo \"" + text + "\" > " + file);
    }

    public String cat_file(String sourceFile, String targetFile) {
        if (this.client.isMacos() || this.client.isLinux()) {
            return this.echo("$(cat " + sourceFile + ")", targetFile);
        }
        if (this.client.isWindows()) {
            sourceFile = ShellFileUtil.fixWindowsFilePath(sourceFile);
            targetFile = ShellFileUtil.fixWindowsFilePath(targetFile);
            return this.client.exec("type \"" + sourceFile + "\" > " + targetFile + "\"");
        }
        return this.client.exec("cat " + sourceFile + " > " + targetFile);
    }

    public String append(String text, String file) {
        return this.client.exec("echo \"" + text + "\" >> " + file);
    }

    public String append_file(String sourceFile, String targetFile) {
        if (this.client.isMacos() || this.client.isLinux()) {
            return this.echo("$(cat " + sourceFile + ")", targetFile);
        }
        if (this.client.isWindows()) {
            sourceFile = ShellFileUtil.fixWindowsFilePath(sourceFile);
            targetFile = ShellFileUtil.fixWindowsFilePath(targetFile);
            return this.client.exec("type \"" + sourceFile + "\" >> \"" + targetFile + "\"");
        }
        return this.client.exec("cat " + sourceFile + " >> " + targetFile);
    }

    public String whoami() {
        return this.client.exec("whoami");
    }

    @Override
    public void close() throws Exception {
        this.client = null;
    }

    /**
     * 修改权限，这个方法不能兼容windows，请用以下方法
     *
     * @param permission 权限
     * @param filePath   文件路径
     * @return 结果
     * @see ShellSFTPChannel#chmod(int, String)
     */
    @Deprecated
    public String chmod(String permission, String filePath) {
        return this.client.exec("chmod " + permission + " " + filePath);
    }

}
