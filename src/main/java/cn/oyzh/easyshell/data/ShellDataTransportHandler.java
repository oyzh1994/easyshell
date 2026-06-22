package cn.oyzh.easyshell.data;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class ShellDataTransportHandler extends ShellDataHandler {

    /**
     * 执行传输
     *
     * @throws Exception 异常
     */
    public abstract void doTransport() throws Exception;
}

