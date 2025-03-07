package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.sftp.download.SftpDownloadCanceled;
import cn.oyzh.easyssh.sftp.download.SftpDownloadChanged;
import cn.oyzh.easyssh.sftp.download.SftpDownloadEnded;
import cn.oyzh.easyssh.sftp.upload.SftpUploadCanceled;
import cn.oyzh.easyssh.sftp.upload.SftpUploadChanged;
import cn.oyzh.easyssh.sftp.upload.SftpUploadEnded;
import cn.oyzh.easyssh.store.SSHSettingStore;
import cn.oyzh.easyssh.trees.sftp.SSHSftpTableView;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.file.FXChooser;
import cn.oyzh.fx.plus.file.FileChooserHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.File;
import java.util.List;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHSftpTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    @FXML
    private FXLabel filePath;

    @FXML
    private FXLabel fileUpload;

    @FXML
    private FXLabel uploadProgressInfo;

    @FXML
    private FXProgressBar uploadProgress;

    @FXML
    private FXLabel fileDownload;

    @FXML
    private FXLabel downloadProgressInfo;

    @FXML
    private FXProgressBar downloadProgress;

    @FXML
    private SVGGlyph copyFilePath;

    @FXML
    private FXToggleSwitch hiddenFile;

    @FXML
    private SSHSftpTableView fileTable;


    @FXML
    private ClearableTextField filterFile;

    @FXML
    private FXHBox downloadBox;

    @FXML
    private FXHBox uploadBox;

    private final SSHSetting setting = SSHSettingStore.SETTING;

    private final SSHSettingStore settingStore = SSHSettingStore.INSTANCE;

    private boolean initialized = false;

    private void init() {
        if (this.initialized) {
            return;
        }
        try {
            this.initialized = true;
            this.fileTable.setClient(this.client());
            this.fileTable.setShowHiddenFile(this.setting.isShowHiddenFile());
            this.fileTable.setUploadEndedCallback(this::updateUploadInfo);
            this.fileTable.setUploadChangedCallback(this::updateUploadInfo);
            this.fileTable.setUploadCanceledCallback(this::updateUploadInfo);
            this.fileTable.setDownloadEndedCallback(this::updateDownloadInfo);
            this.fileTable.setDownloadCanceledCallback(this::updateDownloadInfo);
            this.fileTable.setDownloadChangedCallback(this::updateDownloadInfo);
            this.fileTable.loadFile();
        } catch (Exception ex) {
            this.initialized = false;
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
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
            this.hiddenFile.setSelected(this.setting.isShowHiddenFile());
            this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    this.init();
                }
            });
            this.fileTable.currPathProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1 == null) {
                    this.filePath.clear();
                    this.copyFilePath.disable();
                } else {
                    this.filePath.setText(t1);
                    this.copyFilePath.enable();
                }
            });
            this.hiddenFile.selectedChanged((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setShowHiddenFile(t1);
                    this.setting.setShowHiddenFile(t1);
                    this.settingStore.update(this.setting);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            this.filterFile.addTextChangeListener((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setFilterText(t1);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public SSHConnectTabController parent() {
        return (SSHConnectTabController) super.parent();
    }

    public SSHClient client() {
        return this.parent().client();
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
            this.fileTable.deleteFile();
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

    @FXML
    private void copyFilePath() {
        try {
            this.fileTable.copyFilePath();
            MessageBox.okToast(I18nHelper.copySuccess());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void mkDir() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputDirName());
            this.fileTable.mkDir(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void touchFile() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputFileName());
            this.fileTable.touchFile(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void uploadFile() {
        try {
            List<File> files = FileChooserHelper.chooseMultiple(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
            for (File file : files) {
                if (file.isDirectory()) {
                    MessageBox.warn(I18nHelper.directory() + " [" + file.getName() + "] " + I18nHelper.notSupport());
                    return;
                }
            }
            if (this.fileTable.uploadFile(files)) {
                this.uploadBox.display();
                this.updateLayout();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void cancelUpload() {
        try {
            this.fileTable.cancelUpload();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void cancelDownload() {
        try {
            this.fileTable.cancelDownload();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    private void updateUploadInfo(SftpUploadEnded ended) {
        try {
            JulLog.info("updateUploadInfo:{}", ended);
            this.fileTable.fileUploaded(ended.getFileName(), ended.getDest());
            if (ended.getFileCount() == 0) {
                this.fileUpload.clear();
                this.uploadBox.disappear();
                this.updateLayout();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    private void updateUploadInfo(SftpUploadCanceled canceled) {
        try {
            JulLog.info("updateUploadInfo:{}", canceled);
            this.fileUpload.clear();
            this.uploadBox.disappear();
            this.updateLayout();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    private void updateUploadInfo(SftpUploadChanged changed) {
        if (changed.getFileCount() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("File Count: ").append(changed.getFileCount());
            builder.append(" Total Size: ").append(NumberUtil.formatSize(changed.getFileSize(), 2));
            builder.append(" Current File: ").append(changed.getFileName());
            builder.append(" Dest: ").append(changed.getDest());
            this.fileUpload.text(builder.toString());
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("File: ").append(changed.getFileName());
            builder.append(" Dest: ").append(changed.getDest());
            this.fileUpload.text(builder.toString());
        }
        StringBuilder progress = new StringBuilder();
        progress.append(NumberUtil.formatSize(changed.getCurrent(), 2))
                .append("/")
                .append(NumberUtil.formatSize(changed.getTotal(), 2));
        this.uploadProgressInfo.text(progress.toString());
        this.uploadProgress.progress(changed.progress());
    }

    private void updateDownloadInfo(SftpDownloadEnded ended) {
        try {
            JulLog.info("updateDownloadInfo:{}", ended);
            if (ended.getFileCount() == 0) {
                this.fileDownload.clear();
                this.downloadBox.disappear();
                this.updateLayout();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    private void updateDownloadInfo(SftpDownloadCanceled canceled) {
        try {
            JulLog.info("updateDownloadInfo:{}", canceled);
            this.fileDownload.clear();
            this.downloadBox.disappear();
            this.updateLayout();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    private void updateDownloadInfo(SftpDownloadChanged changed) {
        if(!this.downloadBox.isVisible()){
            this.downloadBox.display();
            this.updateLayout();
        }
        if (changed.getFileCount() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("File Count: ").append(changed.getFileCount());
            builder.append(" Total Size: ").append(NumberUtil.formatSize(changed.getFileSize(), 2));
            builder.append(" Current File: ").append(changed.getFileName());
            builder.append(" Remote: ").append(changed.getRemote());
            this.fileDownload.text(builder.toString());
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("File: ").append(changed.getFileName());
            builder.append(" Remote: ").append(changed.getRemote());
            this.fileDownload.text(builder.toString());
        }
        StringBuilder progress = new StringBuilder();
        progress.append(NumberUtil.formatSize(changed.getCurrent(), 2))
                .append("/")
                .append(NumberUtil.formatSize(changed.getTotal(), 2));
        this.downloadProgressInfo.text(progress.toString());
        this.downloadProgress.progress(changed.progress());
    }

    private void updateLayout() {
        if (this.uploadBox.isVisible() && this.downloadBox.isVisible()) {
            this.fileTable.setFlexHeight("100% - 120");
        } else if (this.uploadBox.isVisible() || this.downloadBox.isVisible()) {
            this.fileTable.setFlexHeight("100% - 90");
        } else {
            this.fileTable.setFlexHeight("100% - 60");
        }
        this.fileTable.parentAutosize();
    }
}
