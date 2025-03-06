package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SSHSftp {

    private SftpUploadManager uploader = new SftpUploadManager();

    private ChannelSftp channel = null;

    public SSHSftp(ChannelSftp channel) {
        this.channel = channel;
    }

    public void close() {
        try {
            this.channel.disconnect();
            this.channel = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isClosed() {
        return this.channel == null || this.channel.isClosed();
    }

    public InputStream getInputStream() throws IOException {
        return this.channel.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.channel.getOutputStream();
    }

    public void connect(int connectTimeout) throws JSchException {
        this.channel.connect(connectTimeout);
    }

    public boolean isConnected() {
        return this.channel.isConnected();
    }

    public List<SftpFile> ls(String path) throws SftpException {
        return this.ls(path, null);
    }

    public List<SftpFile> ls(String path, SSHClient client) throws SftpException {
        Vector<ChannelSftp.LsEntry> vector = this.channel.ls(path);
        List<SftpFile> files = new ArrayList<>();
        for (ChannelSftp.LsEntry lsEntry : vector) {
            SftpFile file = new SftpFile(lsEntry);
            files.add(file);
            if (client != null) {
                String ownerName = SftpUtil.getOwner(file.getUid(), client);
                file.setOwner(ownerName);
                String groupName = SftpUtil.getGroup(file.getGid(), client);
                file.setGroup(groupName);
            }
        }
        return files;
    }

    public void rm(String path) throws SftpException {
        this.channel.rm(path);
    }

    public void rmDir(String path) throws SftpException {
        this.channel.rmdir(path);
    }

    public String pwd() throws SftpException {
        return this.channel.pwd();
    }

    public void mkdir(String path) throws SftpException {
        this.channel.mkdir(path);
    }

    public void touch(String path) throws SftpException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
        this.channel.put(inputStream, path);
    }

    public void rename(String path, String newPath) throws SftpException {
        this.channel.rename(path, newPath);
    }

    public SftpATTRS stat(String path) throws SftpException {
        return this.channel.stat(path);
    }

    private final SftpUploadManager uploadManager = new SftpUploadManager();

    public void upload(File file, String dst) {
        this.uploadManager.addFile(file, dst);
        this.doUpload();
    }

    private void doUpload() {
        if (this.uploadManager.isUploading()) {
            return;
        }
        this.uploadManager.setUploading(true);
        ThreadUtil.start(() -> {
            try {
                do {
                    SftpUploadMonitor monitor = this.uploadManager.takeMonitor();
                    if (monitor == null) {
                        break;
                    }
                    try {
                        this.channel.put(monitor.getFilePath(), monitor.getDest(), monitor, ChannelSftp.OVERWRITE);
                    } catch (SftpException ex) {
                        ex.printStackTrace();
                    }
                } while (!this.uploadManager.isEmpty());
            } finally {
                this.uploadManager.setUploading(false);
            }
        });
    }

    public void setUploadEndCallback(Consumer<SftpUploadEnded> callback) {
        this.uploadManager.setUploadEndCallback(callback);
    }

    public void setUploadChangedCallback(Consumer<SftpUploadChanged> callback) {
        this.uploadManager.setUploadChangedCallback(callback);
    }
}
