package cn.oyzh.easyshell.rsh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * rsh 输入流。
 *
 * <p>读取远端 pty 包装器的启动握手，并把握手标记从终端输出中剔除。
 * 非 pty 模式（script 或普通 shell）不会输出握手标记，读取到首个普通字节后即结束探测。</p>
 *
 * @author oyzh
 * @since 2026-11-14
 */
final class RshPtyInputStream extends FilterInputStream {

    /**
     * 握手标记前缀。
     */
    private static final byte[] HANDSHAKE_PREFIX = RshPtySupport.PTY_HANDSHAKE_PREFIX
            .getBytes(StandardCharsets.US_ASCII);

    /**
     * 握手数据最大长度，避免异常数据导致一直缓冲。
     */
    private static final int MAX_HANDSHAKE_LENGTH = 64;

    /**
     * OSC 结束符。
     */
    private static final byte HANDSHAKE_END = 0x07;

    /**
     * 已读取但尚未返回的数据。
     */
    private byte[] pending = new byte[0];

    /**
     * 待返回数据偏移。
     */
    private int pendingOffset;

    /**
     * 是否已完成握手探测。
     */
    private boolean probed;

    /**
     * 远端 pty 是否支持尺寸调整。
     */
    private volatile boolean resizeSupported;

    RshPtyInputStream(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * 远端 pty 是否支持尺寸调整。
     *
     * @return 是否支持
     */
    boolean isResizeSupported() {
        return this.resizeSupported;
    }

    /**
     * 探测启动握手。调用方应设置合适的 socket 读取超时，超时后按不支持尺寸调整处理。
     *
     * @throws IOException io 异常
     */
    void probe() throws IOException {
        if (this.probed) {
            return;
        }
        this.probed = true;
        byte[] header = new byte[MAX_HANDSHAKE_LENGTH];
        int length = 0;
        int matched = 0;
        IOException failure = null;
        try {
            while (length < MAX_HANDSHAKE_LENGTH) {
                int value = super.read();
                if (value < 0) {
                    break;
                }
                header[length++] = (byte) value;
                if (matched < HANDSHAKE_PREFIX.length) {
                    if ((byte) value != HANDSHAKE_PREFIX[matched]) {
                        break;
                    }
                    matched++;
                    continue;
                }
                if (value == HANDSHAKE_END) {
                    String mode = new String(header, HANDSHAKE_PREFIX.length,
                            length - HANDSHAKE_PREFIX.length - 1, StandardCharsets.US_ASCII);
                    this.resizeSupported = "resize".equalsIgnoreCase(mode);
                    length = 0;
                    break;
                }
            }
        } catch (SocketTimeoutException ex) {
            // 非 pty 模式没有握手标记，保留已读取数据后继续
        } catch (IOException ex) {
            failure = ex;
        } finally {
            this.pending = Arrays.copyOf(header, length);
            this.pendingOffset = 0;
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public int read() throws IOException {
        byte[] buffer = new byte[1];
        int length = this.read(buffer, 0, 1);
        return length <= 0 ? -1 : buffer[0] & 0xff;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        if (!this.probed) {
            this.probe();
        }
        if (this.pendingOffset < this.pending.length) {
            int count = Math.min(length, this.pending.length - this.pendingOffset);
            System.arraycopy(this.pending, this.pendingOffset, buffer, offset, count);
            this.pendingOffset += count;
            if (this.pendingOffset >= this.pending.length) {
                this.pending = new byte[0];
                this.pendingOffset = 0;
            }
            return count;
        }
        return super.read(buffer, offset, length);
    }
}
