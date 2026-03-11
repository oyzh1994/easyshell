package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerImage;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerTag;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * docker标签更改业务
 *
 * @author oyzh
 * @since 2026/03/11
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "docker/shellDockerTag.fxml"
)
public class ShellDockerTagController extends StageController {

    /**
     * 新名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 镜像名称
     */
    @FXML
    private ReadOnlyTextField imageName;

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
        this.name.setText(this.image.getImageName());
        this.imageName.setText(this.image.getImageName());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateTag();
    }

    @FXML
    private void run() {
        try {
            ShellDockerTag tag = new ShellDockerTag();
            tag.setImageName(this.image.getImageName());
            tag.setNewImageName(this.name.getTextTrim());
            this.exec.docker_tag(tag);
            ShellEventUtil.imageTag(this.exec);
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
