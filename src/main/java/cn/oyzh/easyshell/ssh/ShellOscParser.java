package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 终端 OSC 序列增量解析器。
 * <p>
 * 目前用于提取 OSC 7 和 OSC 9;9 上报的当前工作目录，并处理 OSC/ST 分片及 UTF-8
 * 百分号编码。解析器有状态，终端输出应按块持续传入同一个实例。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public final class ShellOscParser {

    /**
     * OSC 转义序列（如终端标题、超链接），removeAnsi 无法处理
     */
    private static final Pattern OSC_PATTERN = Pattern.compile("\u001B\\][^\u0007\u001B]*(?:\u0007|\u001B\\\\)");

    /**
     * OSC 内容上限，避免异常序列无上限占用内存。
     */
    private static final int MAX_OSC_LENGTH = 8192;

    private final StringBuilder oscBuffer = new StringBuilder();

    private OscState state = OscState.TEXT;

    /**
     * 解析一块终端输出中的 OSC 工作目录
     *
     * @param output 输出
     * @return 工作目录
     */
    public String parseCwd(String output) {
        return this.parseCwd(output, null);
    }

    /**
     * 解析一块终端输出中的 OSC 工作目录
     *
     * @param output  输出
     * @param homeDir 用户home目录
     * @return 工作目录
     */
    public synchronized String parseCwd(String output, String homeDir) {
        if (StringUtil.isBlank(output)) {
            return null;
        }
        String dir = this.parseOscDir(output);
        if (StringUtil.isBlank(dir)) {
            return null;
        }
        return normalizeOscWorkerDir(dir, homeDir);
    }

    /**
     * 清理跨块解析状态
     */
    public synchronized void reset() {
        this.oscBuffer.setLength(0);
        this.state = OscState.TEXT;
    }

    /**
     * 移除文本中完整的 OSC 序列
     *
     * @param text 文本
     * @return 移除后的文本
     */
    public static String removeOsc(String text) {
        if (text == null) {
            return null;
        }
        return OSC_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * 从输出中提取最后一条有效 OSC 工作目录
     *
     * @param output 输出
     * @return 目录
     */
    private String parseOscDir(String output) {
        String lastDir = null;
        for (int i = 0; i < output.length(); i++) {
            char ch = output.charAt(i);
            switch (this.state) {
                case TEXT -> {
                    if (ch == '\u001B') {
                        this.state = OscState.ESCAPE;
                    }
                }
                case ESCAPE -> {
                    if (ch == ']') {
                        this.oscBuffer.setLength(0);
                        this.state = OscState.OSC;
                    } else if (ch != '\u001B') {
                        this.state = OscState.TEXT;
                    }
                }
                case OSC -> {
                    if (ch == '\u0007') {
                        String dir = parseOscDirPayload(this.oscBuffer);
                        if (dir != null) {
                            lastDir = dir;
                        }
                        this.finishOsc();
                    } else if (ch == '\u001B') {
                        this.state = OscState.OSC_ESCAPE;
                    } else if (ch == '\r' || ch == '\n' || this.oscBuffer.length() >= MAX_OSC_LENGTH) {
                        // 未按协议终止的 OSC 不能吞掉后续普通输出
                        this.finishOsc();
                    } else {
                        this.oscBuffer.append(ch);
                    }
                }
                case OSC_ESCAPE -> {
                    if (ch == '\\') {
                        String dir = parseOscDirPayload(this.oscBuffer);
                        if (dir != null) {
                            lastDir = dir;
                        }
                        this.finishOsc();
                    } else if (ch == ']') {
                        // ESC 已中止上一条 OSC，当前 ESC] 开始新序列
                        this.oscBuffer.setLength(0);
                        this.state = OscState.OSC;
                    } else if (ch != '\u001B') {
                        this.finishOsc();
                    }
                }
            }
        }
        return lastDir;
    }

    private void finishOsc() {
        this.oscBuffer.setLength(0);
        this.state = OscState.TEXT;
    }

    /**
     * 解析 OSC 载荷中的工作目录
     *
     * @param payload OSC 载荷
     * @return 目录
     */
    private static String parseOscDirPayload(CharSequence payload) {
        String value = payload.toString();
        // OSC 7;file://host/path：xterm、VTE、kitty、foot、Windows Terminal 等通用
        if (StringUtil.startWith(value, "7;")) {
            return parseOsc7Dir(value.substring(2));
        }
        // OSC 9;9;path：ConEmu、Windows Terminal 的当前目录扩展
        if (StringUtil.startWith(value, "9;9;")) {
            String path = value.substring(4);
            if (StringUtil.startWithIgnoreCase(path, "file://")) {
                return parseOsc7Dir(path);
            }
            return normalizeOscCandidate(path);
        }
        return null;
    }

    /**
     * 解析 OSC 7 的 file URI
     *
     * @param uri file URI
     * @return 目录
     */
    private static String parseOsc7Dir(String uri) {
        if (!StringUtil.startWithIgnoreCase(uri, "file://")) {
            return null;
        }
        String authorityAndPath = uri.substring(7);
        int pathStart = authorityAndPath.indexOf('/');
        if (pathStart < 0) {
            return null;
        }
        String path = decodePercentEncodedPath(authorityAndPath.substring(pathStart));
        // file:///C:/path 是 Windows 上常见的 OSC 7 形式，去掉 URI 根斜杠后再规范化。
        if (path.length() >= 3 && path.charAt(0) == '/'
                && Character.isLetter(path.charAt(1)) && path.charAt(2) == ':') {
            path = path.substring(1);
        }
        return normalizeOscCandidate(path);
    }

    /**
     * 校验 OSC 上报的目录
     *
     * @param path 目录
     * @return 目录
     */
    private static String normalizeOscCandidate(String path) {
        if (StringUtil.isBlank(path) || containsControl(path)) {
            return null;
        }
        return isPathLike(path) ? path : null;
    }

    /**
     * 规范化 OSC 上报的目录
     *
     * @param dir     目录
     * @param homeDir 用户home目录
     * @return 目录
     */
    private static String normalizeOscWorkerDir(String dir, String homeDir) {
        if (isWindowsPath(dir)) {
            return ShellFileUtil.fixFilePath(dir);
        }
        if (homeDir != null) {
            if ("~".equals(dir)) {
                return homeDir;
            }
            if (StringUtil.startWith(dir, "~/")) {
                return ShellFileUtil.concat(homeDir, dir.substring(1));
            }
        }
        return isAbsolutePath(dir) ? dir : null;
    }

    /**
     * 解码 OSC 路径中的 UTF-8 百分号编码。OSC 路径不是 application/x-www-form-urlencoded，
     * 因此不能把 '+' 当作空格。
     *
     * @param path 原始路径
     * @return 解码后的路径
     */
    private static String decodePercentEncodedPath(String path) {
        if (path == null || path.indexOf('%') < 0) {
            return path;
        }
        StringBuilder result = new StringBuilder(path.length());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int index = 0;
        while (index < path.length()) {
            char ch = path.charAt(index);
            if (ch != '%') {
                result.append(ch);
                index++;
                continue;
            }
            int encodedStart = index;
            bytes.reset();
            while (index < path.length() && path.charAt(index) == '%') {
                if (index + 2 >= path.length()) {
                    break;
                }
                int high = hexValue(path.charAt(index + 1));
                int low = hexValue(path.charAt(index + 2));
                if (high < 0 || low < 0) {
                    break;
                }
                bytes.write((high << 4) | low);
                index += 3;
            }
            // 非法的百分号按普通字符保留。远端目录名可能本身包含 '%'，
            // 不能因为它不是合法的 URI 编码就丢弃整条 OSC 上报。
            if (index == encodedStart) {
                result.append('%');
                index++;
                continue;
            }
            try {
                result.append(StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes.toByteArray())));
            } catch (CharacterCodingException ex) {
                result.append(path, encodedStart, index);
            }
        }
        return result.toString();
    }

    /**
     * 十六进制字符转数字
     *
     * @param ch 字符
     * @return 数字，非法时返回-1
     */
    private static int hexValue(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f') {
            return ch - 'a' + 10;
        }
        if (ch >= 'A' && ch <= 'F') {
            return ch - 'A' + 10;
        }
        return -1;
    }

    /**
     * 是否为明显的路径
     *
     * @param path 路径
     * @return 结果
     */
    private static boolean isPathLike(String path) {
        if (StringUtil.isBlank(path)) {
            return false;
        }
        return StringUtil.startWithAny(path, "~", "/", "./", "../") || isWindowsPath(path);
    }

    /**
     * 是否为绝对路径
     *
     * @param path 路径
     * @return 结果
     */
    private static boolean isAbsolutePath(String path) {
        return StringUtil.startWith(path, "/") || isWindowsPath(path);
    }

    /**
     * 是否为Windows路径
     *
     * @param path 路径
     * @return 结果
     */
    private static boolean isWindowsPath(String path) {
        if (StringUtil.isBlank(path)) {
            return false;
        }
        if (path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0))) {
            return true;
        }
        return StringUtil.startWith(path, "\\\\") || StringUtil.startWith(path, "\\");
    }

    /**
     * 是否包含控制字符
     *
     * @param value 值
     * @return 结果
     */
    private static boolean containsControl(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < 0x20 || ch == 0x7F) {
                return true;
            }
        }
        return false;
    }

    private enum OscState {
        TEXT,
        ESCAPE,
        OSC,
        OSC_ESCAPE
    }
}
