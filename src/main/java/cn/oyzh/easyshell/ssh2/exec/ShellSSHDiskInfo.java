package cn.oyzh.easyshell.ssh2.exec;

/**
 * ssh磁盘信息
 *
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

    /**
     * 获取文件系统
     *
     * @return 文件系统
     */
    public String getFileSystem() {
        return fileSystem;
    }

    /**
     * 设置文件系统
     *
     * @param fileSystem 文件系统
     */
    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * 获取大小
     *
     * @return 大小
     */
    public String getSize() {
        return size;
    }

    /**
     * 设置大小
     *
     * @param size 大小
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * 获取已用
     *
     * @return 已用
     */
    public String getUsed() {
        return used;
    }

    /**
     * 设置已用
     *
     * @param used 已用
     */
    public void setUsed(String used) {
        this.used = used;
    }

    /**
     * 获取可用
     *
     * @return 可用
     */
    public String getAvail() {
        return avail;
    }

    /**
     * 设置可用
     *
     * @param avail 可用
     */
    public void setAvail(String avail) {
        this.avail = avail;
    }

    /**
     * 获取使用率
     *
     * @return 使用率
     */
    public String getUse() {
        return use;
    }

    /**
     * 设置使用率
     *
     * @param use 使用率
     */
    public void setUse(String use) {
        this.use = use;
    }

    /**
     * 获取挂载点
     *
     * @return 挂载点
     */
    public String getMountedOn() {
        return mountedOn;
    }

    /**
     * 设置挂载点
     *
     * @param mountedOn 挂载点
     */
    public void setMountedOn(String mountedOn) {
        this.mountedOn = mountedOn;
    }
}
