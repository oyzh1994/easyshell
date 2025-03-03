package cn.oyzh.easyssh.tabs.terminal;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.ssh.SSHClient;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHTerminalTabContentController {

    /**
     * ssh客户端
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHClient client;

//    /**
//     * ssh命令行文本域
//     */
//    @FXML
//    private SSHShellTerminalTextArea terminal;

    /**
     * 设置ssh客户端
     *
     * @param client ssh客户端
     */
    public void client(@NonNull SSHClient client) {
        this.client = client;
//        this.terminal.init(client);
    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    protected SSHConnect info() {
        return this.client.sshInfo();
    }

}
