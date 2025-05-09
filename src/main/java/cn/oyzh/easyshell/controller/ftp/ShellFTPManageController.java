package cn.oyzh.easyshell.controller.ftp;

import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.fx.ftp.ShellFTPDownloadFileTableView;
import cn.oyzh.easyshell.fx.ftp.ShellFTPUploadTaskTableView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * @author oyzh
 * @since 2025-03-15
 */
@StageAttribute(
        value = FXConst.FXML_PATH + "ftp/shellFTPManage.fxml"
)
public class ShellFTPManageController extends StageController {

    /**
     * 上传表
     */
    @FXML
    private ShellFTPUploadTaskTableView uploadTable;

    /**
     * 下载表
     */
    @FXML
    private ShellFTPDownloadFileTableView downloadTable;

    private ShellFTPClient client;

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    protected void initUploadTable() {
        this.uploadTable.setItem(this.client.uploadTasks());
    }

    protected void initDownloadTable() {
        this.downloadTable.setItem(this.client.getDownloadFiles());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.client = this.getProp("client");
        this.initUploadTable();
        this.initDownloadTable();
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return ShellI18nHelper.fileTip17();
    }
}
