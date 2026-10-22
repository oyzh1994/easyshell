package cn.oyzh.easyshell.ssh.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.ShellUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * docker输出解析器
 *
 * @author oyzh
 * @since 2025-03-12
 */
public class ShellDockerParser {

    /**
     * 私有构造，禁止实例化
     */
    private ShellDockerParser() {
    }

    /**
     * 解析容器
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellDockerContainer> ps(String output) {
        if (StringUtil.isBlank(output)
                || ShellUtil.isCommandNotFound(output)
                || ShellUtil.isWindowsCommandNotFound(output, "docker")) {
            //        if (StringUtil.isBlank(output) || StringUtil.containsIgnoreCase(output, "daemon")) {
            return Collections.emptyList();
        }
        if (JulLog.isInfoEnabled()) {
            JulLog.info(output);
        }
        List<ShellDockerContainer> containers = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            if (StringUtil.isBlank(line)) {
                continue;
            }
            String[] columns = line.split("\t", -1);
            if (columns.length < 7) {
                JulLog.warn("ignore invalid docker ps output line:{}", line);
                continue;
            }
            String containerId = columns[0];
            String image = columns[1];
            String command = columns[2];
            String created = columns[3];
            String status = columns[4];
            String ports = columns[5];
            String names = columns[6];

            ShellDockerContainer container = new ShellDockerContainer();
            container.setContainerId(containerId);
            container.setImage(image);
            container.setPorts(ports);
            container.setNames(names);
            container.setStatus(status);
            container.setCreated(created);
            container.setCommand(command);

            containers.add(container);
        }
        return containers;
    }

    /**
     * 解析镜像
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellDockerImage> images(String output) {
        if (StringUtil.isBlank(output)
                || ShellUtil.isCommandNotFound(output)
                || ShellUtil.isWindowsCommandNotFound(output, "docker")) {
            return Collections.emptyList();
        }
        if (JulLog.isInfoEnabled()) {
            JulLog.info(output);
        }
        List<ShellDockerImage> images = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            if (StringUtil.isBlank(line)) {
                continue;
            }
            String[] columns = line.split("\t", -1);
            if (columns.length < 5) {
                JulLog.warn("ignore invalid docker images output line:{}", line);
                continue;
            }
            String repository = columns[0];
            String tag = columns[1];
            String imageId = columns[2];
            String created = columns[3];
            String size = columns[4];

            ShellDockerImage image = new ShellDockerImage();
            image.setTag(tag);
            image.setSize(size);
            image.setImageId(imageId);
            image.setCreated(created);
            image.setRepository(repository);

            images.add(image);
        }
        return images;
    }

    /**
     * 解析资源
     *
     * @param output 输出
     * @return 结果
     */
    public static ShellDockerResource resource(String output) {
        if (StringUtil.isBlank(output)
                || ShellUtil.isCommandNotFound(output)
                || ShellUtil.isWindowsCommandNotFound(output, "docker")) {
            return null;
        }
        output = output.replace("\n", "");
        if (JulLog.isInfoEnabled()) {
            JulLog.info("docker resource:{}", output);
        }
        ShellDockerResource dockerResource = new ShellDockerResource();
        String[] cols = output.replace("\n", "").split("\t");
        if (cols.length > 0) {
            dockerResource.setMemory(parseLong(cols[0]));
        }
        if (cols.length > 1) {
            dockerResource.setMemorySwap(parseLong(cols[1]));
        }
        if (cols.length > 2) {
            dockerResource.setCpuShares(parseLong(cols[2]));
        }
        if (cols.length > 3) {
            dockerResource.setNanoCpus(parseLong(cols[3]));
        }
        if (cols.length > 4) {
            dockerResource.setCpuPeriod(parseLong(cols[4]));
        }
        if (cols.length > 5) {
            dockerResource.setCpuQuota(parseLong(cols[5]));
        }
        return dockerResource;
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            JulLog.warn("ignore invalid docker resource value:{}", value);
            return 0;
        }
    }

    /**
     * 解析端口
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellDockerPort> port(String output) {
        if (StringUtil.isBlank(output)
                || ShellUtil.isCommandNotFound(output)
                || ShellUtil.isWindowsCommandNotFound(output, "docker")) {
            return Collections.emptyList();
        }
        if (JulLog.isInfoEnabled()) {
            JulLog.info(output);
        }
        List<ShellDockerPort> ports = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            if (StringUtil.isBlank(line)) {
                continue;
            }
            String[] cols = line.split("->", -1);
            if (cols.length < 2) {
                JulLog.warn("ignore invalid docker port output line:{}", line);
                continue;
            }
            String innerPort = cols[0];
            String outerPort = cols[1];

            ShellDockerPort port = new ShellDockerPort();
            port.setInnerPort(innerPort);
            port.setOuterPort(outerPort);

            ports.add(port);
        }
        return ports;
    }

    /**
     * 解析镜像历史
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellDockerImageHistory> history(String output) {
        if (StringUtil.isBlank(output)
                || ShellUtil.isCommandNotFound(output)
                || ShellUtil.isWindowsCommandNotFound(output, "docker")) {
            return Collections.emptyList();
        }
        if (JulLog.isInfoEnabled()) {
            JulLog.info(output);
        }
        List<ShellDockerImageHistory> histories = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            String[] columns;
            if (line.contains("\u001f")) {
                columns = line.split("\u001f", -1);
            } else if (line.contains("\\r\t")) {
                columns = line.split("\\\\r\t", -1);
            } else {
                columns = line.split("\r\t", -1);
            }
            if (columns.length < 4) {
                JulLog.warn("ignore invalid docker history output line:{}", line);
                continue;
            }
            String imageId = columns[0];
            String created = columns[1];
            String createdBy = columns[2];
            String size = columns[3];
            String comment;
            if (columns.length >= 5) {
                comment = columns[4];
            } else {
                comment = "";
            }
            ShellDockerImageHistory history = new ShellDockerImageHistory();
            history.setSize(size);
            history.setImageId(imageId);
            history.setComment(comment);
            history.setCreated(created);
            history.setCreatedBy(createdBy);

            histories.add(history);
        }
        return histories;
    }
}
