package cn.oyzh.easyshell.terminal.zk_v2;

/**
 * ZooKeeper 终端命令元数据。
 *
 * @param name  命令名
 * @param usage 参数用法
 * @param type  命令类型
 */
public record ZKCommandDescriptor(String name, String usage, Type type) {

    public enum Type {
        CLI,
        FOUR_LETTER_WORD,
        TERMINAL
    }
}
