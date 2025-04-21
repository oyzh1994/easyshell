package cn.oyzh.easyshell.tabs.connect.docker;

import cn.oyzh.easyshell.docker.ShellDockerExec;
import cn.oyzh.easyshell.fx.docker.ShellDockerImageTableView;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellDockerTabController;
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
public class ShellDockerImageTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    @FXML
    private ClearableTextField filterImage;

    @FXML
    private ShellDockerImageTableView imageTable;

    private boolean initialized = false;

    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            ShellDockerExec exec = this.client().dockerExec();
            this.imageTable.setExec(exec);
            this.refreshImage();
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
                    this.init();
                }
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
    public ShellDockerTabController parent() {
        return (ShellDockerTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().getClient();
    }

    @FXML
    public void refreshImage() {
        StageManager.showMask(() -> this.imageTable.loadImage());
    }

    @FXML
    private void deleteImage() {
        this.imageTable.deleteImage(this.imageTable.getSelectedItem(), false);
    }

    @FXML
    private void deleteImageForce() {
        this.imageTable.deleteImage(this.imageTable.getSelectedItem(), true);
    }
}
