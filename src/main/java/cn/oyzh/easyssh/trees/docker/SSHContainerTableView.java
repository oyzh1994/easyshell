package cn.oyzh.easyssh.trees.docker;

import cn.oyzh.easyssh.docker.DockerContainer;
import cn.oyzh.easyssh.docker.DockerParser;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.plus.controls.table.FXTableView;

import java.util.List;

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

    public void loadContainer() {
        String output = this.client.exec_docker_ps();
        DockerParser parser = new DockerParser();
        List<DockerContainer> list = parser.ps(output);
        this.setItem(list);
    }
}
