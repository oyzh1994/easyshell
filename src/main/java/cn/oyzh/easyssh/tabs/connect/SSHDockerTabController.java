package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.docker.DockerExec;
import cn.oyzh.easyssh.fx.SSHContainerStatusComboBox;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.trees.docker.SSHContainerTableView;
import cn.oyzh.easyssh.trees.docker.SSHImageTableView;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
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
    private ClearableTextField filterContainer;

    @FXML
    private SSHContainerTableView containerTable;

    @FXML
    private SSHContainerStatusComboBox containerStatus;

    @FXML
    private ClearableTextField filterImage;

    @FXML
    private SSHImageTableView imageTable;

    private boolean initialized = false;

    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        DockerExec exec = this.client().dockerExec();
        this.containerTable.setExec(exec);
        this.imageTable.setExec(exec);
        StageManager.showMask(() -> {
            this.containerTable.loadContainer();
            this.imageTable.loadImage();
        });
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
            this.filterContainer.addTextChangeListener((observableValue, aBoolean, t1) -> {
                this.containerTable.setFilterText(t1);
            });
            this.containerStatus.selectedIndexChanged((observableValue, aBoolean, t1) -> {
                this.containerTable.setStatus(t1.byteValue());
            });
            this.filterImage.addTextChangeListener((observableValue, aBoolean, t1) -> {
                this.imageTable.setFilterText(t1);
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
        this.containerTable.loadContainer();
    }

    @FXML
    private void deleteContainer() {
        this.containerTable.deleteContainer(false);
    }

    @FXML
    private void deleteContainerForce() {
        this.containerTable.deleteContainer(true);
    }

    @FXML
    private void refreshImage() {
        this.imageTable.loadImage();
    }

    @FXML
    private void deleteImage() {
        this.imageTable.deleteImage(false);
    }

    @FXML
    private void deleteImageForce() {
        this.imageTable.deleteImage(true);
    }
}
