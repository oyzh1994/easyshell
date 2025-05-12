package cn.oyzh.easyshell.ftp;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFTPProgressMonitor {

    public static ShellFTPInputStream of(InputStream in, Function<Long, Boolean> callback) {
        return new ShellFTPInputStream(in, callback);
    }

    public static ShellFTPOuputStream of(OutputStream out, Function<Long, Boolean> callback) {
        return new ShellFTPOuputStream(out, callback);
    }

    public static class ShellFTPInputStream extends InputStream {

        private InputStream in;

        private Function<Long, Boolean> callback;

        public ShellFTPInputStream(InputStream in, Function<Long, Boolean> callback) {
            this.in = in;
            this.callback = callback;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            int l = this.in.read(b);
            if (!this.callback.apply((long) l)) {
                throw new InterruptedIOException();
            }
            return l;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int l = this.in.read(b, off, len);
            if (!this.callback.apply((long) l)) {
                throw new IOException();
            }
            return l;
        }

        @Override
        public void close() throws IOException {
            this.in.close();
            this.in = null;
            this.callback = null;
        }
    }

    public static class ShellFTPOuputStream extends OutputStream {

        private OutputStream out;

        private Function<Long, Boolean> callback;

        public ShellFTPOuputStream(OutputStream out, Function<Long, Boolean> callback) {
            this.out = out;
            this.callback = callback;
        }

        @Override
        public void write(int b) throws IOException {
            this.out.write(b);
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            this.out.write(b, off, len);
            if (!this.callback.apply((long) len)) {
                throw new IOException();
            }
        }

        @Override
        public void flush() throws IOException {
            this.out.flush();
        }

        @Override
        public void close() throws IOException {
            this.out.flush();
            this.out.close();
            this.out = null;
            this.callback = null;
        }
    }
}
