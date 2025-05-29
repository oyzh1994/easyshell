package cn.oyzh.easyshell.docker;

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

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
