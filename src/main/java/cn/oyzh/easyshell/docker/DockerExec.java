package cn.oyzh.easyshell.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.shell.ShellClient;

/**
 * @author oyzh
 * @since 2025-03-13
 */
public class DockerExec {

    private final ShellClient client;

    private final String image_format = "'{{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}'";

    private final String container_format = "'{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}'";

    private final String history_format = "'{{.ID}}\r\t{{.CreatedAt}}\r\t{{.CreatedBy}}\r\t{{.Size}}\r\t{{.Comment}}'";

    //private final String resource_format = "{{.HostConfig.Memory}}\t{{.HostConfig.MemorySwap}}\t{{.HostConfig.CpuShares}}\t{{.HostConfig.NanoCpus}}\t{{.HostConfig.CpuPeriod}}\t{{.HostConfig.CpuQuota}}";

    public DockerExec(ShellClient client) {
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

    public String docker_rename(String containerId, String newName) {
        return this.client.exec("/usr/bin/docker rename " + containerId + " " + newName);
    }

    public String docker_port(String containerId) {
        return this.client.exec("/usr/bin/docker port " + containerId);
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

    public String docker_kill(String containerId) {
        return this.client.exec("/usr/bin/docker kill " + containerId);
    }

    public String docker_rmi(String imageId) {
        return this.client.exec("/usr/bin/docker rmi " + imageId);
    }

    public String docker_rmi_f(String imageId) {
        return this.client.exec("/usr/bin/docker rmi -f " + imageId);
    }

    public String docker_inspect(String id) {
        return docker_inspect(id, null);
    }

    public String docker_inspect(String id, String format) {
        if (format == null) {
            return this.client.exec("/usr/bin/docker inspect " + id);
        }
        return this.client.exec("/usr/bin/docker inspect --format=" + format + " " + id);
    }

    public String docker_images() {
        return this.client.exec("/usr/bin/docker images --format " + this.image_format);
    }

    public String docker_resource(String id) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.docker_inspect(id, "{{.HostConfig.Memory}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.MemorySwap}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.CpuShares}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.NanoCpus}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.CpuPeriod}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.CpuQuota}}"));
//        return this.docker_inspect(id, this.resource_format);
        return builder.toString();
    }

    public String docker_update(DockerResource resource, String id) {
        StringBuilder builder = new StringBuilder("/usr/bin/docker update");
        if (resource.getMemory() > 0) {
            builder.append(" --memory ").append(resource.getMemory()).append("m");
        }
        if (resource.getMemorySwap() > 0) {
            builder.append(" --memory-swap ").append(resource.getMemorySwap()).append("m");
        }
        if (resource.getCpuShares() > 0) {
            builder.append(" --cpu-shares ").append(resource.getCpuShares());
        }
        if (resource.getNanoCpus() > 0) {
            builder.append(" --cpus ").append(resource.getNanoCpus());
        }
        if (resource.getCpuPeriod() > 0) {
            builder.append(" --cpu-period ").append(resource.getCpuPeriod());
        }
        if (resource.getCpuQuota() > 0) {
            builder.append(" --cpu-quota ").append(resource.getCpuQuota());
        }
        builder.append(" ").append(id);
        JulLog.info("docker update:{}", builder.toString());
        return this.client.exec(builder.toString());
    }

    public String docker_info() {
        return this.client.exec("/usr/bin/docker info");
    }

    public String docker_version() {
        return this.client.exec("/usr/bin/docker version");
    }

    public String docker_history(String imageId) {
        return this.client.exec("/usr/bin/docker history " + imageId + " --format " + this.history_format);
    }

    public String docker_container_prune_f() {
        return this.client.exec("/usr/bin/docker container prune -f");
    }

    public String docker_image_prune_f() {
        return this.client.exec("/usr/bin/docker image prune -f");
    }

    public String docker_network_prune_f() {
        return this.client.exec("/usr/bin/docker network prune -f");
    }

    public String docker_volume_prune_f() {
        return this.client.exec("/usr/bin/docker volume prune -f");
    }
}
