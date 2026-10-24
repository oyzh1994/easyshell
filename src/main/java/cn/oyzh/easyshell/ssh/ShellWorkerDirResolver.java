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
     * 未完成提示符的保留上限。
     */
    private static final int MAX_PROMPT_LENGTH = 4096;

    private final StringBuilder promptBuffer = new StringBuilder();

    private final ShellOscParser oscParser = new ShellOscParser();

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
            this.promptBuffer.setLength(0);
            return oscDir;
        }
        this.promptBuffer.append(output);
        if (this.promptBuffer.length() > MAX_PROMPT_LENGTH) {
            this.promptBuffer.delete(0, this.promptBuffer.length() - MAX_PROMPT_LENGTH);
        }
        // 提示符可能尚未接收完整，保留最近输出，下一块到达后可继续解析
        return ShellSSHUtil.resolvePromptWorkerDir(this.promptBuffer.toString(), homeDir);
    }

    /**
     * 清理跨块解析状态
     */
    public synchronized void reset() {
        this.promptBuffer.setLength(0);
        this.oscParser.reset();
    }
}
