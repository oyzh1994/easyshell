package cn.oyzh.easyssh.ssh;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyssh.sftp.SftpAttr;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.SftpMonitor;
import cn.oyzh.easyssh.sftp.SftpUploadManager;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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

    public List<SftpFile> ls(String path) throws SftpException, JSchException, IOException {
        return this.ls(path, null);
    }

    public List<SftpFile> ls(String path, SSHClient client) throws SftpException, JSchException, IOException {
        Vector<ChannelSftp.LsEntry> vector = this.channel.ls(path);
        List<SftpFile> files = new ArrayList<>();
        for (ChannelSftp.LsEntry lsEntry : vector) {
            SftpFile file = new SftpFile(lsEntry);
            files.add(file);
            if (client != null) {
                SftpAttr attr = client.getAttr();
                int uid = file.getUid();
                String ownerName = attr.getOwner(uid);
                if (ownerName == null) {
                    ownerName = client.exec_id_un(uid);
                    attr.putOwner(uid, ownerName);
                }
                file.setOwner(ownerName);

                int gid = file.getGid();
                String groupName = attr.getGroup(gid);
                if (groupName == null) {
                    groupName = client.exec_id_gn(gid);
                    attr.putGroup(gid, groupName);
                }
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
        uploadManager.addFile(file, dst);
        doUpload();
    }

    private void doUpload() {
        if (uploadManager.isUploading()) {
            return;
        }
        uploadManager.setUploading(true);
        ThreadUtil.start(() -> {
            try {
                do {
                    SftpMonitor monitor = uploadManager.takeMonitor();
                    if (monitor == null) {
                        break;
                    }
                    try {
                        this.channel.put(monitor.getFilePath(), monitor.getDest(), monitor, ChannelSftp.OVERWRITE);
                    } catch (SftpException ex) {
                        ex.printStackTrace();
                    }
                } while (!uploadManager.isEmpty());
            } finally {
                uploadManager.setUploading(false);
            }
        });
    }
}
