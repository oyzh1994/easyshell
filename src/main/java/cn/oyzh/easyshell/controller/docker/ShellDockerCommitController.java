package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerCommit;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerContainer;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * docker容器保存业务
 *
 * @author oyzh
 * @since 2025/07/03
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerCommit.fxml"
)
public class ShellDockerCommitController extends StageController {

    /**
     * 镜像名称
     */
    @FXML
    private ReadOnlyTextField containerName;

    /**
     * 仓库
     */
    @FXML
    private ClearableTextField repository;

    /**
     * 标签
     */
    @FXML
    private ClearableTextField tag;

    /**
     * 注释
     */
    @FXML
    private FXTextArea comment;

    /**
     * exec对象
     */
    private ShellDockerExec exec;

    /**
     * 镜像
     */
    private ShellDockerContainer container;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.exec = this.getProp("exec");
        this.container = this.getProp("container");
        this.repository.setText(this.container.getImage());
        this.containerName.setText(this.container.getNames());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.saveContainer();
    }

    @FXML
    private void run() {
        StageManager.showMask(() -> {
            try {
                ShellDockerCommit commit = new ShellDockerCommit();
                commit.setTag(this.tag.getTextTrim());
                commit.setComment(this.comment.getTextTrim());
                commit.setRepository(this.repository.getTextTrim());
                commit.setContainerId(this.container.getContainerId());
                String output = this.exec.docker_commit(commit);
                if (StringUtil.isBlank(output)) {
                    MessageBox.warn(I18nHelper.operationFail());
                } else {
                    MessageBox.info(I18nHelper.imageId() + ":" + output);
                    ShellEventUtil.containerCommit(this.exec);
                    this.closeWindow();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }
}
