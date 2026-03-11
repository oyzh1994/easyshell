package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker保存参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerSave {

    private String imageId;

    private String imageName;

    private String filePath;

    private boolean quiet;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
