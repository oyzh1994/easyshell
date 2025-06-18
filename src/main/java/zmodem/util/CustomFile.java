package zmodem.util;

import org.apache.commons.io.input.RandomAccessFileInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class CustomFile implements FileAdapter {
    File file = null;

    public CustomFile(File file) {
        super();
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
       //return new FileInputStream(file);
        return RandomAccessFileInputStream.builder()
                .setCloseOnClose(true)
                .setRandomAccessFile(new RandomAccessFile(file, "r"))
                .setBufferSize(1024 * 8)
                .get();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return getOutputStream(false);
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file, append));
    }

    @Override
    public FileAdapter getChild(String name) {
        if (name.equals(file.getName())) {
            return this;
        } else if (file.isDirectory()) {
            return new CustomFile(new File(file.getAbsolutePath(), name));
        }
        return null;

    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

}
