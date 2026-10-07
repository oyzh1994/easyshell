package cn.oyzh.easyshell.ssh2.docker;

import cn.oyzh.common.util.StringUtil;

/**
 * docker容器定义
 *
 * @author oyzh
 * @since 2025-03-12
 */
public class ShellDockerContainer {

    /**
     * 容器id
     */
    private String containerId;

    /**
     * 镜像
     */
    private String image;

    /**
     * 命令
     */
    private String command;

    /**
     * 创建时间
     */
    private String created;

    /**
     * 状态
     */
    private String status;

    /**
     * 端口
     */
    private String ports;

    /**
     * 名称
     */
    private String names;

    /**
     * 获取容器id
     *
     * @return 容器id
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * 设置容器id
     *
     * @param containerId 容器id
     */
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    /**
     * 获取镜像
     *
     * @return 镜像
     */
    public String getImage() {
        return image;
    }

    /**
     * 设置镜像
     *
     * @param image 镜像
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * 获取命令
     *
     * @return 命令
     */
    public String getCommand() {
        return command;
    }

    /**
     * 设置命令
     *
     * @param command 命令
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public String getCreated() {
        return created;
    }

    /**
     * 设置创建时间
     *
     * @param created 创建时间
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取端口
     *
     * @return 端口
     */
    public String getPorts() {
        return ports;
    }

    /**
     * 设置端口
     *
     * @param ports 端口
     */
    public void setPorts(String ports) {
        this.ports = ports;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getNames() {
        return names;
    }

    /**
     * 设置名称
     *
     * @param names 名称
     */
    public void setNames(String names) {
        this.names = names;
    }

    /**
     * 是否已退出
     *
     * @return 是否已退出
     */
    public boolean isExited() {
        return StringUtil.contains(this.status, "Exited");
    }

    /**
     * 是否已暂停
     *
     * @return 是否已暂停
     */
    public boolean isPaused() {
        return StringUtil.contains(this.status, "Paused");
    }

    /**
     * 是否运行中
     *
     * @return 是否运行中
     */
    public boolean isRunning() {
        return !this.isExited();
    }
}
