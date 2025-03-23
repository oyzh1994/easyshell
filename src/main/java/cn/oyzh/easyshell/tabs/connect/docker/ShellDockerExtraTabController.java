package cn.oyzh.easyshell.tabs.connect.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.docker.DockerInfoController;
import cn.oyzh.easyshell.controller.docker.DockerVersionController;
import cn.oyzh.easyshell.docker.DockerExec;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellDockerTabController;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDockerExtraTabController extends SubTabController {

    @Override
    public ShellDockerTabController parent() {
        return (ShellDockerTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().getClient();
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
    private void dockerComposeVersion() {
        DockerExec exec = this.client().dockerExec();
        StageManager.showMask(() -> {
            try {
                String output = exec.docker_compose_version();
                MessageBox.info(output);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerRestart() {
        DockerExec exec = this.client().dockerExec();
        StageManager.showMask(() -> {
            try {
                String output = exec.docker_restart();
                if (StringUtil.isNotBlank(output)) {
                    MessageBox.warn(output);
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
                this.parent().loadContainer();
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
                this.parent().loadImage();
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
