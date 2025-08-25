package cn.oyzh.easyshell.ssh2.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;

/**
 * docker执行器
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class ShellDockerExec implements AutoCloseable {

    private ShellSSHClient client;

    public ShellDockerExec(ShellSSHClient client) {
        this.client = client;
    }

    public ShellSSHClient getClient() {
        return client;
    }

    /**
     * 获取容器格式
     *
     * @return 结果
     */
    protected String getContainerFormat() {
        if (this.client.isWindows()) {
            return "\"{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}\"";
        }
        return "'{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}'";
    }

    /**
     * 获取镜像格式
     *
     * @return 结果
     */
    protected String getImageFormat() {
        if (this.client.isWindows()) {
            return "\"{{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}\"";
        }
        return "'{{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}\t{{.Size}}'";
    }

    /**
     * 获取历史格式
     *
     * @return 结果
     */
    protected String getHistoryFormat() {
        if (this.client.isWindows()) {
            return "\"{{.ID}}\r\t{{.CreatedAt}}\r\t{{.CreatedBy}}\r\t{{.Size}}\r\t{{.Comment}}\"";
        }
        return "'{{.ID}}\r\t{{.CreatedAt}}\r\t{{.CreatedBy}}\r\t{{.Size}}\r\t{{.Comment}}'";
    }

    /**
     * 执行docker ps命令
     *
     * @return 结果
     */
    public String docker_ps() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker ps --format  \"" + container_format.substring(1, container_format.length() - 1) + "\"");
//        }
        return this.client.exec("docker ps --format " + this.getContainerFormat());
    }

    /**
     * 执行docker ps -a命令
     *
     * @return 结果
     */
    public String docker_ps_a() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker ps -a --format \"" + container_format + "\"");
//        }
        return this.client.exec("docker ps -a --format " + this.getContainerFormat());
    }

    /**
     * 执行docker ps -f 'status=exited' 命令
     *
     * @return 结果
     */
    public String docker_ps_exited() {
//        if (this.client.isWindows()) {
//            return this.client.exec("docker ps -f \"status=exited\" --format \"" + container_format + "\"");
//        }
        return this.client.exec("docker ps -f \"status=exited\" --format " + this.getContainerFormat());
    }

    /**
     * 执行docker rm命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_rm(String containerId) {
        return this.client.exec("docker rm " + containerId);
    }

    /**
     * 执行docker logs命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_logs(String containerId) {
        return this.client.exec("docker logs " + containerId);
    }

    /**
     * 执行docker rename命令
     *
     * @param containerId 容器id
     * @param newName     新名称
     * @return 结果
     */
    public String docker_rename(String containerId, String newName) {
        return this.client.exec("docker rename " + containerId + " " + newName);
    }

    /**
     * 执行docker port命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_port(String containerId) {
        return this.client.exec("docker port " + containerId);
    }

    /**
     * 执行docker rm -f命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_rm_f(String containerId) {
        return this.client.exec("docker rm -f " + containerId);
    }

    /**
     * 执行docker start命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_start(String containerId) {
        return this.client.exec("docker start " + containerId);
    }

    /**
     * 执行docker restart命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_restart(String containerId) {
        return this.client.exec("docker restart " + containerId);
    }

    /**
     * 执行docker pause命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_pause(String containerId) {
        return this.client.exec("docker pause " + containerId);
    }

    /**
     * 执行docker unpause命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_unpause(String containerId) {
        return this.client.exec("docker unpause " + containerId);
    }

    /**
     * 执行docker stop命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_stop(String containerId) {
        return this.client.exec("docker stop " + containerId);
    }

    /**
     * 执行docker kill命令
     *
     * @param containerId 容器id
     * @return 结果
     */
    public String docker_kill(String containerId) {
        return this.client.exec("docker kill " + containerId);
    }

    /**
     * 执行docker rmi命令
     *
     * @param imageId 镜像id
     * @return 结果
     */
    public String docker_rmi(String imageId) {
        return this.client.exec("docker rmi " + imageId);
    }

    /**
     * 执行docker rm -f命令
     *
     * @param imageId 镜像id
     * @return 结果
     */
    public String docker_rmi_f(String imageId) {
        return this.client.exec("docker rmi -f " + imageId);
    }

    /**
     * 执行docker inspect命令
     *
     * @param id id
     * @return 结果
     */
    public String docker_inspect(String id) {
        return docker_inspect(id, null);
    }

    /**
     * 执行docker inspect命令
     *
     * @param id     id
     * @param format 格式
     * @return 结果
     */
    public String docker_inspect(String id, String format) {
        if (format == null) {
            return this.client.exec("docker inspect " + id);
        }
        return this.client.exec("docker inspect --format=" + format + " " + id, 3000);
    }

    /**
     * 执行docker images命令
     *
     * @return 结果
     */
    public String docker_images() {
        return this.client.exec("docker images --format " + this.getImageFormat(), 3000);
    }

    /**
     * 获取docker资源
     *
     * @param id id
     * @return 结果
     */
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

    /**
     * 修改docker资源
     *
     * @param resource 资源
     * @param id       id
     * @return 结果
     */
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
        if (JulLog.isInfoEnabled()) {
            JulLog.info("docker update:{}", builder.toString());
        }
        return this.client.exec(builder.toString());
    }

    /**
     * 执行docker run命令
     *
     * @param run 参数
     * @return 结果
     */
    public String docker_run(ShellDockerRun run) {
        StringBuilder builder = new StringBuilder("docker run ");
        if (StringUtil.isNotEmpty(run.getContainerName())) {
            builder.append("--name ").append(run.getContainerName()).append(" ");
        }
        if (run.isI()) {
            builder.append("-i ");
        }
        if (run.isT()) {
            builder.append("-t ");
        }
        if (run.isD()) {
            builder.append("-d ");
        }
        if (run.isRm()) {
            builder.append("--rm ");
        }
        if (run.isPrivileged()) {
            builder.append("--privileged ");
        }
        if (!run.isIgnoreRestart()) {
            builder.append("--restart=").append(run.getRestart()).append(" ");
        }

        // 端口
        if (CollectionUtil.isNotEmpty(run.getPorts())) {
            for (ShellDockerRun.DockerPort port : run.getPorts()) {
                builder.append("-p ").append(port.getOuterPort()).append(":").append(port.getInnerPort());
                if (!port.isTcp()) {
                    builder.append("/udp");
                }
                builder.append(" ");
            }
        }

        // 卷
        if (CollectionUtil.isNotEmpty(run.getVolumes())) {
            for (ShellDockerRun.DockerVolume volume : run.getVolumes()) {
                builder.append("-v ").append(volume.getOuterVolume()).append(":").append(volume.getInnerVolume()).append(" ");
            }
        }

        // 环境
        if (CollectionUtil.isNotEmpty(run.getEnvs())) {
            for (ShellDockerRun.DockerEnv env : run.getEnvs()) {
                builder.append("-e ").append(env.getName()).append("=").append(env.getValue()).append(" ");
            }
        }

        // 标签
        if (CollectionUtil.isNotEmpty(run.getLabels())) {
            for (ShellDockerRun.DockerLabel label : run.getLabels()) {
                builder.append("--label ").append(label.getName()).append("=").append(label.getValue()).append(" ");
            }
        }
        builder.append(run.getImageId());

        if (JulLog.isInfoEnabled()) {
            JulLog.info("docker run:{}", builder.toString());
        }
        return this.client.exec(builder.toString());
    }

    /**
     * 执行docker save命令
     *
     * @param save 参数
     * @return 结果
     */
    public String docker_save(ShellDockerSave save) {
        StringBuilder builder = new StringBuilder("docker save ");
        if (save.isQuiet()) {
            builder.append("-q ");
        }
        builder.append(" -o")
                .append(save.getFilePath())
                .append(" ")
                .append(save.getImageId());
        if (JulLog.isInfoEnabled()) {
            JulLog.info("docker save:{}", builder.toString());
        }
        return this.client.exec(builder.toString());
    }

    /**
     * 执行docker commit命令
     *
     * @param commit 参数
     * @return 结果
     */
    public String docker_commit(ShellDockerCommit commit) {
        StringBuilder builder = new StringBuilder("docker commit ");
        if (StringUtil.isNotBlank(commit.getComment())) {
            builder.append("-m ").append(commit.getComment()).append(" ");
        }
        builder.append(commit.getContainerId());
        if (StringUtil.isNotBlank(commit.getRepository())) {
            builder.append(" ").append(commit.getRepository());
            if (StringUtil.isNotBlank(commit.getTag())) {
                builder.append(":").append(commit.getTag());
            }
        }
        if (JulLog.isInfoEnabled()) {
            JulLog.info("docker commit:{}", builder.toString());
        }
        return this.client.exec(builder.toString());
    }

    /**
     * 执行docker info命令
     *
     * @return 结果
     */
    public String docker_info() {
        return this.client.exec("docker info");
    }

    /**
     * 执行docker version命令
     *
     * @return 结果
     */
    public String docker_version() {
        return this.client.exec("docker version");
    }

    /**
     * 执行docker-compose version命令
     *
     * @return 结果
     */
    public String docker_compose_version() {
        return this.client.exec("docker-compose version");
    }

    /**
     * 重启docker
     *
     * @return 结果
     */
    public String docker_restart() {
        return this.client.exec("systemctl restart docker");
    }

    // public String docker_v() {
    //     return this.client.exec("docker --version");
    // }

    /**
     * 执行docker history命令
     *
     * @param imageId 镜像id
     * @return 结果
     */
    public String docker_history(String imageId) {
        return this.client.exec("docker history " + imageId + " --format " + this.getHistoryFormat());
    }

    /**
     * 执行docker container prune -f命令
     *
     * @return 结果
     */
    public String docker_container_prune_f() {
        return this.client.exec("docker container prune -f");
    }

    /**
     * 执行docker image prune -f命令
     *
     * @return 结果
     */
    public String docker_image_prune_f() {
        return this.client.exec("docker image prune -f");
    }

    /**
     * 执行docker network prune -f命令
     *
     * @return 结果
     */
    public String docker_network_prune_f() {
        return this.client.exec("docker network prune -f");
    }

    /**
     * 执行docker volume prune -f命令
     *
     * @return 结果
     */
    public String docker_volume_prune_f() {
        return this.client.exec("docker volume prune -f");
    }

    @Override
    public void close() throws Exception {
        this.client = null;
    }

    /**
     * 获取配置文件路径
     *
     * @return 配置文件
     */
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
