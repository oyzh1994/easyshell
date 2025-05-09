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
public abstract class ShellFTPProgressMonitor {

    public ShellFTPInputStream init(InputStream in) {
     return    new ShellFTPInputStream(in, this::count);
    }

    public ShellFTPOuputStream init(OutputStream out) {
        return   new ShellFTPOuputStream(out, this::count);
    }

    public abstract boolean count(long count);

    public static class ShellFTPInputStream extends InputStream {

        private InputStream in;

        private Function<Integer, Boolean> callback;

        public ShellFTPInputStream(InputStream in, Function<Integer, Boolean> callback) {
            this.in = in;
            this.callback = callback;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte @NotNull [] b ) throws IOException {
            int l = this.in.read(b);
            if (!this.callback.apply(l)) {
                throw new InterruptedIOException();
            }
            return l;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int l = this.in.read(b, off, len);
            if (!this.callback.apply(l)) {
                throw new IOException();
            }
            return l;
        }
    }

    public static class ShellFTPOuputStream extends OutputStream {

        private OutputStream out;

        private Function<Integer, Boolean> callback;

        public ShellFTPOuputStream(OutputStream out, Function<Integer, Boolean> callback) {
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
            if (!this.callback.apply(len)) {
                throw new IOException();
            }
        }
    }
}
