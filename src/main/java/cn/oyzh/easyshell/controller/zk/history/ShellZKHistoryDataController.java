package cn.oyzh.easyshell.controller.zk.history;

import cn.oyzh.easyshell.fx.zk.ShellZKHistoryDataTableView;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;


/**
 * db数据传输业务
 *
 * @author oyzh
 * @since 2024/09/05
 */
@StageAttribute(
        value = FXConst.FXML_PATH + "zk/history/shellZKHistoryData.fxml"
)
public class ShellZKHistoryDataController extends StageController {

    /**
     * 数据列表
     */
    @FXML
    private ShellZKHistoryDataTableView listTable;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        ShellZKClient client = this.getProp("client");
        String nodePath = this.getProp("nodePath");
        this.listTable.init(client, nodePath);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.dataHistory();
    }
}
