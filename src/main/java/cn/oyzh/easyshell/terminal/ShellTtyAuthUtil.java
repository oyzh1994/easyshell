package cn.oyzh.easyshell.terminal;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 交互式终端认证工具，用于识别 telnet、rlogin 等协议返回的登录提示。
 *
 * @author oyzh
 * @since 2026-10-26
 */
public final class ShellTtyAuthUtil {

    /**
     * 默认最大尝试次数
     */
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * 保留的尾部数据长度
     */
    private static final int MAX_BUFFER_LENGTH = 4096;

    /**
     * ANSI 控制序列。提示符可能被颜色等控制序列包裹。
     */
    private static final Pattern ANSI_ESCAPE = Pattern.compile(
            "\\x1B\\][^\\x07]*(?:\\x07|\\x1B\\\\)"
                    + "|\\x1B\\[[0-?]*[ -/]*[@-~]"
                    + "|\\x1B(?![\\[\\]])[@-_]");

    /**
     * 其他不可见的终端控制字符，保留换行和制表符。
     */
    private static final Pattern CONTROL_CHARACTER = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    /**
     * 用户名提示
     */
    private static final Pattern USERNAME_PROMPT = Pattern.compile(
            "(?i)^[\\t ]*(?!(?:last|previous|failed|successful|invalid)[\\t ]+[^\\r\\n:]*login[\\t ]*[:：])"
                    + "(?:[^\\r\\n]{0,128}[\\t ]+)?"
                    + "(?!(?:last|previous|failed|successful|invalid)[\\t ]+[^\\r\\n:]*login[\\t ]*[:：])"
                    + "(?:请(?:输入)?|输入|登录)?"
                    + "(?:login(?:[\\t ]+(?:as|name|id))?|user(?:[\\t ]*(?:id|name|account))?|"
                    + "account(?:[\\t ]*(?:id|name))?|登录(?:名|用户)?|用户名?|账号|帐号|账户|帐户)"
                    + "[\\t ]*[:：][\\t ]*$");

    /**
     * 密码提示
     */
    private static final Pattern PASSWORD_PROMPT = Pattern.compile(
            "(?i)^[\\t ]*(?:(?:(?:please|current|verify|confirm|enter|input|user|login|the|your|"
                    + "unix|windows|domain|local)[\\t ]+)*"
                    + "(?:password|passwd|passphrase|passcode|密码|口令)"
                    + "(?:[\\t ]+for[\\t ]+[^:\\r\\n]+)?"
                    + "|[^:\\r\\n]{1,128}(?:'s|’s)[\\t ]+password"
                    + "|(?:(?:请(?:输入)?|输入|用户|账户|帐号|确认|当前|您的?|登录)[\\t ]*)*(?:密码|口令))"
                    + "[\\t ]*[:：][\\t ]*$");

    /**
     * 常见 shell 或网络设备命令行提示符。认证过程中出现时表示已经进入命令交互阶段。
     */
    private static final Pattern SHELL_PROMPT = Pattern.compile(
            "^[\\t ]*[^\\r\\n]{0,512}[#$%>❯➜λ→»][\\t ]*$");

    private final StringBuilder tail = new StringBuilder(256);

    private final int maxAttempts;

    private int usernameAttempts;

    private int passwordAttempts;

    private boolean completed;

    public ShellTtyAuthUtil() {
        this(DEFAULT_MAX_ATTEMPTS);
    }

    public ShellTtyAuthUtil(int maxAttempts) {
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    /**
     * 处理终端输出，并在识别到认证提示时写入配置的用户名或密码。
     *
     * @param buffer     输出缓冲区
     * @param offset     起始位置
     * @param length     数据长度
     * @param username   用户名
     * @param password   密码
     * @param writer     终端输出流
     * @param lineEnding 行结束符
     * @throws IOException IO 异常
     */
    public synchronized void authenticate(char[] buffer, int offset, int length,
                                          String username, String password,
                                          Writer writer, String lineEnding) throws IOException {
        if (this.completed || writer == null || length <= 0) {
            return;
        }
        this.append(buffer, offset, length);
        AuthPrompt prompt = this.detectPrompt();
        if (prompt == AuthPrompt.NONE) {
            return;
        }
        // 提示符已经消费，避免同一次提示在下个输出块中重复提交。
        this.tail.setLength(0);
        String credential;
        if (prompt == AuthPrompt.USERNAME) {
            if (++this.usernameAttempts > this.maxAttempts || isBlank(username)) {
                return;
            }
            credential = username;
        } else {
            if (++this.passwordAttempts > this.maxAttempts || isBlank(password)) {
                return;
            }
            credential = password;
        }
        writer.write(credential);
        writer.write(lineEnding == null ? "\r" : lineEnding);
        writer.flush();
    }

    /**
     * 结束认证识别。登录成功后调用，避免后续终端内容中的 Password 文本触发自动输入。
     */
    public synchronized void complete() {
        this.completed = true;
        this.tail.setLength(0);
    }

    /**
     * 重置认证状态。
     */
    public synchronized void reset() {
        this.completed = false;
        this.usernameAttempts = 0;
        this.passwordAttempts = 0;
        this.tail.setLength(0);
    }

    private void append(char[] buffer, int offset, int length) {
        this.tail.append(buffer, offset, length);
        int overflow = this.tail.length() - MAX_BUFFER_LENGTH;
        if (overflow > 0) {
            this.tail.delete(0, overflow);
        }
    }

    private AuthPrompt detectPrompt() {
        String line = this.lastLine(this.normalize(this.tail.toString()));
        if (this.passwordAttempts > 0 && SHELL_PROMPT.matcher(line).matches()) {
            this.complete();
            return AuthPrompt.NONE;
        }
        Matcher passwordMatcher = PASSWORD_PROMPT.matcher(line);
        if (passwordMatcher.matches()) {
            return AuthPrompt.PASSWORD;
        }
        Matcher usernameMatcher = USERNAME_PROMPT.matcher(line);
        if (usernameMatcher.matches()) {
            return AuthPrompt.USERNAME;
        }
        return AuthPrompt.NONE;
    }

    private String lastLine(String output) {
        int end = output.length();
        while (end > 0 && Character.isWhitespace(output.charAt(end - 1))) {
            end--;
        }
        if (end == 0) {
            return "";
        }
        int start = output.lastIndexOf('\n', end - 1) + 1;
        return output.substring(start, end);
    }

    private String normalize(String output) {
        String value = ANSI_ESCAPE.matcher(output).replaceAll("");
        value = value.replace('\r', '\n');
        return CONTROL_CHARACTER.matcher(value).replaceAll("");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private enum AuthPrompt {
        NONE,
        USERNAME,
        PASSWORD
    }
}
