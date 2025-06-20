package cn.oyzh.easyshell.tabs.ssh.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sshj.docker.ShellDockerExec;
import cn.oyzh.easyshell.sshj.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHDockerTabController;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellSSHDockerExtraTabController extends SubTabController {

    @Override
    public ShellSSHDockerTabController parent() {
        return (ShellSSHDockerTabController) super.parent();
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    @FXML
    private void dockerInfo() {
        ShellDockerExec exec = this.client().dockerExec();
        StageManager.showMask(() -> {
            try {
                String output = exec.docker_info();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
//                    FXUtil.runLater(() -> {
//                        StageAdapter adapter = StageManager.parseStage(ShellDockerInfoController.class);
//                        adapter.setProp("info", output);
//                        adapter.display();
//                    });
                    ShellViewFactory.dockerInfo(output);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerVersion() {
        ShellDockerExec exec = this.client().dockerExec();
        StageManager.showMask(() -> {
            try {
                String output = exec.docker_version();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
//                    FXUtil.runLater(() -> {
//                        StageAdapter adapter = StageManager.parseStage(ShellDockerVersionController.class, StageManager.getPrimaryStage());
//                        adapter.setProp("version", output);
//                        adapter.display();
//                    });
                    ShellViewFactory.dockerVersion(output);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void dockerComposeVersion() {
        ShellDockerExec exec = this.client().dockerExec();
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
        if (this.client().isMacos() || this.client().isWindows()) {
            MessageBox.warn(I18nHelper.operationNotSupport());
            return;
        }
        ShellDockerExec exec = this.client().dockerExec();
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
        ShellDockerExec exec = this.client().dockerExec();
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
        ShellDockerExec exec = this.client().dockerExec();
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
        ShellDockerExec exec = this.client().dockerExec();
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
        ShellDockerExec exec = this.client().dockerExec();
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
