package cn.oyzh.easyshell.rdp;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import com.sun.jna.platform.win32.Crypt32Util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author oyzh
 * @since 2025-09-12
 */
public class ShellRDPUtil {

    public static String cryptRdpPassword(String password) {
        byte[] bytes = Crypt32Util.cryptProtectData(password.getBytes(StandardCharsets.UTF_16LE));
        return HexUtil.bytesToHex(bytes);
    }

    /**
     * 是否内建版本
     *
     * @param connect 连接
     * @return 结果
     */
    public static boolean isBuiltIn(ShellConnect connect) {
        Integer method = connect.getExtra("method");
        return method != null && method == 0;
    }

    /**
     * 初始化rdp文件
     *
     * @param connect 连接
     * @return 结果
     */
    public static File initRDPFile(ShellConnect connect) {
        String ip = connect.hostIp();
        int port = connect.hostPort();
        String username = connect.getUser();
        String domain = connect.getDomain();
        String password = connect.getPassword();
        Integer color = connect.getExtra("color");
        String resolution = connect.getExtra("resolution");
        Boolean remoteAudio = connect.getExtra("remoteAudio");
        Boolean redirectClipboard = connect.getExtra("redirectClipboard");
        String cachePath = ShellConst.getCachePath();
        File tempFile = new File(cachePath, UUIDUtil.uuidSimple() + ".rdp");
        ArrayList<String> list = new ArrayList<>();
        // 地址
        list.add("full address:s:" + ip + ":" + port);
        // 用户名
        list.add("username:s:" + username);
        // 颜色
        if (StringUtil.isNotBlank(domain)) {
            list.add("session bpp:i:" + color);
        }
        // 音频
        if (BooleanUtil.isTrue(remoteAudio)) {
            list.add("audiomode:i:1");
        } else {
            list.add("audiomode:i:0");
        }
        // 剪切板
        if (BooleanUtil.isTrue(redirectClipboard)) {
            list.add("redirectclipboard:i:1");
        } else {
            list.add("redirectclipboard:i:0");
        }
        // 域
        if (StringUtil.isNotBlank(domain)) {
            list.add("domain:s" + domain);
        }
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

}
