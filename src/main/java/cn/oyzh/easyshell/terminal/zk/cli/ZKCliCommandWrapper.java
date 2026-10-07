package cn.oyzh.easyshell.terminal.zk.cli;

import org.apache.commons.cli.ParseException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.cli.CliCommand;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Cli命令包装器
 *
 * @author oyzh
 * @since 2023/9/20
 */
public class ZKCliCommandWrapper {

    /**
     * cli命令
     */
    private final CliCommand command;

    /**
     * 行结束文本
     */
    private final String lineEndingText;

    /**
     * 是否已初始化
     */
    private boolean initialized;

    /**
     * 响应消费者
     */
    private Consumer<String> onResponse;

    /**
     * 设置响应消费者
     *
     * @param onResponse 响应消费者
     */
    public void setOnResponse(Consumer<String> onResponse) {
        this.onResponse = onResponse;
    }

    /**
     * 获取响应消费者
     *
     * @return 响应消费者
     */
    public Consumer<String> getOnResponse() {
        return onResponse;
    }

    /**
     * 构造方法
     *
     * @param command        cli命令
     * @param zooKeeper      zk客户端
     * @param lineEndingText 行结束文本
     */
    public ZKCliCommandWrapper(CliCommand command, ZooKeeper zooKeeper, String lineEndingText) {
        this.command = command;
        this.lineEndingText = lineEndingText;
        this.init(zooKeeper);
    }

    /**
     * 初始化
     *
     * @param zooKeeper zk客户端
     */
    private void init(ZooKeeper zooKeeper) {
        if (!this.initialized) {
            this.initialized = true;
            this.command.setZk(zooKeeper);
            this.command.setOut(new ZKCliPrintStream(lineEndingText) {
                @Override
                public void onResponse(String str) {
                    if (onResponse != null) {
                        onResponse.accept(str);
                    }
                }
            });
            this.command.setErr(new ZKCliPrintStream(lineEndingText) {
                @Override
                public void onResponse(String response) {
                    if (onResponse != null) {
                        onResponse.accept(response);
                    }
                }
            });
        }
    }

    /**
     * 解析命令
     *
     * @param cmdArgs 命令及参数
     * @return 命令
     * @throws ParseException 异常
     */
    public CliCommand parse(String[] cmdArgs) throws ParseException {
        this.command.parse(cmdArgs);
        return this.command;
    }

    /**
     * 执行命令
     *
     * @return 结果
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws KeeperException      异常
     */
    public boolean exec() throws IOException, InterruptedException, KeeperException {
        return this.command.exec();
    }
}
