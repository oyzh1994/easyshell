package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker提交参数
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellDockerCommit {

    private String tag;

    private String comment;

    private String repository;

    private String containerId;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}