package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.sftp.ShellSFTPFileConnectTableView;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.delete.ShellSFTPDeleteManager;
import cn.oyzh.easyshell.sftp.download.ShellSFTPDownloadManager;
import cn.oyzh.easyshell.sftp.download.ShellSFTPDownloadMonitor;
import cn.oyzh.easyshell.sftp.download.ShellSFTPDownloadTask;
import cn.oyzh.easyshell.sftp.upload.ShellSFTPUploadManager;
import cn.oyzh.easyshell.sftp.upload.ShellSFTPUploadMonitor;
import cn.oyzh.easyshell.sftp.upload.ShellSFTPUploadTask;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.FXProgressTextBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.util.List;

/**
 * sftp组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellSSHSFTPTabController extends SubTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * 当前位置
     */
    @FXML
    private ShellFileLocationTextField location;

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

//    /**
//     * 文件管理组件
//     */
//    @FXML
//    private SVGGlyph sftpBox;

//    /**
//     * 隐藏文件
//     */
//    @FXML
//    private FXToggleSwitch hiddenFile;

    /**
     * 刷新文件
     */
    @FXML
    private SVGGlyph refreshFile;

    /**
     * 删除文件
     */
    @FXML
    private SVGGlyph deleteFile;

    /**
     * 隐藏文件
     */
    @FXML
    private HiddenSVGPane hiddenPane;

    /**
     * 文件表格
     */
    @FXML
    private ShellSFTPFileConnectTableView fileTable;

    /**
     * 文件过滤
     */
    @FXML
    private ClearableTextField filterFile;

    /**
     * 文件删除文本
     */
    @FXML
    private FXLabel fileDelete;

    /**
     * 文件删除组件
     */
    @FXML
    private FXHBox deleteBox;

    /**
     * 上传文件按钮
     */
    @FXML
    private SVGGlyph uploadFile;

    /**
     * 上传文件夹按钮
     */
    @FXML
    private SVGGlyph uploadDir;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置储存
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 初始化标志位
     */
    private boolean initialized = false;

    /**
     * 删除管理器
     */
    private ShellSFTPDeleteManager deleteManager;

    /**
     * 上传管理器
     */
    private ShellSFTPUploadManager uploadManager;

    /**
     * 下载管理器
     */
    private ShellSFTPDownloadManager downloadManager;

    /**
     * 初始化
     */
    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.fileTable.setClient(this.sftpClient());
        this.deleteManager = this.sftpClient().getDeleteManager();
        this.uploadManager = this.sftpClient().getUploadManager();
        this.downloadManager = this.sftpClient().getDownloadManager();
        // 上传
        this.uploadManager.addMonitorFailedCallback(this, this::uploadFailed);
        this.uploadManager.addMonitorChangedCallback(this, this::uploadMonitorChanged);
        this.uploadManager.addTaskSizeChangedCallback(this, this::uploadTaskSizeChanged);
        this.uploadManager.addTaskStatusChangedCallback(this, this::uploadStatusChanged);
        // 下载
        this.downloadManager.addMonitorFailedCallback(this, this::downloadFailed);
        this.downloadManager.addMonitorChangedCallback(this, this::downloadMonitorChanged);
        this.downloadManager.addTaskSizeChangedCallback(this, this::downloadTaskSizeChanged);
        this.downloadManager.addTaskStatusChangedCallback(this, this::downloadStatusChanged);
        // 删除
        this.deleteManager.addDeleteEndedCallback(this, this::deleteEnded);
        this.deleteManager.addDeleteFailedCallback(this, this::deleteFailed);
        this.deleteManager.addDeleteDeletedCallback(this, this::deleteDeleted);
        // 显示隐藏文件
        this.hiddenFile(this.setting.isShowHiddenFile());
//        // 上传回调
//        this.fileTable.setUploadFileCallback(files -> {
//            AnimationUtil.move(new FileSVGGlyph("150"), this.fileTable, this.uploadBox);
//        });
//        // 下载回调
//        this.fileTable.setDownloadFileCallback(files -> {
//            AnimationUtil.move(new FileSVGGlyph("150"), this.fileTable, this.downloadBox);
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
                } else {
                    this.location.text(t1);
                }
            });
//            // 隐藏文件
//            this.hiddenFile.setSelected(this.setting.isShowHiddenFile());
//            this.hiddenFile.selectedChanged((observableValue, aBoolean, t1) -> {
//                try {
//                    this.fileTable.setShowHiddenFile(t1);
//                    this.setting.setShowHiddenFile(t1);
//                    this.settingStore.update(this.setting);
//                } catch (Exception ex) {
//                    MessageBox.exception(ex);
//                }
//            });
            // 文件过滤
            this.filterFile.addTextChangeListener((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setFilterText(t1);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            // 路径跳转
            this.location.setOnJumpLocation(path -> {
                this.fileTable.cd(path);
            });
            // 快捷键
            this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (KeyboardUtil.search_keyCombination.match(event)) {
                    this.filterFile.requestFocus();
                } else if (KeyboardUtil.hide_keyCombination.match(event)) {
                    this.hiddenFile();
//                } else if (KeyboardUtil.refresh_keyCombination.match(event)) {
//                    this.refreshFile();
//                } else if (KeyboardUtil.delete_keyCombination.match(event)) {
//                    this.deleteFile();
                }
            });
            // 绑定提示快捷键
            this.hiddenPane.setTipKeyCombination(KeyboardUtil.hide_keyCombination);
            this.filterFile.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            this.deleteFile.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
            this.refreshFile.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellSSHTabController parent() {
        return (ShellSSHTabController) super.parent();
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    public ShellSFTPClient sftpClient() {
        return this.client().getSftpClient();
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

    /**
     * 进入home目录
     */
    @FXML
    private void intoHome() {
        try {
            this.fileTable.intoHome();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void mkdir() {
        this.fileTable.mkdir();
    }

    @FXML
    private void touchFile() {
        this.fileTable.touch();
    }

    @FXML
    private void uploadFile() {
        this.fileTable.uploadFile();
    }

    @FXML
    private void uploadFolder() {
        this.fileTable.uploadFolder();
    }

    @EventSubscribe
    private void draggedFile(ShellFileDraggedEvent event) {
        try {
            List<File> files = event.data();
            this.fileTable.uploadFile(files);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 取消上传
     */
    @FXML
    private void cancelUpload() {
        try {
            this.uploadManager.cancel();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 取消下载
     */
    @FXML
    private void cancelDownload() {
        try {
            this.downloadManager.cancel();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 下载失败
     *
     * @param monitor   监听器
     * @param exception 异常
     */
    private void downloadFailed(ShellSFTPDownloadMonitor monitor, Throwable exception) {
        if (exception != null) {
            MessageBox.exception(exception, I18nHelper.downloadFailed() + " " + monitor.getLocalFileName());
        }
    }

    /**
     * 下载状态改变事件
     *
     * @param status 状态
     * @param task   任务
     */
    private void downloadStatusChanged(String status, ShellSFTPDownloadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.downloadManager.getTaskSize());
        builder.append(" ").append(I18nHelper.status()).append(": ").append(status);
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        this.fileDownload.text(builder.toString());
        this.downloadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 下载监听变更事件
     *
     * @param monitor 监听器
     * @param task    任务
     */
    private void downloadMonitorChanged(ShellSFTPDownloadMonitor monitor, ShellSFTPDownloadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.downloadManager.getTaskSize());
//        builder.append(" ").append(I18nHelper.count()).append(": ").append(task.size());
        builder.append(" ").append(I18nHelper.speed()).append(": ").append(task.getSpeed());
        builder.append(" ").append(I18nHelper.size()).append(": ").append(task.getFileSize());
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        builder.append(" ").append(I18nHelper.current()).append(": ").append(monitor.getRemoteFileName());
        this.fileDownload.text(builder.toString());
        this.downloadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 下载任务大小变更事件
     */
    private void downloadTaskSizeChanged() {
        if (this.downloadManager.isEmpty()) {
            this.downloadBox.disappear();
        } else {
            this.downloadBox.display();
        }
        this.updateLayout();
    }

    /**
     * 上传失败
     *
     * @param monitor   监听器
     * @param exception 异常
     */
    private void uploadFailed(ShellSFTPUploadMonitor monitor, Throwable exception) {
        if (exception != null) {
            MessageBox.exception(exception, I18nHelper.uploadFailed() + " " + monitor.getLocalFileName());
        }
    }

    /**
     * 上传状态改变事件
     *
     * @param status 状态
     * @param task   任务
     */
    private void uploadStatusChanged(String status, ShellSFTPUploadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.uploadManager.getTaskSize());
        builder.append(" ").append(I18nHelper.status()).append(": ").append(status);
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        this.fileUpload.text(builder.toString());
        this.uploadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 上传监听变更事件
     *
     * @param monitor 监听器
     * @param task    任务
     */
    private void uploadMonitorChanged(ShellSFTPUploadMonitor monitor, ShellSFTPUploadTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.uploadManager.getTaskSize());
//        builder.append(" ").append(I18nHelper.count()).append(": ").append(task.size());
        builder.append(" ").append(I18nHelper.speed()).append(": ").append(task.getSpeed());
        builder.append(" ").append(I18nHelper.size()).append(": ").append(task.getFileSize());
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        builder.append(" ").append(I18nHelper.current()).append(": ").append(monitor.getLocalFileName());
        this.fileUpload.text(builder.toString());
        this.uploadProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 上传任务大小变更事件
     */
    private void uploadTaskSizeChanged() {
        if (this.uploadManager.isEmpty()) {
            this.uploadBox.disappear();
            this.fileTable.loadFile();
        } else {
            this.uploadBox.display();
        }
        this.updateLayout();
    }

    /**
     * 删除失败
     *
     * @param file   文件
     * @param exception 异常
     */
    private void deleteFailed(ShellSFTPFile file, Throwable exception) {
        if (exception != null) {
            MessageBox.exception(exception, I18nHelper.deleteFailed() + " " + file.getFileName());
        }
    }

    /**
     * 删除结束事件
     */
    private void deleteEnded() {
        this.deleteBox.disappear();
        this.updateLayout();
    }

    /**
     * 文件已删除事件
     *
     * @param fileName 文件名
     */
    private void deleteDeleted(String fileName) {
        if (!this.deleteBox.isVisible()) {
            this.deleteBox.display();
            this.updateLayout();
        }
//        this.fileTable.fileDeleted(fileName);
        this.fileDelete.text(I18nHelper.deleteIng() + ": " + fileName);
    }

    /**
     * 更新布局
     */
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
        this.fileTable.setFlexHeight("100% - " + (30 + showNum * 30));
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

//    /**
//     * 文件保存事件
//     *
//     * @param event 事件
//     */
//    @EventSubscribe
//    private void onFileSaved(ShellFileSavedEvent event) {
//        this.fileTable.refresh();
//    }

    /**
     * 隐藏文件
     */
    @FXML
    private void hiddenFile() {
        this.hiddenFile(!this.hiddenPane.isHidden());
    }

    /**
     * 隐藏文件
     *
     * @param hidden 是否隐藏
     */
    private void hiddenFile(boolean hidden) {
        if (hidden) {
            this.hiddenPane.hidden();
            this.fileTable.setShowHiddenFile(false);
            this.hiddenPane.setTipText(I18nHelper.showHiddenFiles());
        } else {
            this.hiddenPane.show();
            this.fileTable.setShowHiddenFile(true);
            this.hiddenPane.setTipText(I18nHelper.doNotShowHiddenFiles());
        }
        this.setting.setShowHiddenFile(hidden);
        this.settingStore.update(this.setting);
    }

//    @FXML
//    public void showSftpBox() {
//        // 判断窗口是否存在
//        List<StageAdapter> list = StageManager.listStage(ShellSFTPManageController.class);
//        for (StageAdapter adapter : list) {
//            if (adapter.getProp("client") == this.client()) {
//                adapter.toFront();
//                return;
//            }
//        }
//        StageAdapter adapter = StageManager.parseStage(ShellSFTPManageController.class, null);
//        adapter.setProp("client", this.client());
//        adapter.display();
//    }
}
