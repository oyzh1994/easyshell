package cn.oyzh.easyssh.trees.docker;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.docker.DockerContainer;
import cn.oyzh.easyssh.docker.DockerParser;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-12
 */
public class SSHContainerTableView extends FXTableView<DockerContainer> {

    private SSHClient client;

    public SSHClient getClient() {
        return client;
    }

    public void setClient(SSHClient client) {
        this.client = client;
    }

    private List<DockerContainer> containers;

    public void loadContainer() {
        String output = this.client.exec_docker_ps();
        DockerParser parser = new DockerParser();
        this.containers = parser.ps(output);
        this.setItem(this.containers);
    }

    private String filterText;

    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshContainer();
        }
    }

    public void refreshContainer() {
        if (this.containers == null) {
            this.loadContainer();
        } else {
            this.setItem(this.doFilter(this.containers));
        }
    }


    private List<DockerContainer> doFilter(List<DockerContainer> files) {
//        if (CollectionUtil.isNotEmpty(files)) {
//            return files.stream()
//                    .filter(f -> !StringUtil.isNotEmpty(this.filterText) || StringUtil.containsIgnoreCase(f.getFileName(), this.filterText))
//                    .collect(Collectors.toList());
//        }
        return files;
    }
}
