package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker镜像历史
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class ShellDockerImageHistory {

    /**
     * 镜像id
     */
    private String imageId;

    /**
     * 创建事件
     */
    private String created;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 大小
     */
    private String size;

    /**
     * 命令
     */
    private String comment;

    /**
     * 获取镜像id
     *
     * @return 镜像id
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * 设置镜像id
     *
     * @param imageId 镜像id
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
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
     * 获取创建人
     *
     * @return 创建人
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建人
     *
     * @param createdBy 创建人
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
     * 获取命令
     *
     * @return 命令
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置命令
     *
     * @param comment 命令
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
