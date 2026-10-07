package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker标签参数
 *
 * @author oyzh
 * @since 2026-03-11
 */
public class ShellDockerTag {

    /**
     * 镜像名称
     */
    private String imageName;

    /**
     * 新镜像名称
     */
    private String newImageName;

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

    /**
     * 获取新镜像名称
     *
     * @return 新镜像名称
     */
    public String getNewImageName() {
        return newImageName;
    }

    /**
     * 设置新镜像名称
     *
     * @param newImageName 新镜像名称
     */
    public void setNewImageName(String newImageName) {
        this.newImageName = newImageName;
    }
}
