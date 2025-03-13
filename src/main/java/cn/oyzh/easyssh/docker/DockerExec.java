package cn.oyzh.easyssh.docker;

import cn.oyzh.easyssh.ssh.SSHClient;

/**
 * @author oyzh
 * @since 2025-03-13
 */
public class DockerExec {

    private SSHClient client;

    private final String docker_format = "'{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}'";

    public DockerExec(SSHClient client) {
        this.client = client;
    }

    public String docker_ps() {
        return this.client.exec("/usr/bin/docker ps --format " + docker_format);
    }

    public String docker_ps_a() {
        return this.client.exec("/usr/bin/docker ps -a --format " + docker_format);
    }

    public String docker_ps_exited() {
        return this.client.exec("/usr/bin/docker ps -f \"status=exited\" --format " + docker_format);
    }

    public String docker_rm(String containerId) {
        return this.client.exec("/usr/bin/docker rm " + containerId);
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

    public String docker_stop(String containerId) {
        return this.client.exec("/usr/bin/docker stop " + containerId);
    }

}
