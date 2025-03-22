package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.easyshell.fx.sftp.SftpDownloadTaskTableView;
import cn.oyzh.easyshell.fx.sftp.SftpUploadTaskTableView;
import cn.oyzh.easyshell.sftp.download.SftpDownloadManager;
import cn.oyzh.easyshell.sftp.upload.SftpUploadManager;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * @author oyzh
 * @since 2025-03-15
 */
@StageAttribute(
        value = FXConst.FXML_PATH + "sftp/shellSftpManage.fxml"
)
public class ShellSftpManageController extends StageController {

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

    /**
     * 上传管理器
     */
    private SftpUploadManager uploadManager;

    /**
     * 下载管理器
     */
    private SftpDownloadManager downloadManager;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.uploadManager.setTaskChangedCallback(this::initUploadTable);
        this.downloadManager.setTaskChangedCallback(this::initUploadTable);
    }

    protected void initUploadTable() {
        this.uploadTable.setItem(this.uploadManager.getTasks());
    }

    protected void initDownloadTable() {
        this.downloadTable.setItem(this.downloadManager.getTasks());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        ShellClient client = this.getWindowProp("client");
        this.uploadManager = client.getUploadManager();
        this.downloadManager = client.getDownloadManager();
        this.initUploadTable();
        this.initDownloadTable();
        if (!this.uploadManager.isCompleted()) {
            this.tabPane.select(0);
        } else if (!this.downloadManager.isCompleted()) {
            this.tabPane.select(1);
        } else if (!this.downloadManager.isEmpty() && !this.uploadManager.isEmpty()) {
            this.tabPane.select(1);
        }
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return ShellI18nHelper.fileTip17();
    }
}
