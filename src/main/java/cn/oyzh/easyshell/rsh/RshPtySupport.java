package cn.oyzh.easyshell.rsh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;

/**
 * rsh 伪终端支持。
 *
 * <p>rsh/rcmd 本身不分配伪终端，stdin/stdout 直接连接 socket，导致远端 shell 没有作业控制、
 * Ctrl+C 无法转成 SIGINT、readline 也不能正常工作。这里在远端启动一个 pty 包装器，
 * 优先使用 python3 的 pty 模块，其次退回 script 命令，最后退回普通 shell。</p>
 *
 * @author oyzh
 * @since 2026-11-14
 */
final class RshPtySupport {

    /**
     * pty 能力握手标记前缀，由远端包装器启动时输出，客户端读取后从终端输出中剔除。
     */
    static final String PTY_HANDSHAKE_PREFIX = "\u001b]9999;rshpty;";

    /**
     * pty 尺寸调整指令前缀，通过远端标准输入发送，由包装器拦截。
     */
    private static final String RESIZE_PREFIX = "\u0000\u0000RSZ;";

    /**
     * 默认终端行数。
     */
    private static final int DEFAULT_ROWS = 24;

    /**
     * 默认终端列数。
     */
    private static final int DEFAULT_COLS = 80;

    /**
     * 远端 pty 包装器脚本的 base64 编码，避免远端 shell 引号转义问题。
     */
    private static final String PYTHON_BASE64 = Base64.getEncoder()
            .encodeToString(compress(loadPythonScript()));

    private RshPtySupport() {
    }

    /**
     * 构造交互式 shell 启动命令。
     *
     * @param termType 终端类型
     * @return shell 命令
     */
    static String buildCommand(String termType) {
        return buildCommand(termType, DEFAULT_ROWS, DEFAULT_COLS);
    }

    /**
     * 构造交互式 shell 启动命令。
     *
     * @param termType 终端类型
     * @param rows     终端行数
     * @param cols     终端列数
     * @return shell 命令
     */
    static String buildCommand(String termType, int rows, int cols) {
        String term = termType == null || termType.isBlank() ? "xterm" : termType;
        if (rows <= 0) {
            rows = DEFAULT_ROWS;
        }
        if (cols <= 0) {
            cols = DEFAULT_COLS;
        }
        String quotedTerm = shellQuote(term);
        StringBuilder command = new StringBuilder(4096);
        // 优先使用 python3 创建真正的 pty，支持动态调整窗口尺寸
        if (!PYTHON_BASE64.isEmpty()) {
            String pythonCode = "import base64,zlib;exec(zlib.decompress(base64.b64decode(\""
                    + PYTHON_BASE64 + "\")).decode())";
            command.append("if command -v python3 >/dev/null 2>&1 && python3 -c ")
                    .append(shellQuote("import pty,fcntl,termios,select,struct,signal,zlib"))
                    .append(" >/dev/null 2>&1; then exec env TERM=").append(quotedTerm)
                    .append(" python3 -c ").append(shellQuote(pythonCode))
                    .append(' ').append(rows).append(' ').append(cols).append(' ')
                    .append(quotedTerm).append(" \"${SHELL:-/bin/sh}\"; fi; ");
        }
        // 退回 script，能创建 pty，但无法动态调整尺寸
        String scriptCommand = "stty rows " + rows + " cols " + cols
                + " 2>/dev/null; exec env TERM=" + quotedTerm + " \"${SHELL:-/bin/sh}\" -i";
        command.append("if command -v script >/dev/null 2>&1 && script -q -c true /dev/null </dev/null >/dev/null 2>&1; then exec script -q -c ")
                .append(shellQuote(scriptCommand)).append(" /dev/null; fi; ");
        // 最后退回普通流式 shell
        command.append("exec env TERM=").append(quotedTerm).append(" \"${SHELL:-/bin/sh}\" -i");
        return command.toString();
    }

    /**
     * 构造终端尺寸调整指令。
     *
     * @param rows 行数
     * @param cols 列数
     * @return 指令
     */
    static String resizeSequence(int rows, int cols) {
        return RESIZE_PREFIX + rows + ";" + cols + '\0';
    }

    /**
     * 加载远端 pty 包装器脚本。
     *
     * @return 脚本内容
     */
    private static String loadPythonScript() {
        try (InputStream inputStream = RshPtySupport.class.getResourceAsStream("/rsh/rsh_pty.py")) {
            if (inputStream == null) {
                return "";
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * 压缩远端脚本，压缩后通过 base64 传输，避免 rshd 命令长度限制。
     *
     * @param script 脚本内容
     * @return 压缩后的数据
     */
    private static byte[] compress(String script) {
        if (script.isEmpty()) {
            return new byte[0];
        }
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        try {
            deflater.setInput(script.getBytes(StandardCharsets.UTF_8));
            deflater.finish();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (!deflater.finished()) {
                int length = deflater.deflate(buffer);
                output.write(buffer, 0, length);
            }
            return output.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /**
     * 使用单引号包装 shell 参数。
     *
     * @param value 参数
     * @return 包装后的参数
     */
    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
