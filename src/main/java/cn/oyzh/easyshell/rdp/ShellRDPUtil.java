package cn.oyzh.easyshell.rdp;

import cn.oyzh.common.util.TextUtil;
import com.sun.jna.platform.win32.Crypt32Util;

import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2025-09-12
 */
public class ShellRDPUtil {

    public static String cryptRdpPassword(String password) {
        byte[] bytes = Crypt32Util.cryptProtectData(password.getBytes(StandardCharsets.UTF_16LE));
        return TextUtil.bytesToHexStr(bytes);
    }

}
