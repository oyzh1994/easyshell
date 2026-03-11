package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker保存参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerTag {

    private String imageName;

    private String newImageName;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getNewImageName() {
        return newImageName;
    }

    public void setNewImageName(String newImageName) {
        this.newImageName = newImageName;
    }
}
