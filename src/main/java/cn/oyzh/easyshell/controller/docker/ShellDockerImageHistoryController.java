package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.easyshell.ssh.docker.ShellDockerImageHistory;
import cn.oyzh.easyshell.fx.docker.ShellDockerImageHistoryTableView;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * docker镜像历史业务
 *
 * @author oyzh
 * @since 2025/03/14
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerImageHistory.fxml"
)
public class ShellDockerImageHistoryController extends StageController {

    /**
     * 历史表
     */
    @FXML
    private ShellDockerImageHistoryTableView historyTable;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        List<ShellDockerImageHistory> histories = this.getProp("histories");
        this.historyTable.setItem(histories);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.imageHistory();
    }
}
