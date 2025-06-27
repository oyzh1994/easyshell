//package cn.oyzh.easyshell.sshj.docker;
//
//import cn.oyzh.common.util.StringUtil;
//
///**
// * docker容器定义
// *
// * @author oyzh
// * @since 2025-03-12
// */
//public class ShellDockerContainer {
//
//    /**
//     * 容器id
//     */
//    private String containerId;
//
//    /**
//     * 镜像
//     */
//    private String image;
//
//    /**
//     * 命令
//     */
//    private String command;
//
//    /**
//     * 创建时间
//     */
//    private String created;
//
//    /**
//     * 状态
//     */
//    private String status;
//
//    /**
//     * 端口
//     */
//    private String ports;
//
//    /**
//     * 名称
//     */
//    private String names;
//
//    public String getContainerId() {
//        return containerId;
//    }
//
//    public void setContainerId(String containerId) {
//        this.containerId = containerId;
//    }
//
//    public String getImage() {
//        return image;
//    }
//
//    public void setImage(String image) {
//        this.image = image;
//    }
//
//    public String getCommand() {
//        return command;
//    }
//
//    public void setCommand(String command) {
//        this.command = command;
//    }
//
//    public String getCreated() {
//        return created;
//    }
//
//    public void setCreated(String created) {
//        this.created = created;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getPorts() {
//        return ports;
//    }
//
//    public void setPorts(String ports) {
//        this.ports = ports;
//    }
//
//    public String getNames() {
//        return names;
//    }
//
//    public void setNames(String names) {
//        this.names = names;
//    }
//
//    public boolean isExited() {
//        return StringUtil.contains(this.status, "Exited");
//    }
//
//    public boolean isPaused() {
//        return StringUtil.contains(this.status, "Paused");
//    }
//
//    public boolean isRunning() {
//        return !this.isExited();
//    }
//}
