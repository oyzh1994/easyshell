package cn.oyzh.easyshell.rdp;


import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.network.NetworkUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessBuilderUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.beans.property.ObjectProperty;

import java.io.File;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2025-09-12
 */
public class ShellRDPClient implements ShellBaseClient {

    private final ShellConnect shellConnect;

    public ShellRDPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    public File initRDPFile() {
        String ip = this.shellConnect.hostIp();
        int port = this.shellConnect.hostPort();
        String username = this.shellConnect.getUser();
        String password = this.shellConnect.getPassword();
        String resolution = this.shellConnect.getResolution();
        String cachePath = ShellConst.getCachePath();
        File tempFile = new File(cachePath, UUIDUtil.uuidSimple() + ".rdp");
        ArrayList<String> list = new ArrayList<>();
        // 地址
        list.add("full address:s:" + ip + ":" + port);
        // 用户名
        list.add("username:s:" + username);
        // 密码
        if (StringUtil.isNotBlank(password)) {
            if (OSUtil.isWindows()) {
                list.add("password 51:b:" + ShellRDPUtil.cryptRdpPassword(password));
            } else {
                ClipboardUtil.setString(password);
            }
        }
        // 分辨率
        if (StringUtil.isNotBlank(resolution)) {
            String[] resolutions = resolution.toLowerCase().split("x");
            if (resolutions.length == 2) {
                list.add("use multimon:i:0");
                list.add("screen mode id:i:1");
                list.add("desktopwidth:i:" + resolutions[0]);
                list.add("desktopheight:i:" + resolutions[1]);
                // macos额外设置
                if (OSUtil.isMacOS()) {
                    list.add("dynamic resolution:i:0");
                }
            }
        }
        // 写入文件
        FileUtil.writeUtf8Lines(list, tempFile);
        // 返回文件
        return tempFile;
    }

    @Override
    public void start(int timeout) throws Throwable {
        File rdpFile = this.initRDPFile();
        // 执行命令
        if (OSUtil.isMacOS()) {
            ProcessBuilderUtil.exec("open", rdpFile.getPath());
        } else if (OSUtil.isWindows()) {
            ProcessBuilderUtil.exec("mstsc", rdpFile.getPath());
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    @Override
    public boolean isConnected() {
        String ip = this.shellConnect.hostIp();
        int port = this.shellConnect.hostPort();
        return NetworkUtil.reachable(ip, port, 1000);
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
