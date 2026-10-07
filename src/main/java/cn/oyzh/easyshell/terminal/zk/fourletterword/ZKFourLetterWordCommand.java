package cn.oyzh.easyshell.terminal.zk.fourletterword;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.client.FourLetterWordMain;
import org.apache.zookeeper.common.X509Exception;

import java.io.IOException;

/**
 * zk四字命令
 *
 * @author oyzh
 * @since 2024-11-29
 */
public abstract class ZKFourLetterWordCommand {

    /**
     * 命令
     */
    private final String cmd;

    /**
     * 别名
     */
    private final String alias;

    /**
     * 获取命令
     *
     * @return 命令
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * 获取别名
     *
     * @return 别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 构造方法
     *
     * @param cmd 命令
     */
    public ZKFourLetterWordCommand(String cmd) {
        this(cmd, null);
    }

    /**
     * 构造方法
     *
     * @param cmd   命令
     * @param alias 别名
     */
    public ZKFourLetterWordCommand(String cmd, String alias) {
        this.cmd = cmd;
        this.alias = alias;
    }

    /**
     * 执行命令
     *
     * @param host 主机
     * @param port 端口
     * @return 执行结果
     * @throws KeeperException                    异常
     * @throws IOException                        异常
     * @throws InterruptedException               异常
     * @throws X509Exception.SSLContextException 异常
     */
    public String exec(String host, int port) throws KeeperException, IOException, InterruptedException, X509Exception.SSLContextException {
        return FourLetterWordMain.send4LetterWord(host, port, this.cmd);
    }
}
