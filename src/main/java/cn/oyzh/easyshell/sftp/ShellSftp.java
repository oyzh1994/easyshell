package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh.ShellChannel;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.easyshell.util.ShellUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.ByteArrayInputStream;
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
public class ShellSftp extends ShellChannel {

    private ShellClient client;

    public ShellSftp(ChannelSftp channel, ShellClient client) {
        super(channel);
        this.client = client;
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
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            return this.getChannel().ls(path);
        } finally {
            this.setUsing(false);
        }
    }

    public void cd(String path) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            this.getChannel().cd(path);
        } finally {
            this.setUsing(false);
        }
    }

    public List<ShellSftpFile> lsFileNormal(String path) throws SftpException {
        List<ShellSftpFile> files = this.lsFile(path);
        return files.stream().filter(ShellSftpFile::isNormal).collect(Collectors.toList());
    }

    public List<ShellSftpFile> lsFile(String path) throws SftpException {
        return this.lsFile(path, null);
    }

    public String realpath(String path) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            return this.getChannel().realpath(path);
        } finally {
            this.setUsing(false);
        }
    }

    public List<ShellSftpFile> lsFile(String path, ShellClient client) throws SftpException {
        if (this.client.isWindows()) {
            path = ShellUtil.reverseWindowsFilePath(path);
        }
        this.cd(path);
        Vector<ChannelSftp.LsEntry> vector = this.ls(path);
        List<ShellSftpFile> files = new ArrayList<>();
        for (ChannelSftp.LsEntry lsEntry : vector) {
            ShellSftpFile file = new ShellSftpFile(path, lsEntry);
            // 读取链接文件
            ShellSftpUtil.realpath(file, this);
            files.add(file);
            if (client != null && !file.isReturnDirectory() && !file.isCurrentFile()) {
                if (client.isWindows()) {
                    file.setOwner("-");
                    file.setGroup("-");
                } else {
                    String ownerName = ShellSftpUtil.getOwner(file.getUid(), client);
                    String groupName = ShellSftpUtil.getGroup(file.getGid(), client);
                    file.setOwner(ownerName);
                    file.setGroup(groupName);
                }
            }
        }
        return files;
    }

    public void rm(String path) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            this.getChannel().rm(path);
        } finally {
            this.setUsing(false);
        }
    }

    public void rmdir(String path) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            this.getChannel().rmdir(path);
        } finally {
            this.setUsing(false);
        }
    }

//    public void rmdirRecursive(String path) throws SftpException {
//        Vector<ChannelSftp.LsEntry> entries = this.ls(path);
//        for (ChannelSftp.LsEntry entry : entries) {
//            String filename = entry.getFilename();
//            if (!filename.equals(".") && !filename.equals("..")) {
//                String fullPath = path + "/" + filename;
//                if (entry.getAttrs().isDir()) {
//                    this.rmdirRecursive(fullPath);
//                } else {
//                    this.rm(fullPath);
//                }
//            }
//        }
//        this.rmdir(path);
//    }

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
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            this.getChannel().mkdir(path);
        } finally {
            this.setUsing(false);
        }
    }

//    public void mkdirIfNotExist(String path) throws SftpException {
//        if (!this.exist(path)) {
//            this.mkdir(path);
//        }
//    }

    public void mkdirRecursive(String path) throws SftpException {
        if (this.client.isWindows()) {
            path = ShellUtil.reverseWindowsFilePath(path);
        }
        String[] dirs = path.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            currentPath.append("/").append(dir);
            try {
                this.stat(currentPath.toString());  // 检查目录是否存在
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    this.mkdir(currentPath.toString());  // 创建缺失目录
                } else {
                    throw e;
                }
            }
        }
    }

    public boolean exist(String path) throws SftpException {
        try {
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
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
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream("".getBytes());
            this.getChannel().put(inputStream, path);
        } finally {
            this.setUsing(false);
        }
    }

    public void rename(String path, String newPath) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            this.getChannel().rename(path, newPath);
        } finally {
            this.setUsing(false);
        }
    }

    public SftpATTRS stat(String path) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            return this.getChannel().stat(path);
        } catch (SftpException ex) {
            if (ExceptionUtil.hasMessage(ex, "No such file")) {
                JulLog.info("File:{}", path);
            }
            throw ex;
        } finally {
            this.setUsing(false);
        }
    }

    public void put(String src, String dest, SftpProgressMonitor monitor) throws SftpException {
        this.put(src, dest, monitor, ChannelSftp.OVERWRITE);
    }

    public void put(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                src = ShellUtil.reverseWindowsFilePath(src);
                dest = ShellUtil.reverseWindowsFilePath(dest);
            }
            this.getChannel().put(src, dest, monitor, mode);
        } finally {
            this.setUsing(false);
        }
    }

    public void put(InputStream src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                dest = ShellUtil.reverseWindowsFilePath(dest);
            }
            this.getChannel().put(src, dest, monitor, mode);
        } finally {
            this.setUsing(false);
        }
    }

    public void put(String src, String dest) throws SftpException {
        this.put(src, dest, null, ChannelSftp.OVERWRITE);
    }

    public void put(InputStream src, String dest) throws SftpException {
        this.put(src, dest, null, ChannelSftp.OVERWRITE);
    }

    public void get(String src, String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                src = ShellUtil.reverseWindowsFilePath(src);
                dest = ShellUtil.reverseWindowsFilePath(dest);
            }
            this.getChannel().get(src, dest, monitor, mode);
        } finally {
            this.setUsing(false);
        }
    }

    public InputStream get(String src) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                src = ShellUtil.reverseWindowsFilePath(src);
            }
            return this.getChannel().get(src);
        } finally {
            this.setUsing(false);
        }
    }

    public OutputStream put(String dest, SftpProgressMonitor monitor) throws SftpException {
        return this.put(dest, monitor, ChannelSftp.OVERWRITE);
    }

    public OutputStream put(String dest, SftpProgressMonitor monitor, int mode) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                dest = ShellUtil.reverseWindowsFilePath(dest);
            }
            return this.getChannel().put(dest, monitor, mode);
        } finally {
            this.setUsing(false);
        }
    }

    public void get(String src, String dest) throws SftpException {
        this.get(src, dest, null, ChannelSftp.OVERWRITE);
    }

    @Override
    public void close() {
        this.setHolding(true);
        super.close();
        this.setHolding(false);
        this.client = null;
    }

    /**
     * 修改权限，这个方法windows执行无效果
     *
     * @param permission 权限
     * @param path       文件路径
     */
    public void chmod(int permission, String path) throws SftpException {
        try {
            this.setUsing(true);
            if (this.client.isWindows()) {
                path = ShellUtil.reverseWindowsFilePath(path);
            }
            this.getChannel().chmod(permission, path);
        } finally {
            this.setUsing(false);
        }
    }
}
