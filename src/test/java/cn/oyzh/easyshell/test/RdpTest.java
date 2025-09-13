package cn.oyzh.easyshell.test;


import cn.hutool.core.io.FileUtil;
import com.sun.jna.platform.win32.Crypt32;
import com.sun.jna.platform.win32.Crypt32Util;
import com.sun.jna.platform.win32.WinCrypt;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Formatter;

/**
 * @author oyzh
 * @since 2025-09-12
 */
public class RdpTest {

    @Test
    public void test1(){
        getMstsc("192.168.3.71",3389,"rdp1","123456");
    }

    @Test
    public void test2(){
        getMstsc("192.168.3.156",3389,"oyzh","123456");
    }

    public static boolean getMstsc(String ip, int port, String username, String password) {
        File tempFile = new File("temp.rdp");
        ArrayList<String> list = new ArrayList<>();
        list.add("full address:s:" + ip + ":" + port);
        list.add("username:s:" + username);
        list.add("password 51:b:" + cryptRdpPassword(password));
        list.add("drivestoredirect:s:*");
        list.add("screen mode id:i:1");
        list.add("use multimon:i:0");
        list.add("desktopwidth:i:1920");
        list.add("desktopheight:i:1080");
        list.add("session bpp:i:32");
        list.add("winposstr:s:0,1,0,30,1680,1032");
        list.add("compression:i:1");
        list.add("keyboardhook:i:2");
        list.add("audiocapturemode:i:0");
        list.add("videoplaybackmode:i:1");
        list.add("connection type:i:7");
        list.add("networkautodetect:i:1");
        list.add("bandwidthautodetect:i:1");
        list.add("displayconnectionbar:i:1");
        list.add("enableworkspacereconnect:i:0");
        list.add("disable wallpaper:i:0");
        list.add("allow font smoothing:i:0");
        list.add("allow desktop composition:i:0");
        list.add("disable full window drag:i:1");
        list.add("disable menu anims:i:1");
        list.add("disable themes:i:0");
        list.add("disable cursor setting:i:0");
        list.add("bitmapcachepersistenable:i:1");
        list.add("audiomode:i:0");
        list.add("redirectprinters:i:1");
        list.add("redirectcomports:i:0");
        list.add("redirectsmartcards:i:1");
        list.add("redirectclipboard:i:1");
        list.add("redirectposdevices:i:0");
        list.add("autoreconnection enabled:i:1");
        list.add("authentication level:i:2");
        list.add("prompt for credentials:i:0");
        list.add("negotiate security layer:i:1");
        list.add("remoteapplicationmode:i:0");
        list.add("alternate shell:s:");
        list.add("shell working directory:s:");
        list.add("gatewayhostname:s:");
        list.add("gatewayusagemethod:i:4");
        list.add("gatewaycredentialssource:i:4");
        list.add("gatewayprofileusagemethod:i:0");
        list.add("promptcredentialonce:i:0");
        list.add("gatewaybrokeringtype:i:0");
        list.add("use redirection server name:i:0");
        list.add("rdgiskdcproxy:i:0");
        list.add("kdcproxyname:s:");
        list.add("redirectlocation:i:0");
        FileUtil.writeUtf8Lines(list, tempFile);
        return cmd("mstsc temp.rdp");
    }

    public static boolean cmd(String command) {
        boolean flag = false;
        try {
            Runtime.getRuntime().exec("cmd.exe /c " + command);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static String cryptRdpPassword0(String password) {
        WinCrypt.DATA_BLOB pDataIn = new WinCrypt.DATA_BLOB(password.getBytes(StandardCharsets.UTF_16LE));
        WinCrypt.DATA_BLOB pDataEncrypted = new WinCrypt.DATA_BLOB();
        Crypt32.INSTANCE.CryptProtectData(pDataIn, "psw", null, null, null, 1, pDataEncrypted);
        StringBuffer epwsb = new StringBuffer();
        byte[] pwdBytes = new byte[pDataEncrypted.cbData];
        pwdBytes = pDataEncrypted.getData();
        Formatter formatter = new Formatter(epwsb);
        for (final byte b : pwdBytes) {
            formatter.format("%02X", b);
        }
        return epwsb.toString();
    }

    public static String cryptRdpPassword(String password) {
        return toHexString(Crypt32Util.cryptProtectData(password.getBytes(StandardCharsets.UTF_16LE)));
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02X", b);
        }
        formatter.close();
        return sb.toString();
    }
    public static String decodeRdpPassword(String password) {
        try {
            return new String(Crypt32Util.cryptUnprotectData(toBytes(password)), "UTF-16LE");
        } catch (Exception e1) {
            e1.printStackTrace();
            return "ERROR";
        }
    }
    public static byte[] toBytes(String str) {// 去掉0x以后,转整数再转型成字节
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }




}
