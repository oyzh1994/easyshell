package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker删除参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerRmi {

    private boolean force;

    private String imageId;

    private String imageName;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

