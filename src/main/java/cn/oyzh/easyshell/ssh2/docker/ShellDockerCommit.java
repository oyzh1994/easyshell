package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker提交参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerCommit {

    /**
     * 标签
     */
    private String tag;

    /**
     * 备注
     */
    private String comment;

    /**
     * 仓库
     */
    private String repository;

    /**
     * 容器id
     */
    private String containerId;

    /**
     * 获取标签
     *
     * @return 标签
     */
    public String getTag() {
        return tag;
    }

    /**
     * 设置标签
     *
     * @param tag 标签
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置备注
     *
     * @param comment 备注
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取仓库
     *
     * @return 仓库
     */
    public String getRepository() {
        return repository;
    }

    /**
     * 设置仓库
     *
     * @param repository 仓库
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

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
}