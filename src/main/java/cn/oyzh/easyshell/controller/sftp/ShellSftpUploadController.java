//package cn.oyzh.easyshell.controller.sftp;
//
//import cn.oyzh.easyshell.sftp.upload.ShellSftpUploadManager;
//import cn.oyzh.easyshell.sftp.upload.ShellSftpUploadTask;
//import cn.oyzh.easyshell.shell.ShellClient;
//import cn.oyzh.easyshell.fx.sftp.SftpUploadTaskTableView;
//import cn.oyzh.easyshell.util.ShellI18nHelper;
//import cn.oyzh.fx.plus.FXConst;
//import cn.oyzh.fx.plus.controller.StageController;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.fx.plus.window.FXStageStyle;
//import cn.oyzh.fx.plus.window.StageAttribute;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.fxml.FXML;
//import javafx.stage.Modality;
//import javafx.stage.WindowEvent;
//
///**
// * @author oyzh
// * @since 2025-03-15
// */
//@StageAttribute(
//        value = FXConst.FXML_PATH + "sftp/shellSftpUpload.fxml"
//)
//public class ShellSftpUploadController extends StageController {
//
//    /**
//     * 上传表
//     */
//    @FXML
//    private SftpUploadTaskTableView uploadTable;
//
//    /**
//     * 上传管理器
//     */
//    private ShellSftpUploadManager uploadManager;
//
//    @Override
//    protected void bindListeners() {
//        super.bindListeners();
//        this.uploadManager.setTaskChangedCallback(this::initUploadTable);
//    }
//
//    protected void initUploadTable() {
//        this.uploadTable.setItem(this.uploadManager.getTasks());
//    }
//
//    @Override
//    public void onWindowShown(WindowEvent event) {
//        ShellClient client = this.getProp("client");
//        this.uploadManager = client.getSftpUploadManager();
//        this.initUploadTable();
//        super.onWindowShown(event);
//    }
//
////    @FXML
////    private void cancelTask() {
////        try {
////            ShellSftpUploadTask task = this.uploadTable.getSelectedItem();
////            if (task != null) {
////                if (task.isFinished()) {
////                    if (MessageBox.confirm(ShellI18nHelper.fileTip13())) {
////                        this.uploadManager.remove(task);
////                        this.uploadTable.removeItem(task);
////                    }
////                    return;
////                }
////                if (MessageBox.confirm(ShellI18nHelper.fileTip11())) {
////                    this.uploadManager.cancel(task);
////                }
////            }
////        } catch (Exception ex) {
////            ex.printStackTrace();
////            MessageBox.exception(ex);
////        }
////    }
////
////    @FXML
////    private void removeTask() {
////        try {
////            ShellSftpUploadTask task = this.uploadTable.getSelectedItem();
////            if (task != null && MessageBox.confirm(ShellI18nHelper.fileTip12())) {
////                this.uploadManager.remove(task);
////                this.uploadTable.removeItem(task);
////            }
////        } catch (Exception ex) {
////            ex.printStackTrace();
////            MessageBox.exception(ex);
////        }
////    }
//
//    @Override
//    public String getViewTitle() {
//        return I18nHelper.uploadManage();
//    }
//}
