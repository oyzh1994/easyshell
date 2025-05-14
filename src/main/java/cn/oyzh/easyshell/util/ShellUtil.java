package cn.oyzh.easyshell.util;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.ssh.domain.SSHConnect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellUtil {

    public static boolean isCommandNotFound(String output) {
        return StringUtil.containsAnyIgnoreCase(output, "not found", "未找到命令", "不是内部或外部");
    }

    public static boolean isWindowsCommandNotFound(String output, String cmd) {
        return StringUtil.containsIgnoreCase(output, "'" + cmd + "'");
    }

    public static String fixWindowsFilePath(String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return StringUtil.replace(filePath, "/", "\\");
    }

    public static String reverseWindowsFilePath(String filePath) {
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        return StringUtil.replace(filePath, "\\", "/");
    }

    public static String permission(String permission) {
        int[] permissions = new int[3];
        for (int i = 0; i < 3; i++) {
            int start = i * 3;
            int octal = 0;
            if (permission.charAt(start) == 'r') {
                octal += 4;
            }
            if (permission.charAt(start + 1) == 'w') {
                octal += 2;
            }
            if (permission.charAt(start + 2) == 'x') {
                octal += 1;
            }
            permissions[i] = octal;
        }
        return permissions[0] + "" + permissions[1] + permissions[2];
    }

    public static String getWindowsCommandResult(String output) {
        if (StringUtil.isBlank(output)) {
            return "";
        }
        String[] arr = output.split("\n");
        if (arr.length < 2) {
            return "";
        }
        output = arr[1];
        return output.trim();
    }

    public static List<String> splitWindowsCommandResult(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < output.length(); i++) {
            char c = output.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString().trim());
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }
        result.add(currentValue.toString().trim());
        return result;
    }

    /**
     * 从windows命令输出获取字符集名称
     *
     * @param chcp 命令结果
     * @return 字符集
     */
    public static String getCharsetFromChcp(String chcp) {
        if (chcp.contains("437")) {
            return "iso-8859-1";
        }
        if (chcp.contains("936")) {
            return "gbk";
        }
        if (chcp.contains("950")) {
            return "big5";
        }
        if (chcp.contains("65001")) {
            return "utf-8";
        }
        return "gbk";
    }

    /**
     * 转换为ssh连接
     *
     * @param connect shell连接
     * @return ssh连接
     */
    public static SSHConnect toSSHConnect(ShellConnect connect) {
        SSHConnect sshConnect = new SSHConnect();
        sshConnect.setHost(connect.hostIp());
        sshConnect.setPort(connect.hostPort());
        return sshConnect;
    }
}
