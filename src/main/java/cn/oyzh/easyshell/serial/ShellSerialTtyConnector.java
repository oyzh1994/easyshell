package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellSerialTtyConnector extends ShellDefaultTtyConnector {

    private ShellSerialClient client;

    private ShellSerialDataListener listener;

    public void initSerial(ShellSerialClient client) {
        this.client = client;
        this.listener = new ShellSerialDataListener();
        this.client.addDataListener(this.listener);
    }

    public ShellSerialTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
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
        JulLog.debug("shell write : {}", str);
        byte[] bytes = str.getBytes(this.myCharset);
        this.client.write(bytes);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        super.write(bytes);
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        this.client.write(bytes);
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && this.client.isConnected();
    }

    @Override
    public void close() {
        super.close();
        this.client.close();
    }
}