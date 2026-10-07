package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker保存参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerSave {

    /**
     * 镜像id
     */
    private String imageId;

    /**
     * 镜像名称
     */
    private String imageName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 是否静默
     */
    private boolean quiet;

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
     * 获取文件路径
     *
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置文件路径
     *
     * @param filePath 文件路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 是否静默
     *
     * @return 是否静默
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * 设置是否静默
     *
     * @param quiet 是否静默
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
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
