package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.easyshell.ssh2.docker.ShellDockerPort;
import cn.oyzh.easyshell.fx.docker.ShellDockerPortTableView;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
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
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerPort.fxml"
)
public class ShellDockerPortController extends StageController {

    /**
     * 端口表
     */
    @FXML
    private ShellDockerPortTableView portTable;

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
