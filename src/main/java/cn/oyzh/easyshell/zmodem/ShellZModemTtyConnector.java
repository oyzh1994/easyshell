package cn.oyzh.easyshell.zmodem;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TtyConnector;
import org.jetbrains.annotations.NotNull;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.IOException;
import java.util.Arrays;

/**
 * ZModem协议tty连接器
 *
 * @author oyzh
 * @since 2025/06/24
 */
public class ShellZModemTtyConnector implements TtyConnector {

    /**
     * ZModem协议前缀
     */
    public static final char[] ZMODEM_PREFIX = new char[]{
            (char) ZModemCharacter.ZPAD.value(),
            (char) ZModemCharacter.ZPAD.value(),
            (char) ZModemCharacter.ZDLE.value()
    };

    /**
     * 终端容器
     */
    private Terminal terminal;

    /**
     * tty连接器
     */
    private ShellDefaultTtyConnector connector;

    /**
     * ZModem处理器
     */
    private volatile ShellZModemProcessor processor;

    public ShellDefaultTtyConnector getConnector() {
        return connector;
    }

    public ShellZModemTtyConnector(Terminal terminal, ShellDefaultTtyConnector connector) {
        this.terminal = terminal;
        this.connector = connector;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        // 处理ZModem
        if (this.processor != null) {
            this.processor.process();
            this.processor = null;
        }

        int i = this.connector.read(buffer, offset, length);
        if (i <= 3) {
            return i;
        }

        char[] bufferSlice = Arrays.copyOfRange(buffer, 0, i);
        int e = indexOfZModem(bufferSlice);
        if (e == -1) {
            return i;
        }
        char[] frame = Arrays.copyOfRange(buffer, e, i);
        // 创建ZModem处理器
        this.processor = new ShellZModemProcessor(
                frame,
                this.connector.input(),
                this.connector.output(),
                this.terminal
        );
        return e;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        // 取消ZModem
        if (this.processor != null && bytes.length > 0 && bytes[0] == 0x03) {
            this.processor.cancel();
            return;
        }
        this.connector.write(bytes);
    }

    @Override
    public void write(String string) throws IOException {
        this.connector.write(string);
    }

    @Override
    public boolean isConnected() {
        return this.connector != null && this.connector.isConnected();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return this.connector.waitFor();
    }

    @Override
    public boolean ready() throws IOException {
        return this.connector.ready();
    }

    @Override
    public String getName() {
        return this.connector.getName();
    }

    @Override
    public void close() {
        if (this.connector != null) {
            this.terminal = null;
            this.connector = null;
            this.processor = null;
            if (JulLog.isInfoEnabled()) {
                JulLog.info("close ZModem tty");
            }
        }
    }

    /**
     * 获取ZModem协议前缀位置
     *
     * @param a 数组
     * @return 结果
     */
    private static int indexOfZModem(char[] a) {
        if (a.length >= ZMODEM_PREFIX.length) {
            for (int i = 0; i <= a.length - ZMODEM_PREFIX.length; i++) {
                if (a[i] != ZMODEM_PREFIX[0]) {
                    continue;
                }
                if (a[i + 1] != ZMODEM_PREFIX[1]) {
                    continue;
                }
                if (a[i + 2] != ZMODEM_PREFIX[2]) {
                    continue;
                }
                // char[] range = Arrays.copyOfRange(a, i, i + ZMODEM_PREFIX.length);
                // if (Arrays.equals(range, ZMODEM_PREFIX)) {
                //     return i;
                // }
                return i;
            }
        }
        return -1;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        this.connector.resize(termSize);
    }
}