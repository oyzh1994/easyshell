package cn.oyzh.easyshell.ssh;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Reader;

/**
 * 修复 Windows GBK 控制台把 UTF-8 BOM 转换后产生的行首残片。
 * conhost 在代码页 936 下会把 EF BB BF 转成 Unicode 的“锘?”再通过 SSH 输出，
 * 该过程不可逆，只能在展示层移除这个已知的行首残片和原样 BOM 字符。
 */
final class ShellWindowsBomReader extends Reader {

    private static final char UTF8_BOM = '\uFEFF';
    private static final char GBK_BOM_FIRST = '\u9518';
    private static final char INVALID_GBK_REPLACEMENT = '?';

    private final Reader delegate;
    private final char[] source = new char[1024];
    private int sourcePos;
    private int sourceLimit;
    private int pushedChar = -1;
    private boolean lineStart = true;
    private boolean closed;

    ShellWindowsBomReader(Reader delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read(char @NonNull [] buffer, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset + length > buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        if (this.closed) {
            throw new IOException("reader is closed");
        }
        int count = 0;
        while (true) {
            int ch = this.nextChar();
            if (ch < 0) {
                return count == 0 ? -1 : count;
            }
            if (this.lineStart) {
                if (ch == UTF8_BOM) {
                    continue;
                }
                if (ch == GBK_BOM_FIRST) {
                    int next = this.nextChar();
                    if (next == INVALID_GBK_REPLACEMENT || next == '\uFFFD') {
                        continue;
                    }
                    if (next < 0) {
                        buffer[offset + count++] = (char) ch;
                        return count;
                    }
                    this.pushedChar = next;
                }
            }
            buffer[offset + count++] = (char) ch;
            this.lineStart = ch == '\r' || ch == '\n';
            if (count >= length) {
                break;
            }
            if (this.sourcePos >= this.sourceLimit && !this.delegate.ready()) {
                break;
            }
        }
        return count;
    }

    private int nextChar() throws IOException {
        if (this.pushedChar >= 0) {
            int ch = this.pushedChar;
            this.pushedChar = -1;
            return ch;
        }
        if (this.sourcePos >= this.sourceLimit) {
            this.sourceLimit = this.delegate.read(this.source, 0, this.source.length);
            this.sourcePos = 0;
            if (this.sourceLimit < 0) {
                return -1;
            }
        }
        return this.source[this.sourcePos++];
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        this.delegate.close();
    }
}
