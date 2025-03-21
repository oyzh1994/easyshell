package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.easyshell.sftp.download.SftpDownloadManager;
import cn.oyzh.easyshell.sftp.download.SftpDownloadTask;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.fx.sftp.SftpDownloadTableView;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * @author oyzh
 * @since 2025-03-15
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sftp/shellSftpDownload.fxml"
)
public class ShellSftpDownloadController extends StageController {

    @FXML
    private SftpDownloadTableView downloadTable;

    private SftpDownloadManager downloadManager;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        ShellClient client = this.getWindowProp("client");
        this.downloadManager = client.getSftpDownloadManager();
        this.downloadTable.setItem(downloadManager.getTasks());
    }

    @FXML
    private void cancelTask() {
        try {
            SftpDownloadTask task = this.downloadTable.getSelectedItem();
            if (task != null) {
                if (task.isFinished()) {
                    if (MessageBox.confirm(ShellI18nHelper.fileTip13())) {
                        this.downloadManager.remove(task);
                        this.downloadTable.removeItem(task);
                    }
                    return;
                }
                if (MessageBox.confirm(ShellI18nHelper.fileTip11())) {
                    this.downloadManager.cancel(task);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void removeTask() {
        try {
            SftpDownloadTask task = this.downloadTable.getSelectedItem();
            if (task != null && MessageBox.confirm(ShellI18nHelper.fileTip12())) {
                this.downloadManager.remove(task);
                this.downloadTable.removeItem(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.downloadManage();
    }
}
