package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.easyshell.docker.DockerHistory;
import cn.oyzh.easyshell.trees.docker.DockerHistoryTableView;
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
 * docker镜像历史业务
 *
 * @author oyzh
 * @since 2025/03/14
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/dockerHistory.fxml"
)
public class DockerHistoryController extends StageController {

    /**
     * 历史表
     */
    @FXML
    private DockerHistoryTableView historyTable;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        List<DockerHistory> histories = this.getWindowProp("histories");
        this.historyTable.setItem(histories);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.imageHistory();
    }
}
