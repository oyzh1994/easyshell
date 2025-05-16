package cn.oyzh.easyshell.controller.file;

import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.fx.file.ShellFileDownloadTaskTableView;
import cn.oyzh.easyshell.fx.file.ShellFileUploadTaskTableView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * @author oyzh
 * @since 2025-03-15
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "file/shellFileManage.fxml"
)
public class ShellFileManageController extends StageController {

    /**
     * 根节点
     */
    @FXML
    private FXTabPane root;

    /**
     * 上传列表
     */
    @FXML
    private ShellFileUploadTaskTableView uploadTable;

    /**
     * 下载列表
     */
    @FXML
    private ShellFileDownloadTaskTableView downloadTable;

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        ShellFileClient<?> client = this.getProp("client");
        // 处理上传列表
        this.uploadTable.setItems(client.uploadTasks());
        // 处理下载列表
        this.downloadTable.setItems(client.downloadTasks());
        // 上传为空，下载不为空，则选择下载tab
        if (client.isUploadTaskEmpty() && !client.isDownloadTaskEmpty()) {
            this.root.select(1);
        }
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return ShellI18nHelper.fileTip17();
    }
}
