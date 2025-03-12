package cn.oyzh.easyssh.docker;

import cn.oyzh.common.log.JulLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class DockerParser {

    public List<DockerContainer> ps(String output) {
        JulLog.info(output);
        List<DockerContainer> containers = new ArrayList<>();
        // 4. 解析容器信息
        String[] lines = output.split("\n");
        for (int i = 1; i < lines.length; i++) { // 跳过表头
            String[] columns = lines[i].split("\\s{2,}"); // 按多空格分割列
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

            container.setCommand(command);
            container.setCreated(created);
            container.setStatus(status);
            container.setPorts(ports);
            container.setNames(names);

            containers.add(container);
            System.out.printf("容器ID: %s | 镜像: %s | 状态: %s%n", containerId, image, status);
        }
        return containers;
    }

}
