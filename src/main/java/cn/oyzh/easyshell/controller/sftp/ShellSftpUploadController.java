package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.easyshell.trees.sftp.SftpUploadTableView;
import cn.oyzh.easyshell.sftp.upload.SftpUploadManager;
import cn.oyzh.easyshell.sftp.upload.SftpUploadTask;
import cn.oyzh.easyshell.ssh.ShellClient;
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
        value = FXConst.FXML_PATH + "sftp/shellSftpUpload.fxml"
)
public class ShellSftpUploadController extends StageController {

    @FXML
    private SftpUploadTableView uploadTable;

    private SftpUploadManager uploadManager;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        ShellClient client = this.getWindowProp("client");
        this.uploadManager = client.getSftpUploadManager();
        this.uploadTable.setItem(uploadManager.getTasks());
    }

    @FXML
    private void cancelTask() {
        try {
            SftpUploadTask task = this.uploadTable.getSelectedItem();
            if (task != null) {
                if (task.isFinished()) {
                    if (MessageBox.confirm(ShellI18nHelper.fileTip13())) {
                        this.uploadManager.remove(task);
                        this.uploadTable.removeItem(task);
                    }
                    return;
                }
                if (MessageBox.confirm(ShellI18nHelper.fileTip11())) {
                    this.uploadManager.cancel(task);
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
            SftpUploadTask task = this.uploadTable.getSelectedItem();
            if (task != null && MessageBox.confirm(ShellI18nHelper.fileTip12())) {
                this.uploadManager.remove(task);
                this.uploadTable.removeItem(task);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.uploadManage();
    }
}
