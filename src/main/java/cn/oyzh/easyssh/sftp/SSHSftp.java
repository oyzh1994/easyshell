package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SSHSftp {

    private ChannelSftp channel;

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

    public List<SftpFile> lsNormal(String path) throws SftpException {
        List<SftpFile> files = this.ls(path);
        return files.stream().filter(SftpFile::isNormal).collect(Collectors.toList());
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
            if (client != null && !file.isReturnDirectory() && !file.isCurrentFile()) {
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

    public void rmdir(String path) throws SftpException {
        this.channel.rmdir(path);
    }

    public void rmdirRecursive(String path) throws SftpException {
        Vector<ChannelSftp.LsEntry> entries = this.channel.ls(path);
        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (!filename.equals(".") && !filename.equals("..")) {
                String fullPath = path + "/" + filename;
                if (entry.getAttrs().isDir()) {
                    this.rmdirRecursive(fullPath);
                } else {
                    this.rm(fullPath);
                }
            }
        }
        this.rmdir(path);
    }

    public String pwd() throws SftpException {
        return this.channel.pwd();
    }

    public void mkdir(String path) throws SftpException {
        this.channel.mkdir(path);
    }

    public void mkdirIfNotExist(String path) throws SftpException {
        if (!this.exist(path)) {
            this.mkdir(path);
        }
    }

    public void mkdirRecursive(String path) throws SftpException {
        String[] dirs = path.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            currentPath.append("/").append(dir);
            try {
                this.channel.stat(currentPath.toString());  // 检查目录是否存在‌:ml-citation{ref="1,7" data="citationList"}
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    this.channel.mkdir(currentPath.toString());  // 创建缺失目录‌:ml-citation{ref="1,6" data="citationList"}
                } else {
                    throw e;
                }
            }
        }
    }


//    public void mkdirRecursive(String path) throws SftpException {
//        String[] dirs = path.split("/");
//        String dir = "";
//        for (String s : dirs) {
//            if(s.isEmpty()) {
//                continue;
//            }
//            dir = dir + "/" + s;
//            if (!this.exist(dir)) {
//                this.mkdir(path);
//            }
//        }
//    }

    public boolean exist(String path) throws SftpException {
        try {
            return this.stat(path) != null;
        } catch (SftpException ex) {
            if (StringUtil.contains(ex.getMessage(), "No such file")) {
                return false;
            }
            throw ex;
        }
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

    private final AtomicBoolean using = new AtomicBoolean(false);

    public void setUsing(boolean using) {
        this.using.set(using);
    }

    public boolean isUsing() {
        return this.using.get();
    }

    public void put(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        this.channel.put(src, dest, monitor, mode);
    }

    public void get(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        this.channel.get(src, dest, monitor, mode);
    }
}
