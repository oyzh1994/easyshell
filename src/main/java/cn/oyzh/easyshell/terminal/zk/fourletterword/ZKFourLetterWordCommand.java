package cn.oyzh.easyshell.terminal.zk.fourletterword;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.client.FourLetterWordMain;
import org.apache.zookeeper.common.X509Exception;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2024-11-29
 */
public abstract class ZKFourLetterWordCommand {

    private final String cmd;

    private final String alias;

    public String getCmd() {
        return cmd;
    }

    public String getAlias() {
        return alias;
    }

    public ZKFourLetterWordCommand(String cmd) {
        this(cmd, null);
    }

    public ZKFourLetterWordCommand(String cmd, String alias) {
        this.cmd = cmd;
        this.alias = alias;
    }

    public String exec(String host, int port) throws KeeperException, IOException, InterruptedException, X509Exception.SSLContextException {
        return FourLetterWordMain.send4LetterWord(host, port, this.cmd);
    }
}
