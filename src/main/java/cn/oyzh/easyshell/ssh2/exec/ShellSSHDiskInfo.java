package cn.oyzh.easyshell.ssh2.exec;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellSSHDiskInfo {

    /**
     * 文件系统
     */
    private String fileSystem;

    /**
     * 大小
     */
    private String size;

    /**
     * 已用
     */
    private String used;

    /**
     * 可用
     */
    private String avail;

    /**
     * 使用率
     */
    private String use;

    /**
     * 挂载点
     */
    private String mountedOn;

    public String getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getAvail() {
        return avail;
    }

    public void setAvail(String avail) {
        this.avail = avail;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getMountedOn() {
        return mountedOn;
    }

    public void setMountedOn(String mountedOn) {
        this.mountedOn = mountedOn;
    }
}
