package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.easyshell.docker.ShellDockerPort;
import cn.oyzh.easyshell.fx.docker.DockerPortTableView;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * docker容器端口业务
 *
 * @author oyzh
 * @since 2025/03/14
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/dockerPort.fxml"
)
public class DockerPortController extends StageController {

    /**
     * 端口表
     */
    @FXML
    private DockerPortTableView portTable;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        List<ShellDockerPort> ports = this.getProp("ports");
        this.portTable.setItem(ports);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.dockerPorts();
    }
}
