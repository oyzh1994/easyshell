package cn.oyzh.easyshell.ssh;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/** Windows GBK 控制台 BOM 残片清理的确定性回归。 */
public final class ShellWindowsBomReaderRegression {

    private ShellWindowsBomReaderRegression() {
    }

    public static void main(String[] args) {
        try {
            verify("\u9518?127.0.0.1 localhost", "127.0.0.1 localhost");
            verify("\u9518? 127.0.0.1 localhost", " 127.0.0.1 localhost");
            verify("\uFEFF# hosts", "# hosts");
            verify("line\r\n\u9518?127.0.0.1 localhost", "line\r\n127.0.0.1 localhost");
            verify("text \u9518? keep", "text \u9518? keep");
            verify("\u9518 keep", "\u9518 keep");
            verifyChunked("\u9518?127.0.0.1", "127.0.0.1");
            System.out.println("Windows BOM reader regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void verify(String input, String expected) throws IOException {
        try (ShellWindowsBomReader reader = new ShellWindowsBomReader(new StringReader(input))) {
            require(expected.equals(readAll(reader)), "unexpected BOM repair result for: " + input);
        }
    }

    private static void verifyChunked(String input, String expected) throws IOException {
        try (ShellWindowsBomReader reader = new ShellWindowsBomReader(new ChunkedReader(input))) {
            require(expected.equals(readAll(reader)), "unexpected chunked BOM repair result");
        }
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[64];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) >= 0) {
            if (read > 0) {
                result.append(buffer, 0, read);
            }
        }
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class ChunkedReader extends Reader {

        private final String value;
        private int offset;

        private ChunkedReader(String value) {
            this.value = value;
        }

        @Override
        public int read(char[] buffer, int targetOffset, int length) {
            if (this.offset >= this.value.length()) {
                return -1;
            }
            buffer[targetOffset] = this.value.charAt(this.offset++);
            return 1;
        }

        @Override
        public void close() {
        }
    }
}
