package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.docker.ShellDockerRunEnvTableView;
import cn.oyzh.easyshell.fx.docker.ShellDockerRunLabelTableView;
import cn.oyzh.easyshell.fx.docker.ShellDockerRunPortTableView;
import cn.oyzh.easyshell.fx.docker.ShellDockerRunVolumeTableView;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerImage;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerRun;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * docker容器运行业务
 *
 * @author oyzh
 * @since 2025/07/03
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerRun.fxml"
)
public class ShellDockerRunController extends StageController {

    /**
     * 容器名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 镜像名称
     */
    @FXML
    private ReadOnlyTextField imageName;

    /**
     * 重启策略
     */
    @FXML
    private FXToggleGroup restart;

    /**
     * -i参数
     */
    @FXML
    private CheckBox i;

    /**
     * -t参数
     */
    @FXML
    private CheckBox t;

    /**
     * -d参数
     */
    @FXML
    private CheckBox d;

    /**
     * -rm参数
     */
    @FXML
    private FXCheckBox rm;

    /**
     * -privileged参数
     */
    @FXML
    private FXCheckBox privileged;

    /**
     * 端口
     */
    @FXML
    private ShellDockerRunPortTableView portTable;

    /**
     * 卷
     */
    @FXML
    private ShellDockerRunVolumeTableView volumeTable;

    /**
     * 环境
     */
    @FXML
    private ShellDockerRunEnvTableView envTable;

    /**
     * 标签
     */
    @FXML
    private ShellDockerRunLabelTableView labelTable;

    /**
     * exec对象
     */
    private ShellDockerExec exec;

    /**
     * 镜像
     */
    private ShellDockerImage image;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.exec = this.getProp("exec");
        this.image = this.getProp("image");
        this.imageName.setText(this.image.getImageName());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.runContainer();
    }

    @FXML
    private void run() {
        ShellDockerRun run = new ShellDockerRun();
        run.setI(this.i.isSelected());
        run.setD(this.d.isSelected());
        run.setT(this.t.isSelected());
        run.setRm(this.rm.isSelected());
        run.setEnvs(this.envTable.getItems());
        run.setPorts(this.portTable.getItems());
        run.setImageId(this.image.getImageId());
        run.setContainerName(this.name.getText());
        run.setLabels(this.labelTable.getItems());
        run.setVolumes(this.volumeTable.getItems());
        run.setRestart(this.restart.selectedUserData());
        run.setPrivileged(this.privileged.isSelected());

        // 执行
        StageManager.showMask(() -> {
            try {
                String output = this.exec.docker_run(run);
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("docker run result: {}", output);
                }
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    MessageBox.info(I18nHelper.containerId() + ":" + output);
                    ShellEventUtil.containerRun(this.exec);
                    this.closeWindow();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });

    }

    /**
     * 添加端口
     */
    @FXML
    private void addPort() {
        ShellDockerRun.DockerPort port = new ShellDockerRun.DockerPort();
        this.portTable.addItem(port);
        this.portTable.clearSelection();
        this.portTable.selectLast();
    }

    /**
     * 删除端口
     */
    @FXML
    private void deletePort() {
        try {
            List<ShellDockerRun.DockerPort> ports = this.portTable.getSelectedItems();
            if (CollectionUtil.isEmpty(ports)) {
                return;
            }
            if (MessageBox.confirm(I18nHelper.deleteData())) {
                this.portTable.removeItem(ports);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加卷
     */
    @FXML
    private void addVolume() {
        ShellDockerRun.DockerVolume volume = new ShellDockerRun.DockerVolume();
        this.volumeTable.addItem(volume);
        this.volumeTable.clearSelection();
        this.volumeTable.selectLast();
    }

    /**
     * 删除卷
     */
    @FXML
    private void deleteVolume() {
        try {
            List<ShellDockerRun.DockerVolume> volumes = this.volumeTable.getSelectedItems();
            if (CollectionUtil.isEmpty(volumes)) {
                return;
            }
            if (MessageBox.confirm(I18nHelper.deleteData())) {
                this.volumeTable.removeItem(volumes);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加环境
     */
    @FXML
    private void addEnv() {
        ShellDockerRun.DockerEnv env = new ShellDockerRun.DockerEnv();
        this.envTable.addItem(env);
        this.envTable.clearSelection();
        this.envTable.selectLast();
    }

    /**
     * 删除环境
     */
    @FXML
    private void deleteEnv() {
        try {
            List<ShellDockerRun.DockerEnv> envs = this.envTable.getSelectedItems();
            if (CollectionUtil.isEmpty(envs)) {
                return;
            }
            if (MessageBox.confirm(I18nHelper.deleteData())) {
                this.envTable.removeItem(envs);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 添加标签
     */
    @FXML
    private void addLabel() {
        ShellDockerRun.DockerLabel label = new ShellDockerRun.DockerLabel();
        this.labelTable.addItem(label);
        this.labelTable.clearSelection();
        this.labelTable.selectLast();
    }

    /**
     * 删除标签
     */
    @FXML
    private void deleteLabel() {
        try {
            List<ShellDockerRun.DockerLabel> labels = this.labelTable.getSelectedItems();
            if (CollectionUtil.isEmpty(labels)) {
                return;
            }
            if (MessageBox.confirm(I18nHelper.deleteData())) {
                this.labelTable.removeItem(labels);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }
}
