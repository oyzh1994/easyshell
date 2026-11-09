package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.StringUtil;

/**
 * 有状态的工作目录解析器。优先使用终端 OSC 上报的目录，失败时回退到提示符解析。
 * <p>
 * 终端输出按任意块大小到达，OSC 序列和提示符都可能跨块，因此应持续复用同一个实例。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public final class ShellWorkerDirResolver {

    /**
     * 当前未完成行的保留上限。
     */
    private static final int MAX_LINE_LENGTH = 4096;

    /**
     * 当前尚未结束的终端行。提示符和用户输入处于同一行，已结束的行不能再参与目录判断。
     */
    private final StringBuilder lineBuffer = new StringBuilder();

    private final ShellOscParser oscParser = new ShellOscParser();

    /**
     * 最近一次成功解析出的工作目录。后续输出没有新提示符时继续沿用它。
     */
    private String lastDir;

    /**
     * 解析一块终端输出
     *
     * @param output  输出
     * @param homeDir 用户home目录
     * @return 工作目录
     */
    public synchronized String resolve(String output, String homeDir) {
        if (StringUtil.isBlank(output)) {
            return null;
        }
        // OSC 7 是获取真实工作目录的标准方式，优先于提示符猜测
        String oscDir = this.oscParser.parseCwd(output, homeDir);
        if (StringUtil.isNotBlank(oscDir)) {
            // OSC 已提供准确目录，清掉可能残留的旧提示符
            this.lineBuffer.setLength(0);
            this.lastDir = oscDir;
            return oscDir;
        }
        this.lineBuffer.append(output);
        int lineStart = 0;
        for (int i = 0; i < this.lineBuffer.length(); i++) {
            char ch = this.lineBuffer.charAt(i);
            if (ch == '\r' || ch == '\n') {
                this.resolveLine(this.lineBuffer.substring(lineStart, i), homeDir);
                // CRLF 只结束一次当前行
                if (ch == '\r' && i + 1 < this.lineBuffer.length()
                        && this.lineBuffer.charAt(i + 1) == '\n') {
                    i++;
                }
                lineStart = i + 1;
            }
        }
        // 最后一行可能还有后续输入，保留下来以支持提示符跨输出块
        if (lineStart > 0) {
            this.lineBuffer.delete(0, lineStart);
        }
        this.resolveLine(this.lineBuffer.toString(), homeDir);
        if (this.lineBuffer.length() > MAX_LINE_LENGTH) {
            this.lineBuffer.delete(0, this.lineBuffer.length() - MAX_LINE_LENGTH);
        }
        return this.lastDir;
    }

    /**
     * 解析一个终端行，只有出现新的有效提示符时才更新目录。
     *
     * @param line    终端行
     * @param homeDir 用户home目录
     */
    private void resolveLine(String line, String homeDir) {
        String dir = ShellSSHUtil.resolvePromptWorkerDir(line, homeDir);
        if (StringUtil.isNotBlank(dir)) {
            this.lastDir = dir;
        }
    }

    /**
     * 清理跨块解析状态
     */
    public synchronized void reset() {
        this.lineBuffer.setLength(0);
        this.lastDir = null;
        this.oscParser.reset();
    }
}
