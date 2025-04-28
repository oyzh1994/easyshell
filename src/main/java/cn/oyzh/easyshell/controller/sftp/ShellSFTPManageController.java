package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.easyshell.fx.sftp.SftpDownloadTaskTableView;
import cn.oyzh.easyshell.fx.sftp.SftpUploadTaskTableView;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPUploadTask;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * @author oyzh
 * @since 2025-03-15
 */
@StageAttribute(
        value = FXConst.FXML_PATH + "sftp/shellSftpManage.fxml"
)
public class ShellSFTPManageController extends StageController {

    /**
     * 组件
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 上传表
     */
    @FXML
    private SftpUploadTaskTableView uploadTable;

    /**
     * 下载表
     */
    @FXML
    private SftpDownloadTaskTableView downloadTable;

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        ShellSFTPClient client = this.getProp("client");
        // 处理上传列表
        this.uploadTable.setItem(client.getUploadTasks());
        client.getUploadTasks().addListener((ListChangeListener<ShellSFTPUploadTask>) change -> {
            this.uploadTable.setItem(client.getUploadTasks());
        });
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return ShellI18nHelper.fileTip17();
    }
}
