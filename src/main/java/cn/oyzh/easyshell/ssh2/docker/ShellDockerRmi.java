package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker删除参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerRmi {

    /**
     * 是否强制删除
     */
    private boolean force;

    /**
     * 镜像id
     */
    private String imageId;

    /**
     * 镜像名称
     */
    private String imageName;

    /**
     * 是否强制删除
     *
     * @return 是否强制删除
     */
    public boolean isForce() {
        return force;
    }

    /**
     * 设置是否强制删除
     *
     * @param force 是否强制删除
     */
    public void setForce(boolean force) {
        this.force = force;
    }

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
     * 获取镜像名称
     *
     * @return 镜像名称
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * 设置镜像名称
     *
     * @param imageName 镜像名称
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

