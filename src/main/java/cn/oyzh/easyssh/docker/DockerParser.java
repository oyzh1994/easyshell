package cn.oyzh.easyssh.docker;

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
        // 4. 解析容器信息
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            // 按\t分割列
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
//            System.out.printf("容器ID: %s | 镜像: %s | 状态: %s%n", containerId, image, status);
        }
        return containers;
    }

    public static List<DockerImage> images(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        JulLog.info(output);
        List<DockerImage> images = new ArrayList<>();
        // 4. 解析容器信息
        String[] lines = output.split("\n");
        for (String line : lines) { // 跳过表头
            // 按\t分割列
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

}
