package cn.oyzh.easyshell.controller.zk.history;

import cn.oyzh.easyshell.dto.zk.ShellZKHistoryData;
import cn.oyzh.easyshell.fx.ShellDataEditor;
import cn.oyzh.easyshell.fx.zk.ShellZKHistoryDataTableView;
import cn.oyzh.easyshell.util.zk.ShellZKDataUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;


/**
 * zk数据历史业务
 *
 * @author oyzh
 * @since 2024/09/05
 */
@StageAttribute(
//        modality = Modality.WINDOW_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "zk/history/shellZKHistoryData.fxml"
)
public class ShellZKHistoryDataController extends StageController {

    /**
     * 数据列表
     */
    @FXML
    private ShellZKHistoryDataTableView listTable;

    /**
     * 编辑器
     */
    @FXML
    private ShellDataEditor editor;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.listTable.selectedItemChanged((observable, oldValue, newValue) -> {
            this.showData(newValue);
        });
    }

    /**
     * 显示数据
     *
     * @param data 数据
     */
    private void showData(ShellZKHistoryData data) {
        if (data == null) {
            this.editor.clear();
            this.editor.disable();
        } else {
            StageManager.showMask(() -> {
                try {
                    byte[] bytes = ShellZKDataUtil.getHistory(listTable.getNodePath(), data.getSaveTime(), listTable.getClient());
                    this.editor.showData(bytes);
                    this.editor.enable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            });
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        ShellZKClient client = this.getProp("client");
        String nodePath = this.getProp("nodePath");
        this.listTable.init(client, nodePath);
        this.appendTitle("[" + nodePath + "]");
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.dataHistory();
    }

    @Override
    public void destroy() {
        this.editor.destroy();
        super.destroy();
    }
}
