package cn.oyzh.easyssh.ssh;

import cn.oyzh.easyssh.sftp.SftpAttr;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.SftpUploader;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private SftpUploader uploader = new SftpUploader();

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

    public void upload(String path, String dst) throws SftpException, IOException {
        File file = new File(path);

        SftpProgressMonitor monitor = new SftpProgressMonitor() {
            @Override
            public void init(int i, String s, String s1, long l) {

                System.out.println("i=" + l);
                System.out.println("s=" + s);
                System.out.println("s1=" + s1);
                System.out.println("l=" + l);
            }

            @Override
            public boolean count(long l) {
                System.out.println("l=" + l);
                return true;
            }

            @Override
            public void end() {

            }
        };
        channel.put(path, dst, monitor, ChannelSftp.OVERWRITE);
//        FileInputStream fis = new FileInputStream(file);
//
//        // 设置缓冲区大小
//        int bufferSize = 4096 ; // 1MB
//        channel.setInputStream(fis);
//        channel.setOutputStream(channel.put(dst + "/" + file.getName(), monitor, ChannelSftp.OVERWRITE));
//
//        byte[] buffer = new byte[bufferSize];
//        int bytesRead;
//        while ((bytesRead = fis.read(buffer)) != -1) {
//            channel.getOutputStream().write(buffer, 0, bytesRead);
//        }

//        this.channel.put(path, dst, , ChannelSftp.OVERWRITE);
    }
}
