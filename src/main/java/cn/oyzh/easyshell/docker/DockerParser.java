package cn.oyzh.easyshell.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class DockerParser {

    private DockerParser() {
    }

    public static List<DockerContainer> ps(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        JulLog.info(output);
        List<DockerContainer> containers = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            String[] columns = line.split("\t");
            String containerId = columns[0];
            String image = columns[1];
            String command = columns[2];
            String created = columns[3];
            String status = columns[4];
            String ports = columns[5];
            String names = columns[6];

            DockerContainer container = new DockerContainer();
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

    public static List<DockerImage> images(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        JulLog.info(output);
        List<DockerImage> images = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            String[] columns = line.split("\t");
            String repository = columns[0];
            String tag = columns[1];
            String imageId = columns[2];
            String created = columns[3];
            String size = columns[4];

            DockerImage image = new DockerImage();
            image.setTag(tag);
            image.setSize(size);
            image.setImageId(imageId);
            image.setCreated(created);
            image.setRepository(repository);

            images.add(image);
        }
        return images;
    }

    public static DockerResource resource(String output) {
        if (StringUtil.isBlank(output)) {
            return null;
        }
        output = output.replaceAll("\n", "");
        JulLog.info("docker resource:{}", output);
        DockerResource dockerResource = new DockerResource();
        String[] cols = output.replace("\n", "").split("\t");
        if (cols.length > 0) {
            dockerResource.setMemory(Long.parseLong(cols[0]));
        }
        if (cols.length > 1) {
            dockerResource.setMemorySwap(Long.parseLong(cols[1]));
        }
        if (cols.length > 2) {
            dockerResource.setCpuShares(Long.parseLong(cols[2]));
        }
        if (cols.length > 3) {
            dockerResource.setNanoCpus(Long.parseLong(cols[3]));
        }
        if (cols.length > 4) {
            dockerResource.setCpuPeriod(Long.parseLong(cols[4]));
        }
        if (cols.length > 5) {
            dockerResource.setCpuQuota(Long.parseLong(cols[5]));
        }
        return dockerResource;
    }

    public static List<DockerPort> port(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        JulLog.info(output);
        List<DockerPort> ports = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            String[] cols = line.split("->");
            String outerPort = cols[0];
            String innerPort = cols[1];

            DockerPort port = new DockerPort();
            port.setInnerPort(innerPort);
            port.setOuterPort(outerPort);

            ports.add(port);
        }
        return ports;
    }

    public static List<DockerHistory> history(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        JulLog.info(output);
        List<DockerHistory> histories = new ArrayList<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            String[] columns = line.split("\r\t");
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
            DockerHistory history = new DockerHistory();
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
