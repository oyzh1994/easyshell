package cn.oyzh.easyshell.zmodem;

import java.io.IOException;
import java.io.InputStream;

/**
 * zmodem输入流
 *
 * @author oyzh
 * @since 20025/06/24
 */
public class ZModemInputStream extends InputStream {
    private final InputStream input;
    private final byte[] buffer;
    private int index = 0;

    public ZModemInputStream(InputStream input, byte[] buffer) {
        this.input = input;
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        if (this.index < buffer.length) {
            return this.buffer[index++];
        }
        return this.input.read();
    }
}