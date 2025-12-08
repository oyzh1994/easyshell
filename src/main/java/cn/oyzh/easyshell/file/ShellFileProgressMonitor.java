package cn.oyzh.easyshell.file;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * 文件进度监听器
 *
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFileProgressMonitor {

    public static InputStream of(InputStream in, Function<Long, Boolean> callback) throws IOException {
        if (in instanceof FileInputStream fIn) {
            return new ShellFileInputStream2(fIn, callback);
        }
        return new ShellFileInputStream(in, callback);
    }

    public static InputStream of2(FileInputStream in, Function<Long, Boolean> callback) throws IOException {
        return new ShellFileInputStream2(in, callback);
    }

    // public static InputStream of3(FileInputStream in, Function<Long, Boolean> callback) throws IOException {
    //     return new ShellFileInputStream3(in, callback);
    // }

    public static OutputStream of(OutputStream out, Function<Long, Boolean> callback) {
        return new ShellFileOutputStream(out, callback);
    }

    /**
     * shell文件输入流
     */
    public static class ShellFileInputStream extends InputStream {

        private InputStream in;

        private Function<Long, Boolean> callback;

        public ShellFileInputStream(InputStream in, Function<Long, Boolean> callback) {
            this.in = in;
            this.callback = callback;
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void mark(int readlimit) {
            in.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            in.reset();
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

        @Override
        public int read() throws IOException {
            this.applyCallback(1);
            return in.read();
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            int l = this.in.read(b);
            this.applyCallback(l);
            return l;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int l = this.in.read(b, off, len);
            this.applyCallback(l);
            return l;
        }

        @Override
        public byte @NotNull [] readAllBytes() throws IOException {
            byte[] bytes = in.readAllBytes();
            this.applyCallback(bytes.length);
            return bytes;
        }

        @Override
        public byte @NotNull [] readNBytes(int len) throws IOException {
            byte[] bytes = in.readNBytes(len);
            this.applyCallback(bytes.length);
            return bytes;
        }

        @Override
        public int readNBytes(byte @NotNull [] b, int off, int len) throws IOException {
            int l = in.readNBytes(b, off, len);
            this.applyCallback(l);
            return l;
        }

        @Override
        public long skip(long n) throws IOException {
            return in.skip(n);
        }

        @Override
        public void skipNBytes(long n) throws IOException {
            in.skipNBytes(n);
        }

        private void applyCallback(int len) throws InterruptedIOException {
            if (!this.callback.apply((long) len)) {
                throw new InterruptedIOException();
            }
        }

        @Override
        public void close() throws IOException {
            if (this.in != null) {
                this.in.close();
                this.in = null;
                this.callback = null;
            }
        }

        @Override
        public long transferTo(OutputStream out) throws IOException {
            return in.transferTo(out);
        }
    }

    /**
     * shell文件输入流2
     */
    public static class ShellFileInputStream2 extends FileInputStream {

        protected Function<Long, Boolean> callback;

        public ShellFileInputStream2(FileInputStream in, Function<Long, Boolean> callback) throws IOException {
            super(in.getFD());
            this.callback = callback;
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            int l = super.read(b);
            this.applyCallback(l);
            return l;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int l = super.read(b, off, len);
            this.applyCallback(l);
            return l;
        }

        @Override
        public byte @NotNull [] readNBytes(int len) throws IOException {
            byte[] bytes = super.readNBytes(len);
            int l = bytes.length;
            this.applyCallback(l);
            return bytes;
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            int l = super.readNBytes(b, off, len);
            this.applyCallback(l);
            return l;
        }

        protected void applyCallback(int len) throws InterruptedIOException {
            if (!this.callback.apply((long) len)) {
                throw new InterruptedIOException();
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.callback = null;
        }
    }

    /**
     * shell文件输入流3
     */
    public static class ShellFileInputStream3 extends ShellFileInputStream2 {

        public ShellFileInputStream3(FileInputStream in, Function<Long, Boolean> callback) throws IOException {
            super(in, callback);
        }

        @Override
        protected void applyCallback(int len) throws InterruptedIOException {
            if (!this.callback.apply((long) len / 2)) {
                throw new InterruptedIOException();
            }
        }
    }

    /**
     * shell文件输出流
     */
    public static class ShellFileOutputStream extends OutputStream {

        private OutputStream out;

        private Function<Long, Boolean> callback;

        public ShellFileOutputStream(OutputStream out, Function<Long, Boolean> callback) {
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
                throw new InterruptedIOException();
            }
        }

        @Override
        public void flush() throws IOException {
            this.out.flush();
        }

        @Override
        public void close() throws IOException {
            if (this.out != null) {
                this.out.flush();
                this.out.close();
                this.out = null;
                this.callback = null;
            }
        }
    }
}
