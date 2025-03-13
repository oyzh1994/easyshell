package cn.oyzh.easyssh.docker;

import cn.oyzh.easyssh.ssh.SSHClient;

/**
 * @author oyzh
 * @since 2025-03-13
 */
public class DockerExec {

    private SSHClient client;

    private final String image_format = "'{{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}'";

    private final String container_format = "'{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}'";

    public DockerExec(SSHClient client) {
        this.client = client;
    }

    public String docker_ps() {
        return this.client.exec("/usr/bin/docker ps --format " + this.container_format);
    }

    public String docker_ps_a() {
        return this.client.exec("/usr/bin/docker ps -a --format " + this.container_format);
    }

    public String docker_ps_exited() {
        return this.client.exec("/usr/bin/docker ps -f \"status=exited\" --format " + this.container_format);
    }

    public String docker_rm(String containerId) {
        return this.client.exec("/usr/bin/docker rm " + containerId);
    }

    public String docker_logs(String containerId) {
        return this.client.exec("/usr/bin/docker logs " + containerId);
    }

    public String docker_rm_f(String containerId) {
        return this.client.exec("/usr/bin/docker rm -f " + containerId);
    }

    public String docker_start(String containerId) {
        return this.client.exec("/usr/bin/docker start " + containerId);
    }

    public String docker_restart(String containerId) {
        return this.client.exec("/usr/bin/docker restart " + containerId);
    }

    public String docker_pause(String containerId) {
        return this.client.exec("/usr/bin/docker pause " + containerId);
    }

    public String docker_unpause(String containerId) {
        return this.client.exec("/usr/bin/docker unpause " + containerId);
    }

    public String docker_stop(String containerId) {
        return this.client.exec("/usr/bin/docker stop " + containerId);
    }

    public String docker_rmi(String imageId) {
        return this.client.exec("/usr/bin/docker rmi " + imageId);
    }

    public String docker_rmi_f(String imageId) {
        return this.client.exec("/usr/bin/docker rmi -f " + imageId);
    }

    public String docker_inspect(String imageId) {
        return this.client.exec("/usr/bin/docker inspect " + imageId);
    }

    public String docker_images() {
        return this.client.exec("/usr/bin/docker images --format " + this.image_format);
    }
}
