package cn.oyzh.easyshell.sshj.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.sshj.ShellBaseSSHClient;

/**
 * @author oyzh
 * @since 2025-03-13
 */
public class ShellDockerExec implements AutoCloseable {

    private ShellBaseSSHClient client;

    public ShellDockerExec(ShellBaseSSHClient client) {
        this.client = client;
    }

    protected String getContainerFormat() {
        if (this.client.isWindows()) {
            return "\"{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}\"";
        }
        return "'{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}'";
    }

    protected String getImageFormat() {
        if (this.client.isWindows()) {
            return "\"{{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}\"";
        }
        return "'{{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}'";
    }

    protected String getHistoryFormat() {
        if (this.client.isWindows()) {
            return "\"{{.ID}}\r\t{{.CreatedAt}}\r\t{{.CreatedBy}}\r\t{{.Size}}\r\t{{.Comment}}\"";
        }
        return "'{{.ID}}\r\t{{.CreatedAt}}\r\t{{.CreatedBy}}\r\t{{.Size}}\r\t{{.Comment}}'";
    }

    public String docker_ps() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker ps --format  \"" + container_format.substring(1, container_format.length() - 1) + "\"");
//        }
        return this.client.exec("docker ps --format " + this.getContainerFormat());
    }

    public String docker_ps_a() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker ps -a --format \"" + container_format + "\"");
//        }
        return this.client.exec("docker ps -a --format " + this.getContainerFormat());
    }

    public String docker_ps_exited() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker ps -f \"status=exited\" --format \"" + container_format + "\"");
//        }
        return this.client.exec("docker ps -f \"status=exited\" --format " + this.getContainerFormat());
    }

    public String docker_rm(String containerId) {
        return this.client.exec("docker rm " + containerId);
    }

    public String docker_logs(String containerId) {
        return this.client.exec("docker logs " + containerId);
    }

    public String docker_rename(String containerId, String newName) {
        return this.client.exec("docker rename " + containerId + " " + newName);
    }

    public String docker_port(String containerId) {
        return this.client.exec("docker port " + containerId);
    }

    public String docker_rm_f(String containerId) {
        return this.client.exec("docker rm -f " + containerId);
    }

    public String docker_start(String containerId) {
        return this.client.exec("docker start " + containerId);
    }

    public String docker_restart(String containerId) {
        return this.client.exec("docker restart " + containerId);
    }

    public String docker_pause(String containerId) {
        return this.client.exec("docker pause " + containerId);
    }

    public String docker_unpause(String containerId) {
        return this.client.exec("docker unpause " + containerId);
    }

    public String docker_stop(String containerId) {
        return this.client.exec("docker stop " + containerId);
    }

    public String docker_kill(String containerId) {
        return this.client.exec("docker kill " + containerId);
    }

    public String docker_rmi(String imageId) {
        return this.client.exec("docker rmi " + imageId);
    }

    public String docker_rmi_f(String imageId) {
        return this.client.exec("docker rmi -f " + imageId);
    }

    public String docker_inspect(String id) {
        return docker_inspect(id, null);
    }

    public String docker_inspect(String id, String format) {
        if (format == null) {
            return this.client.exec("docker inspect " + id);
        }
        return this.client.exec("docker inspect --format=" + format + " " + id);
    }

    public String docker_images() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker images --format \"" + image_format + "\"");
//        }
        return this.client.exec("docker images --format " + this.getImageFormat());
    }

    public String docker_resource(String id) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.docker_inspect(id, "{{.HostConfig.Memory}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.MemorySwap}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.CpuShares}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.NanoCpus}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.CpuPeriod}}")).append("\t");
        builder.append(this.docker_inspect(id, "{{.HostConfig.CpuQuota}}"));
        return builder.toString();
    }

    public String docker_update(ShellDockerResource resource, String id) {
        StringBuilder builder = new StringBuilder("docker update");
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
        return this.client.exec("docker info");
    }

    public String docker_version() {
        return this.client.exec("docker version");
    }

    public String docker_compose_version() {
        return this.client.exec("docker-compose version");
    }

    public String docker_restart() {
        return this.client.exec("systemctl restart docker");
    }

    public String docker_v() {
        return this.client.exec("docker --version");
    }

    public String docker_history(String imageId) {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker history " + imageId + " --format  \"" + history_format + "\"");
//        }
        return this.client.exec("docker history " + imageId + " --format " + this.getHistoryFormat());
    }

    public String docker_container_prune_f() {
        return this.client.exec("docker container prune -f");
    }

    public String docker_image_prune_f() {
        return this.client.exec("docker image prune -f");
    }

    public String docker_network_prune_f() {
        return this.client.exec("docker network prune -f");
    }

    public String docker_volume_prune_f() {
        return this.client.exec("docker volume prune -f");
    }

    @Override
    public void close() throws Exception {
        this.client = null;
    }

    public String getDaemonFilePath() {
        if (this.client.isMacos()) {
            return this.client.getUserHome() + ".docker/daemon.json";
        } else if (this.client.isWindows()) {
            try {
                String daemonFile = this.client.getUserHome() + ".docker\\daemon.json";
                if (this.client.sftpClient().exist(daemonFile)) {
                    return daemonFile;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "C:\\Users\\Administrator\\.docker\\daemon.json";
        }
        return "/etc/docker/daemon.json";
    }
}
