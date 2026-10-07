package cn.oyzh.easyshell.local;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyProcessTtyConnector;
import com.pty4j.PtyProcess;

import java.util.List;

/**
 * 本地终端tty连接器
 *
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellLocalTtyConnector extends TtyProcessTtyConnector {

    /**
     * 本地客户端
     */
    private ShellLocalClient client;

    /**
     * 获取本地客户端
     *
     * @return 本地客户端
     */
    public ShellLocalClient getClient() {
        return client;
    }

    /**
     * 构造本地终端tty连接器
     *
     * @param client       本地客户端
     * @param process      进程
     * @param commandLines 命令行
     */
    public ShellLocalTtyConnector(ShellLocalClient client, PtyProcess process, List<String> commandLines) {
        super(process, client.getCharset(), commandLines);
        this.client = client;
    }

    @Override
    public String getName() {
        return "local-tty";
    }

    @Override
    public void close() {
        super.close();
        IOUtil.close(this.client);
        this.client = null;
    }
}