package cn.oyzh.easyshell.tabs.connect;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.controller.sftp.ShellSftpManageController;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.sftp.ShellSftpFileDraggedEvent;
import cn.oyzh.easyshell.event.sftp.ShellSftpFileSavedEvent;
import cn.oyzh.easyshell.fx.sftp.SftpLocationTextField;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteDeleted;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteEnded;
import cn.oyzh.easyshell.sftp.download.SftpDownloadManager;
import cn.oyzh.easyshell.sftp.download.SftpDownloadMonitor;
import cn.oyzh.easyshell.sftp.download.SftpDownloadTask;
import cn.oyzh.easyshell.sftp.upload.SftpUploadManager;
import cn.oyzh.easyshell.sftp.upload.SftpUploadMonitor;
import cn.oyzh.easyshell.sftp.upload.SftpUploadTask;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.fx.sftp.SftpFileConnectTableView;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.FXProgressTextBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.AnimationUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.effect.ColorAdjust;

import java.io.File;
import java.util.List;

/**
 * sftp组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellSftpTabController extends SubTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * 当前位置
     */
    @FXML
    private SftpLocationTextField location;

    /**
     * 上传组件
     */
    @FXML
    private FXHBox uploadBox;

    /**
     * 上传标签
     */
    @FXML
    private FXLabel fileUpload;

    /**
     * 上传进度
     */
    @FXML
    private FXProgressTextBar uploadProgress;

    /**
     * 下载组件
     */
    @FXML
    private FXHBox downloadBox;

    /**
     * 下载标签
     */
    @FXML
    private FXLabel fileDownload;

    /**
     * 下载进度
     */
    @FXML
    private FXProgressTextBar downloadProgress;

    /**
     * 文件管理组件
     */
    @FXML
    private SVGGlyph sftpBox;

    @FXML
    private FXToggleSwitch hiddenFile;

    @FXML
    private SftpFileConnectTableView fileTable;

    @FXML
    private ClearableTextField filterFile;

    @FXML
    private FXLabel fileDelete;

    @FXML
    private FXHBox deleteBox;

    @FXML
    private SVGGlyph uploadFile;

    @FXML
    private SVGGlyph uploadDir;

    private final ShellSetting setting = ShellSettingStore.SETTING;

    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    private boolean initialized = false;

    /**
     * 上传管理器
     */
    private SftpUploadManager uploadManager;

    /**
     * 下载管理器
     */
    private SftpDownloadManager downloadManager;

    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.fileTable.setClient(this.client());
        this.uploadManager = this.client().getUploadManager();
        this.downloadManager = this.client().getDownloadManager();
        this.uploadManager.setTaskChangedCallback(this::uploadTaskSizeChanged);
        this.uploadManager.setMonitorChangedCallback(this::uploadMonitorChanged);
        this.uploadManager.setTaskStatusChangedCallback(this::uploadStatusChanged);
        this.downloadManager.setTaskChangedCallback(this::downloadTaskSizeChanged);
        this.downloadManager.setMonitorChangedCallback(this::downloadMonitorChanged);
        this.downloadManager.setTaskStatusChangedCallback(this::downloadStatusChanged);
//        this.uploadManager.setTaskChangedCallback(() -> this.initUploadTable());
//        this.downloadManager.setTaskChangedCallback(() -> this.initDownloadTable());
//        // 下载
//        this.fileTable.setUploadEndedCallback(this::updateUploadInfo);
//        this.fileTable.setUploadFailedCallback(this::updateUploadInfo);
//        this.fileTable.setUploadChangedCallback(this::updateUploadInfo);
//        this.fileTable.setUploadCanceledCallback(this::updateUploadInfo);
//        this.fileTable.setUploadInPreparationCallback(this::updateUploadInfo);

//        // 上传
//        this.fileTable.setDownloadEndedCallback(this::updateDownloadInfo);
//        this.fileTable.setDownloadFailedCallback(this::updateDownloadInfo);
//        this.fileTable.setDownloadCanceledCallback(this::updateDownloadInfo);
//        this.fileTable.setDownloadChangedCallback(this::updateDownloadInfo);
//        this.fileTable.setDownloadInPreparationCallback(this::updateDownloadInfo);

        // 删除
        this.client().getDeleteManager().setDeleteEndedCallback(this::updateDeleteInfo);
        this.client().getDeleteManager().setDeleteDeletedCallback(this::updateDeleteInfo);
        // 显示隐藏文件
        this.fileTable.setShowHiddenFile(this.setting.isShowHiddenFile());
        // 监听上传中属性
        this.client().getUploadManager().uploadingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setHue(0.5);
                colorAdjust.setContrast(0.5);
                colorAdjust.setBrightness(0.5);
                colorAdjust.setSaturation(0.5);
                this.sftpBox.setEffect(colorAdjust);
            } else {
                this.fileTable.loadFile();
                this.sftpBox.setEffect(null);
            }
        });
        // 监听删除中属性
        this.client().getDownloadManager().downloadingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setHue(0.5);
                colorAdjust.setContrast(0.5);
                colorAdjust.setBrightness(0.5);
                colorAdjust.setSaturation(0.5);
                this.sftpBox.setEffect(colorAdjust);
            } else {
                this.sftpBox.setEffect(null);
            }
        });
//        // 监听下载中属性
//        this.client().deletingProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
//                this.refreshFile.disable();
//            } else {
//                this.refreshFile.enable();
//            }
//        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client().close();
    }

    @Override
    public void onTabInit(RichTab tab) {
        try {
            super.onTabInit(tab);
            this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    System.setProperty(ShellConst.SFTP_VISIBLE, "1");
                    this.init();
                } else {
                    System.clearProperty(ShellConst.SFTP_VISIBLE);
                }
            });
            // 绑定属性
            this.uploadDir.disableProperty().bind(this.uploadFile.disableProperty());
            // 监听位置
            this.fileTable.locationProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1 == null) {
                    this.location.clear();
//                    this.copyFilePath.disable();
                } else {
                    this.location.text(t1);
//                    this.copyFilePath.enable();
                }
            });
            // 隐藏文件
            this.hiddenFile.setSelected(this.setting.isShowHiddenFile());
            this.hiddenFile.selectedChanged((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setShowHiddenFile(t1);
                    this.setting.setShowHiddenFile(t1);
                    this.settingStore.update(this.setting);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            // 文件过滤
            this.filterFile.addTextChangeListener((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setFilterText(t1);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            // 文件下载回调
            this.fileTable.setDownloadFileCallback((files) -> {
                AnimationUtil.move(new FileSVGGlyph("150"), this.fileTable, this.sftpBox);
            });
            // 文件上传回调
            this.fileTable.setUploadFileCallback((files) -> {
                AnimationUtil.move(new FileSVGGlyph("150"), this.fileTable, this.sftpBox);
            });
            // 路径跳转
            this.location.setOnJumpLocation(path -> {
                this.fileTable.cd(path);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellConnectTabController parent() {
        return (ShellConnectTabController) super.parent();
    }

    public ShellClient client() {
        return this.parent().getClient();
    }

    @FXML
    private void refreshFile() {
        try {
            this.fileTable.loadFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void deleteFile() {
        try {
            this.fileTable.deleteFile(this.fileTable.getSelectedItems());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void returnDir() {
        try {
            this.fileTable.returnDir();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

//    @FXML
//    private void copyFilePath() {
//        try {
//            this.fileTable.copyFilePath();
//            MessageBox.okToast(I18nHelper.copySuccess());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

    @FXML
    private void mkdir() {
//        try {
//            String name = MessageBox.prompt(I18nHelper.pleaseInputDirName());
//            this.fileTable.mkdir(name);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
        this.fileTable.mkdir();
    }

    @FXML
    private void touchFile() {
//        try {
//            String name = MessageBox.prompt(I18nHelper.pleaseInputFileName());
//            this.fileTable.touch(name);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
        this.fileTable.touch();
    }

    @FXML
    private void uploadFile() {
//        try {
//            List<File> files = FileChooserHelper.chooseMultiple(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
//            if (this.fileTable.uploadFile(files)) {
//                AnimationUtil.move(new FileSVGGlyph("150"), this.fileTable, this.sftpBox);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
        this.fileTable.uploadFile();
    }

    @FXML
    private void uploadFolder() {
//        try {
//            File file = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory());
//            if (this.fileTable.uploadFile(file)) {
//                AnimationUtil.move(new FolderSVGGlyph("150"), this.fileTable, this.sftpBox);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
        this.fileTable.uploadFolder();
    }

    @EventSubscribe
    private void draggedFile(ShellSftpFileDraggedEvent event) {
        try {
            List<File> files = event.data();
            this.fileTable.uploadFile(files);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void cancelUpload() {
        try {
            this.uploadManager.cancel();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void cancelDownload() {
        try {
            this.downloadManager.cancel();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    private void downloadStatusChanged(String status, SftpDownloadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.downloadManager.getTaskSize());
        builder.append(" ").append(I18nHelper.status()).append(": ").append(status);
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        this.fileDownload.text(builder.toString());
        this.downloadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    private void downloadMonitorChanged(SftpDownloadMonitor monitor, SftpDownloadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.downloadManager.getTaskSize());
        builder.append(" ").append(I18nHelper.count()).append(": ").append(task.size());
        builder.append(" ").append(I18nHelper.speed()).append(": ").append(task.getSpeed());
        builder.append(" ").append(I18nHelper.size()).append(": ").append(task.getFileSize());
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        builder.append(" ").append(I18nHelper.current()).append(": ").append(monitor.getRemoteFileName());
        this.fileDownload.text(builder.toString());
        this.downloadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    private void downloadTaskSizeChanged() {
        if (this.downloadManager.isEmpty()) {
            this.downloadBox.disappear();
        } else {
            this.downloadBox.display();
        }
        this.updateLayout();
    }

    private void uploadStatusChanged(String status, SftpUploadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.uploadManager.getTaskSize());
        builder.append(" ").append(I18nHelper.status()).append(": ").append(status);
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        this.fileUpload.text(builder.toString());
        this.uploadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    private void uploadMonitorChanged(SftpUploadMonitor monitor, SftpUploadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.uploadManager.getTaskSize());
        builder.append(" ").append(I18nHelper.count()).append(": ").append(task.size());
        builder.append(" ").append(I18nHelper.speed()).append(": ").append(task.getSpeed());
        builder.append(" ").append(I18nHelper.size()).append(": ").append(task.getFileSize());
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        builder.append(" ").append(I18nHelper.current()).append(": ").append(monitor.getLocalFileName());
        this.fileUpload.text(builder.toString());
        this.uploadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    private void uploadTaskSizeChanged() {
        if (this.uploadManager.isEmpty()) {
            this.uploadBox.disappear();
        } else {
            this.uploadBox.display();
        }
        this.updateLayout();
    }
//
//    private void updateUploadInfo(SftpUploadInPreparation inPreparation) {
//        if (!this.uploadBox.isVisible()) {
//            this.uploadBox.display();
//            this.updateLayout();
//        }
//        if (inPreparation.getFileName() != null) {
//            this.fileUpload.text(SSHI18nHelper.fileTip7() + ": " + inPreparation.getFileName());
//        } else {
//            this.fileUpload.text(SSHI18nHelper.fileTip7());
//        }
//    }
//
//    private void updateDownloadInfo(SftpDownloadEnded ended) {
//        try {
//            JulLog.info("updateDownloadInfo:{}", ended);
//            if (ended.getFileCount() == 0) {
//                this.fileDownload.clear();
//                this.downloadProgressInfo.clear();
//                this.downloadBox.disappear();
//                this.updateLayout();
//                // 提示消息
//                if (!StageManager.hasFocusedWindow()) {
//                    TrayManager.displayInfoMessage(I18nHelper.tips(), I18nHelper.fileDownloadFinished());
//                }
//            }
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    private void updateDownloadInfo(SftpDownloadFailed failed) {
//        try {
//            JulLog.info("updateDownloadInfo:{}", failed);
//            if (failed.getFileCount() == 0) {
//                this.fileDownload.clear();
//                this.downloadProgressInfo.clear();
//                this.downloadBox.disappear();
//                this.updateLayout();
//                // 提示消息
//                if (!StageManager.hasFocusedWindow()) {
//                    TrayManager.displayInfoMessage(I18nHelper.tips(), I18nHelper.fileDownloadFailed());
//                }
//            }
//            Exception exception = failed.getException();
//            // 如果异常里面包含文件不存在等信息，则尝试刷新文件列表
//            if (exception != null && StringUtil.containsAny(exception.getMessage(), "No such file")) {
//                this.fileTable.loadFile();
//            }
//            MessageBox.warn(failed.getRemoteFile() + " " + I18nHelper.downloadFailed());
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    private void updateDownloadInfo(SftpDownloadCanceled canceled) {
//        try {
//            JulLog.info("updateDownloadInfo:{}", canceled);
//            this.fileDownload.clear();
//            this.downloadProgressInfo.clear();
//            this.downloadBox.disappear();
//            this.updateLayout();
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    private void updateDownloadInfo(SftpDownloadChanged changed) {
//        if (!this.downloadBox.isVisible()) {
//            this.downloadBox.display();
//            this.updateLayout();
//        }
//        if (changed.getFileCount() > 1) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("Count: ").append(changed.getFileCount());
//            builder.append(" File: ").append(changed.getLocalFileName());
//            builder.append(" Remote: ").append(changed.getRemoteFile());
//            this.fileDownload.text(builder.toString());
//
//            StringBuilder progress = new StringBuilder();
//            progress.append(NumberUtil.formatSize(changed.getCurrent(), 2))
//                    .append("/")
//                    .append(NumberUtil.formatSize(changed.getTotal(), 2))
//                    .append("/")
//                    .append(NumberUtil.formatSize(changed.getFileSize(), 2));
//            this.downloadProgressInfo.text(progress.toString());
//        } else {
//            StringBuilder builder = new StringBuilder();
//            builder.append("File: ").append(changed.getLocalFileName());
//            builder.append(" Remote: ").append(changed.getRemoteFile());
//            this.fileDownload.text(builder.toString());
//
//            StringBuilder progress = new StringBuilder();
//            progress.append(NumberUtil.formatSize(changed.getCurrent(), 2))
//                    .append("/")
//                    .append(NumberUtil.formatSize(changed.getTotal(), 2));
//            this.downloadProgressInfo.text(progress.toString());
//        }
//        this.downloadProgress.progress(changed.progress());
//    }
//
//    private void updateDownloadInfo(SftpDownloadInPreparation inPreparation) {
//        if (!this.downloadBox.isVisible()) {
//            this.downloadBox.display();
//            this.updateLayout();
//        }
//        if (inPreparation.getFileName() != null) {
//            this.fileDownload.text(SSHI18nHelper.fileTip8() + ": " + inPreparation.getFileName());
//        } else {
//            this.fileDownload.text(SSHI18nHelper.fileTip8());
//        }
//    }

    private void updateDeleteInfo(SftpDeleteEnded ended) {
        this.deleteBox.disappear();
        this.updateLayout();
    }

    private void updateDeleteInfo(SftpDeleteDeleted deleted) {
        if (!this.deleteBox.isVisible()) {
            this.deleteBox.display();
            this.updateLayout();
        }
        this.fileTable.fileDeleted(deleted.getRemoteFile());
        this.fileDelete.text(I18nHelper.fileDeleteIng() + ": " + deleted.getRemoteFile());
    }

    private synchronized void updateLayout() {
        int showNum = 0;
        if (this.deleteBox.isVisible()) {
            ++showNum;
        }
        if (this.uploadBox.isVisible()) {
            ++showNum;
        }
        if (this.downloadBox.isVisible()) {
            ++showNum;
        }
        this.fileTable.setFlexHeight("100% - " + (60 + showNum * 30));
        this.fileTable.parentAutosize();
    }

//    @FXML
//    private void showUploadBox() {
//        StageAdapter adapter = StageManager.parseStage(ShellSftpUploadController.class, null);
//        adapter.setProp("client", this.client());
//        adapter.display();
//    }
//
//    @FXML
//    private void showDownloadBox() {
//        StageAdapter adapter = StageManager.parseStage(ShellSftpDownloadController.class, null);
//        adapter.setProp("client", this.client());
//        adapter.display();
//    }

    @EventSubscribe
    private void onFileSaved(ShellSftpFileSavedEvent event) {
        this.fileTable.refresh();
    }

    @FXML
    public void showSftpBox() {
        // 判断窗口是否存在
        List<StageAdapter> list = StageManager.listStage(ShellSftpManageController.class);
        for (StageAdapter adapter : list) {
            if (adapter.getProp("client") == this.client()) {
                adapter.toFront();
                return;
            }
        }
        StageAdapter adapter = StageManager.parseStage(ShellSftpManageController.class, null);
        adapter.setProp("client", this.client());
        adapter.display();
    }
}
