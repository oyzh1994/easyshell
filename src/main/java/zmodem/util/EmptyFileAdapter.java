package zmodem.util;

import java.io.InputStream;
import java.io.OutputStream;

public class EmptyFileAdapter implements FileAdapter {
    public static final EmptyFileAdapter INSTANCE = new EmptyFileAdapter();

    private EmptyFileAdapter() {}

    public static EmptyFileAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public InputStream getInputStream() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public OutputStream getOutputStream(boolean append) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public FileAdapter getChild(String name) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public long length() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean exists() {
        return true;
    }
}