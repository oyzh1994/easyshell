package cn.oyzh.easyshell.zmodem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ZModem输入流
 *
 * @author oyzh
 * @since 2025/06/24
 */
public class ShellZModemInputStream extends InputStream {
    private final InputStream input;
    private final List<Byte> buffer;

    public ShellZModemInputStream(InputStream input, byte[] buffer) {
        this.input = input;
        this.buffer = new ArrayList<>();
        for (byte b : buffer) {
            this.buffer.add(b);
        }
    }

    @Override
    public int read() throws IOException {
        if (!this.buffer.isEmpty()) {
            return this.buffer.removeFirst();
        }
        return this.input.read();
    }
}