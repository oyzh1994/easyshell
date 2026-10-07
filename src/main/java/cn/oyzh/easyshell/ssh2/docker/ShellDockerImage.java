package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker镜像
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class ShellDockerImage {

    /**
     * 镜像id
     */
    private String imageId;

    /**
     * 仓库
     */
    private String repository;

    /**
     * 标签
     */
    private String tag;

    /**
     * 创建时间
     */
    private String created;

    /**
     * 大小
     */
    private String size;

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
     * 获取镜像名称
     *
     * @return 镜像名称
     */
    public String getImageName() {
        return this.repository + ":" + this.tag;
    }
}
