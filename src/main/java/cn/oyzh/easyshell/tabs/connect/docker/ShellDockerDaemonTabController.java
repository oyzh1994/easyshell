package cn.oyzh.easyshell.tabs.connect.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.docker.DockerInfoController;
import cn.oyzh.easyshell.controller.docker.DockerVersionController;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.fx.ShellContainerStatusComboBox;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellConnectTabController;
import cn.oyzh.easyshell.trees.docker.DockerContainerTableView;
import cn.oyzh.easyshell.trees.docker.DockerImageTableView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDockerDaemonTabController extends SubTabController {

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

    @FXML
    private ClearableTextField filterImage;

    @FXML
    private DockerImageTableView imageTable;

    private boolean initialized = false;

    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            DockerExec exec = this.client().dockerExec();
            String output = exec.docker_v();
            if (StringUtil.isBlank(output)) {
                MessageBox.info(ShellI18nHelper.connectTip5());
                return;
            }
//            boolean exist = this.client().openSftp().exist("/usr/bin/docker");
//            if (!exist) {
//                MessageBox.info(ShellI18nHelper.connectTip5());
//                return;
//            }
            this.containerTable.setExec(exec);
            this.imageTable.setExec(exec);
            StageManager.showMask(() -> {
                this.containerTable.loadContainer();
                this.imageTable.loadImage();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
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
    public ShellConnectTabController parent() {
        return (ShellConnectTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().getClient();
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
        StageManager.showMask(() -> this.imageTable.loadImage());
    }

    @FXML
    private void deleteImage() {
        this.imageTable.deleteImage(false);
    }

    @FXML
    private void deleteImageForce() {
        this.imageTable.deleteImage(true);
    }

    @FXML
    private void dockerInfo() {
        DockerExec exec = this.client().dockerExec();
        StageManager.showMask(() -> {
            try {
                String output = exec.docker_info();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    FXUtil.runLater(() -> {
                        StageAdapter adapter = StageManager.parseStage(DockerInfoController.class);
                        adapter.setProp("info", output);
                        adapter.display();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerVersion() {
        DockerExec exec = this.client().dockerExec();
        StageManager.showMask(() -> {
            try {
                String output = exec.docker_version();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    FXUtil.runLater(() -> {
                        StageAdapter adapter = StageManager.parseStage(DockerVersionController.class);
                        adapter.setProp("version", output);
                        adapter.display();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerPruneContainer() {
        DockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearData(), I18nHelper.areYouSure())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                exec.docker_container_prune_f();
                this.containerTable.loadContainer();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerPruneImage() {
        DockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearData(), I18nHelper.areYouSure())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                exec.docker_image_prune_f();
                this.imageTable.loadImage();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerPruneNetwork() {
        DockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearData(), I18nHelper.areYouSure())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                exec.docker_network_prune_f();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerPruneVolume() {
        DockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearData(), I18nHelper.areYouSure())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                exec.docker_volume_prune_f();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }
}
