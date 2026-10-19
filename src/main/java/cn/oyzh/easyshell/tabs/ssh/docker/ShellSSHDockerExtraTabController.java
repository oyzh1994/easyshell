package cn.oyzh.easyshell.tabs.ssh.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHDockerTabController;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;

/**
 * docker扩展tab内容组件
 *
 * @author oyzh
 * @since 2025-03-12
 */
public class ShellSSHDockerExtraTabController extends SubTabController {

    @Override
    public ShellSSHDockerTabController parent() {
        return (ShellSSHDockerTabController) super.parent();
    }

    /**
     * 获取ssh客户端
     *
     * @return ssh客户端
     */
    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    /**
     * docker信息
     */
    @FXML
    private void dockerInfo() {
        ShellDockerExec exec = this.client().dockerExec();
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String output = exec.docker_info();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
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
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * docker版本
     */
    @FXML
    private void dockerVersion() {
        ShellDockerExec exec = this.client().dockerExec();
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String output = exec.docker_version();
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                } else {
//                    FXUtil.runLater(() -> {
//                        StageAdapter adapter = StageManager.parseStage(ShellDockerVersionController.class, StageManager.getFrontWindow());
//                        adapter.setProp("version", output);
//                        adapter.display();
//                    });
                    ShellViewFactory.dockerVersion(output);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * docker compose版本
     */
    @FXML
    private void dockerComposeVersion() {
        ShellDockerExec exec = this.client().dockerExec();
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String output = exec.docker_compose_version();
                MessageBox.info(output, this.getWindow());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * 重启docker
     */
    @FXML
    private void dockerRestart() {
        if (this.client().isMacos() || this.client().isWindows()) {
            MessageBox.warn(I18nHelper.operationNotSupport(), this.getWindow());
            return;
        }
        if (!MessageBox.confirm(I18nHelper.restart() + " docker?", this.getWindow())) {
            return;
        }
        ShellDockerExec exec = this.client().dockerExec();
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String output = exec.docker_restart();
                if (StringUtil.isNotBlank(output)) {
                    MessageBox.warn(output, this.getWindow());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * 清理容器
     */
    @FXML
    private void dockerPruneContainer() {
        ShellDockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearContainerData(), I18nHelper.areYouSure(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> {
            try {
                exec.docker_container_prune_f();
                this.parent().loadContainer();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * 清理镜像
     */
    @FXML
    private void dockerPruneImage() {
        ShellDockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearImageData(), I18nHelper.areYouSure(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> {
            try {
                exec.docker_image_prune_f();
                this.parent().loadImage();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * 清理网络
     */
    @FXML
    private void dockerPruneNetwork() {
        ShellDockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearNetworkData(), I18nHelper.areYouSure(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> {
            try {
                exec.docker_network_prune_f();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    /**
     * 清理数据卷
     */
    @FXML
    private void dockerPruneVolume() {
        ShellDockerExec exec = this.client().dockerExec();
        if (!MessageBox.confirm(I18nHelper.clearVolumeData(), I18nHelper.areYouSure(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> {
            try {
                exec.docker_volume_prune_f();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }
}
