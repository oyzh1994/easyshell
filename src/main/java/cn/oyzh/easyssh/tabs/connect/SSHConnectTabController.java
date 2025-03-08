package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.trees.connect.SSHConnectTreeItem;
import cn.oyzh.fx.gui.tabs.ParentTabController;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHConnectTabController extends ParentTabController {

//    /**
//     * ssh客户端
//     */
//    @Getter
//    @Accessors(chain = true, fluent = true)
//    private SSHClient client;

    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHConnectTreeItem treeItem;

    /**
     * 终端
     */
    @FXML
    private SSHTermTabController sshTermTabController;

    /**
     * 文件
     */
    @FXML
    private SSHSftpTabController sshSftpTabController;

    public SSHClient client(){
        return this.treeItem.client();
    }

    /**
     * 设置ssh客户端
     *
     * @param treeItem ssh客户端
     */
    public void init(@NonNull SSHConnectTreeItem treeItem) {
        try {
            this.treeItem = treeItem;
//            if (!this.treeItem.isConnected()) {
//                this.client.start();
//            }
//            if (!this.client.isConnected()) {
//                MessageBox.warn(I18nHelper.connectFail());
//                return;
//            }
            this.sshTermTabController.init();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * ssh信息
     *
     * @return 当前ssh信息
     */
    protected SSHConnect sshConnect() {
        return this.treeItem.value();
    }

    @Override
    public List<? extends RichTabController> getSubControllers() {
        return List.of(this.sshTermTabController, this.sshSftpTabController);
    }
}
