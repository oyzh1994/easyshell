package cn.oyzh.easyshell.terminal.zk_v2;

/**
 * ZooKeeper 终端命令元数据。
 *
 * @param name  命令名
 * @param usage 参数用法
 * @param type  命令类型
 * @author oyzh
 * @since 2026-10-20
 */
public record ZKCommandDescriptor(String name, String usage, Type type) {

    /** ZooKeeper 命令执行类型。 */
    public enum Type {
        /** ZooKeeper CLI 命令。 */
        CLI,
        /** ZooKeeper 四字命令。 */
        FOUR_LETTER_WORD,
        /** 由终端自身处理的辅助命令。 */
        TERMINAL
    }
}
