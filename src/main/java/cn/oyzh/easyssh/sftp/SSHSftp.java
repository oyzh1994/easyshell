package cn.oyzh.easyssh.sftp;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.ssh.SSHChannel;
import cn.oyzh.easyssh.ssh.SSHClient;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SSHSftp extends SSHChannel {

    public SSHSftp(ChannelSftp channel) {
        super(channel);
    }

    private final AtomicBoolean using = new AtomicBoolean(false);

    public void setUsing(boolean using) {
        this.using.set(using);
    }

    public boolean isUsing() {
        return this.using.get();
    }

    private final AtomicBoolean holding = new AtomicBoolean(false);

    public void setHolding(boolean holding) {
        this.holding.set(holding);
    }

    public boolean isHolding() {
        return this.holding.get();
    }

    @Override
    public ChannelSftp getChannel() {
        return (ChannelSftp) super.getChannel();
    }

    public Vector<ChannelSftp.LsEntry> ls(String path) throws SftpException {
        try {
            this.setUsing(true);
            return this.getChannel().ls(path);
        } finally {
            this.setUsing(false);
        }
    }

    public List<SftpFile> lsFileNormal(String path) throws SftpException {
        List<SftpFile> files = this.lsFile(path);
        return files.stream().filter(SftpFile::isNormal).collect(Collectors.toList());
    }

    public List<SftpFile> lsFile(String path) throws SftpException {
        return this.lsFile(path, null);
    }

    public List<SftpFile> lsFile(String path, SSHClient client) throws SftpException {
        Vector<ChannelSftp.LsEntry> vector = this.ls(path);
        List<SftpFile> files = new ArrayList<>();
        for (ChannelSftp.LsEntry lsEntry : vector) {
            SftpFile file = new SftpFile(path, lsEntry);
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
        try {
            this.setUsing(true);
            this.getChannel().rm(path);
//            this.deleteManager.deleteDeleted(path);
        } finally {
            this.setUsing(false);
        }
    }

    public void rmdir(String path) throws SftpException {
        try {
            this.setUsing(true);
            this.getChannel().rmdir(path);
//            this.deleteManager.deleteDeleted(path);
        } finally {
            this.setUsing(false);
        }
    }

    public void rmdirRecursive(String path) throws SftpException {
        Vector<ChannelSftp.LsEntry> entries = this.ls(path);
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
        try {
            this.setUsing(true);
            return this.getChannel().pwd();
        } finally {
            this.setUsing(false);
        }
    }

    public void mkdir(String path) throws SftpException {
        try {
            this.setUsing(true);
            this.getChannel().mkdir(path);
        } finally {
            this.setUsing(false);
        }
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
                this.stat(currentPath.toString());  // 检查目录是否存在‌:ml-citation{ref="1,7" data="citationList"}
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    this.mkdir(currentPath.toString());  // 创建缺失目录‌:ml-citation{ref="1,6" data="citationList"}
                } else {
                    throw e;
                }
            }
        }
    }

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
        try {
            this.setUsing(true);
            ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
            this.getChannel().put(inputStream, path);
        } finally {
            this.setUsing(false);
        }
    }

    public void rename(String path, String newPath) throws SftpException {
        try {
            this.setUsing(true);
            this.getChannel().rename(path, newPath);
        } finally {
            this.setUsing(false);
        }
    }

    public SftpATTRS stat(String path) throws SftpException {
        try {
            this.setUsing(true);
            return this.getChannel().stat(path);
        } finally {
            this.setUsing(false);
        }
    }

    public void put(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            this.setUsing(true);
            this.getChannel().put(src, dest, monitor, mode);
        } finally {
            this.setUsing(false);
        }
    }

    public void get(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            this.setUsing(true);
            this.getChannel().get(src, dest, monitor, mode);
        } finally {
            this.setUsing(false);
        }
    }
}
