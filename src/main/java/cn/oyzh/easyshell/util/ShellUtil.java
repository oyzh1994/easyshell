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

    //    public static String fixWindowsFilePath(String filePath) {
    //        if (filePath.startsWith("/")) {
    //            filePath = filePath.substring(1);
    //        }
    //        return StringUtil.replace(filePath, "/", "\\");
    //    }

    //    public static String reverseWindowsFilePath(String filePath) {
    //        if (!filePath.startsWith("/")) {
    //            filePath = "/" + filePath;
    //        }
    //        filePath = StringUtil.replace(filePath, "\\", "/");
    //        return StringUtil.replace(filePath, "//", "/");
    //    }

    //    public static String permission(String permission) {
    //        int[] permissions = new int[3];
    //        for (int i = 0; i < 3; i++) {
    //            int start = i * 3;
    //            int octal = 0;
    //            if (permission.charAt(start) == 'r') {
    //                octal += 4;
    //            }
    //            if (permission.charAt(start + 1) == 'w') {
    //                octal += 2;
    //            }
    //            if (permission.charAt(start + 2) == 'x') {
    //                octal += 1;
    //            }
    //            permissions[i] = octal;
    //        }
    //        return permissions[0] + "" + permissions[1] + permissions[2];
    //    }

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

    /**
     * 将wmic的两行表格结果（第一行为列名，第二行为值）转换为 "列名 : 值" 文本
     *
     * @param output wmic命令结果
     * @return 文本
     */
    public static String wmicTableToText(String output) {
        if (StringUtil.isBlank(output)) {
            return "";
        }
        try {
            String[] lines = output.split("\n");
            if (lines.length < 2) {
                return output;
            }
            String[] cols1 = lines[0].split("\\s+");
            String[] cols2 = lines[1].splitWithDelimiters("\\s+", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cols1.length; i++) {
                sb.append(cols1[i]).append(" : ").append(cols2[i]).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return output;
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
     * 从unix、macos、linux命令输出获取字符集名称
     *
     * @param lang 命令结果
     * @return 字符集
     */
    public static String getCharsetFromLang(String lang) {
        if (StringUtil.contains(lang, ".")) {
            return lang.substring(lang.lastIndexOf(".") + 1);
        }
        return lang;
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
