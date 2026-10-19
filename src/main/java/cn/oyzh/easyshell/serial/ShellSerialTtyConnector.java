package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.fx.tty.TtyStreamConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 串口Tty连接器
 *
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellSerialTtyConnector extends TtyStreamConnector {

    /**
     * 串口客户端
     */
    private ShellSerialClient client;

    /**
     * 串口数据监听器
     */
    private ShellSerialDataListener listener;

    /**
     * 构造函数
     *
     * @param client 串口客户端
     */
    public ShellSerialTtyConnector(ShellSerialClient client ) {
        super(client.getCharset());
        this.client = client;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        try {
            int len = 0;
            while (!this.listener.isEmpty()) {
                Character charset = this.listener.takeChar();
                if (charset == null) {
                    break;
                }
                buf[len++] = charset;
                // 已填充满则结束
                if (len >= length) {
                    break;
                }
            }
            // 填充其他数据为0
            if (len == 0) {
                Arrays.fill(buf, 0, buf.length, (char) 0);
            } else if (len != length) {
                Arrays.fill(buf, len, length, (char) 0);
            }
            return len == 0 ? 1 : len;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void write(String str) throws IOException {
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        byte[] bytes = str.getBytes(this.charset());
        this.client.write(bytes);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        super.write(bytes);
        String str = new String(bytes, this.charset());
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        this.client.write(bytes);
    }

    @Override
    public boolean isConnected() {
        return this.client.isConnected();
    }

    @Override
    public boolean ready() throws IOException {
        if (this.listener == null) {
            this.listener = new ShellSerialDataListener(this.charset());
            this.client.addDataListener(this.listener);
        }
        return super.ready();
    }

    @Override
    public String getName() {
        return "serial-tty";
    }

    @Override
    public void close() {
        super.close();
        IOUtil.close(this.client);
        //        this.client.close();
        this.client = null;
    }

    @Override
    public InputStream input() {
        return null;
    }

    @Override
    public OutputStream output() {
        return null;
    }
}