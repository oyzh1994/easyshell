package cn.oyzh.easyssh.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
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
}
