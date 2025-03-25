package cn.oyzh.easyshell.tabs.connect.docker;

import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.fx.ShellContainerStatusComboBox;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellDockerTabController;
import cn.oyzh.easyshell.trees.docker.DockerContainerTableView;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDockerContainerTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    @FXML
    private ClearableTextField filterContainer;

    @FXML
    private DockerContainerTableView containerTable;

    @FXML
    private ShellContainerStatusComboBox containerStatus;

    private boolean initialized = false;

    public void init(DockerExec exec) {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            this.containerTable.setExec(exec);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabInit(RichTab tab) {
        try {
            super.onTabInit(tab);
            this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    this.init(this.client().dockerExec());
                }
            });
            this.filterContainer.addTextChangeListener((observableValue, aBoolean, t1) -> {
                this.containerTable.setFilterText(t1);
            });
            this.containerStatus.selectedIndexChanged((observableValue, aBoolean, t1) -> {
                this.containerTable.setStatus(t1.byteValue());
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellDockerTabController parent() {
        return (ShellDockerTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().getClient();
    }

    @FXML
    public void refreshContainer() {
//        // 设置执行对象
//        if (this.containerTable.getExec() == null) {
//            this.containerTable.setExec(this.client().dockerExec());
//        }
        StageManager.showMask(() -> this.containerTable.loadContainer());
    }

    @FXML
    private void deleteContainer() {
        this.containerTable.deleteContainer(false);
    }

    @FXML
    private void deleteContainerForce() {
        this.containerTable.deleteContainer(true);
    }
}
