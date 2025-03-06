package cn.oyzh.easyssh.sftp;

import com.jcraft.jsch.SftpProgressMonitor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpUploader implements SftpProgressMonitor {

    private List<File> files;

    private IntegerProperty progressProperty;

    public void addFile(List<File> files) {
        if (this.files == null) {
            this.files = new ArrayList<>(files);
        } else {
            this.files.addAll(files);
        }
    }

    public void addFile(File file) {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        this.files.add(file);
    }

    public int fileCount() {
        return this.files == null ? 0 : this.files.size();
    }

    public void progressProperty( ) {
        if (this.progressProperty == null) {
            this.progressProperty = new SimpleIntegerProperty(0);
        }
        this.progressProperty.set(this.fileCount());
    }

    @Override
    public void init(int i, String s, String s1, long l) {

    }

    @Override
    public boolean count(long l) {
        return false;
    }

    @Override
    public void end() {

    }
}
