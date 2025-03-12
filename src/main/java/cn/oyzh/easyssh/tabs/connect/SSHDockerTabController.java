package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.trees.docker.SSHContainerTableView;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHDockerTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    @FXML
    private SSHContainerTableView containerTab;

    private boolean initialized = false;

    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.containerTab.setClient(this.client());
        this.containerTab.loadContainer();
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client().close();
    }

    @Override
    public void onTabInit(RichTab tab) {
        try {
            super.onTabInit(tab);
            this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    this.init();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public SSHConnectTabController parent() {
        return (SSHConnectTabController) super.parent();
    }

    public SSHClient client() {
        return this.parent().client();
    }

    @FXML
    private void refreshContainer() {
        try {
            this.containerTab.loadContainer();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void deleteFile() {
        try {
//            this.containerTab.deleteFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }


}
