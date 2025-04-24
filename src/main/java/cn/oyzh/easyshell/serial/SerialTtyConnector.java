package cn.oyzh.easyshell.serial;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class SerialTtyConnector extends ShellDefaultTtyConnector {

    private SerialClient client;

    private SerialDataListener listener;

    public void initSerial(SerialClient client) {
        this.client = client;
        this.listener = new SerialDataListener(client.getSerialPort());
        this.client.addDataListener(this.listener);
    }

    public SerialTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        try {
            int len = 0;
            while (!this.listener.isEmpty()) {
                Character charset = this.listener.takeChar();
                if (charset != null) {
                    buf[len++] = charset;
                } else {
                    break;
                }
                if (len >= length) {
                    break;
                }
            }
            if (len == 0) {
                Arrays.fill(buf, 0, buf.length, (char) 0);
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
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
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
    public void close() {
        super.close();
        this.client.close();
    }
}