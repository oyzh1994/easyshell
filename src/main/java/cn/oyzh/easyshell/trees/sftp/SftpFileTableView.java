package cn.oyzh.easyshell.trees.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteDeleted;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteEnded;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SftpFileTableView extends FXTableView<SftpFile> {

    @Override
    protected void initTableView() {
        super.initTableView();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<SftpFile> files = this.getSelectedItems();
            if (CollectionUtil.isNotEmpty(files)) {
                this.showContextMenu(this.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);

        // 初始化鼠标多选辅助类
        TableViewMouseSelectHelper.install(this);
    }

    private String filterText;

    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshFile();
        }
    }

    private boolean showHiddenFile = true;

    public void setShowHiddenFile(boolean showHiddenFile) {
        if (showHiddenFile != this.showHiddenFile) {
            this.showHiddenFile = showHiddenFile;
            this.refreshFile();
        }
    }

    @Setter
    @Getter
    private ShellClient client;

    /**
     * 当前路径
     */
    private final StringProperty currPathProperty = new SimpleStringProperty();

    public String getCurrPath() {
        return this.currPathProperty.get();
    }

    public StringProperty currPathProperty() {
        return this.currPathProperty;
    }

    protected void setCurrPath(String currPath) {
        this.currPathProperty.set(currPath);
    }

    protected String currPath() {
        return this.currPathProperty.get();
    }

    protected void currPath(String currPath) {
        if (StringUtil.notEquals(this.currPath(), currPath)) {
            this.clearItems();
        }
        this.currPathProperty.set(currPath);
    }

    public ShellSftp sftp() {
        return this.client.openSftp();
    }

    private List<SftpFile> files;

    public void loadFile() {
        StageManager.showMask(() -> {
            try {
                this._loadFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public synchronized void _loadFile() throws SftpException {
        ShellSftp sftp = this.sftp();
        try {
            String currPath = this.getCurrPath();
            if (currPath == null) {
                this.setCurrPath(sftp.pwd());
                currPath = this.getCurrPath();
            }
            JulLog.info("current path: {}", currPath);
//            List<SftpFile> oldFiles = this.files;
            // 更新当前列表
            this.files = sftp.lsFile(currPath, this.client);
            // 过滤出来待显示的列表
            List<SftpFile> files = this.doFilter(this.files);
            // 当前在显示的列表
            List<SftpFile> items = this.getItems();

            // 删除列表
            List<SftpFile> delList = new ArrayList<>();
            // 新增列表
            List<SftpFile> addList = new ArrayList<>();

            // 遍历已有集合，如果不在待显示列表，则删除，否则更新
            for (SftpFile file : items) {
                Optional<SftpFile> optional = files.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
                if (optional.isEmpty()) {
                    delList.add(file);
                } else {
                    file.copy(optional.get());
                }
            }

            // 遍历待显示列表，如果不在已显示列表，则新增
            for (SftpFile file : files) {
                Optional<SftpFile> optional = items.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
                if (optional.isEmpty()) {
                    addList.add(file);
                }
            }

            // 删除数据
            this.removeItem(delList);
            // 新增数据
            this.addItem(addList);
//            this.setItem(this.doFilter(this.files));
//            List<SftpFile> files = this.doFilter(this.files);
//            this.setItem(files);
//            if (CollectionUtil.isNotEmpty(files) && CollectionUtil.isNotEmpty(oldFiles)) {
//                for (SftpFile file : files) {
//                    oldFiles.parallelStream()
//                            .filter(f -> f.getIcon().isWaiting() && StringUtil.equals(f.getFilePath(), file.getFilePath()))
//                            .findAny()
//                            .ifPresent(f -> file.startWaiting());
//                }
//            }
        } catch (SftpException ex) {
            if (ExceptionUtil.hasMessage(ex, "inputstream is closed")) {
                sftp.close();
                this._loadFile();
            } else {
                throw ex;
            }
        }
    }

    public void refreshFile() {
        if (this.files == null) {
            this.loadFile();
        } else {
            this.setItem(this.doFilter(this.files));
        }
    }

    private List<SftpFile> doFilter(List<SftpFile> files) {
        if (CollectionUtil.isNotEmpty(files)) {
            return files.stream()
                    .filter(f -> {
                        if (f.isCurrentFile()) {
                            return false;
                        }
                        if (this.currentIsRootDirectory() && f.isReturnDirectory()) {
                            return false;
                        }
                        if (!this.showHiddenFile && f.isHiddenFile()) {
                            return false;
                        }
                        if (StringUtil.isNotEmpty(this.filterText) && !StringUtil.containsIgnoreCase(f.getFileName(), this.filterText)) {
                            return false;
                        }
                        return true;
                    })
                    .sorted(Comparator.comparingInt(SftpFile::getOrder))
                    .collect(Collectors.toList());
        }
        return files;
//        return new CopyOnWriteArrayList<>(files);
    }

    public void deleteFile() {
        List<SftpFile> files = new ArrayList<>(this.getSelectedItems());
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (files.size() == 1) {
            SftpFile file = files.getFirst();
            if (file.isDir() && !MessageBox.confirm(I18nHelper.deleteDir() + " " + file.getFileName())) {
                return;
            }
            if (!file.isDir() && !MessageBox.confirm(I18nHelper.deleteFile() + " " + file.getFileName())) {
                return;
            }
        } else if (!MessageBox.confirm(ShellI18nHelper.fileTip2())) {
            return;
        }
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
//                List<SftpFile> filesToDelete = new ArrayList<>(files.size());
                for (SftpFile file : files) {
                    if (file.isHiddenFile() && !MessageBox.confirm(file.getFileName() + " " + ShellI18nHelper.fileTip1())) {
                        continue;
                    }
                    file.startWaiting();
//                    String path = SftpUtil.concat(this.currPath(), file.getFileName());
                    this.client.delete(file);
//                    if (file.isDir()) {
//                        this.sftp().rmdirRecursive(path);
//                    } else {
//                        this.sftp().rm(path);
//                    }
//                    filesToDelete.add(file);
                }
//                this.files.removeAll(filesToDelete);
//                // 删除文件结束
//                this.client.getSftpDeleteManager().deleteEnded();
                this.refreshFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<SftpFile> files = this.getSelectedItems();
        if (CollectionUtil.isEmpty(files)) {
            return Collections.emptyList();
        }
        // 发现操作中的文件，则跳过
        for (SftpFile file : files) {
            if (file.isWaiting()) {
                return Collections.emptyList();
            }
        }
        List<FXMenuItem> menuItems = new ArrayList<>();

        FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> {
            try {
                this.downloadFile(files);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
//        if (this.client.isDownloading()) {
//            downloadFile.disable();
//        }
        menuItems.add(downloadFile);

        if (files.size() == 1) {
            SftpFile file = files.getFirst();
            FXMenuItem fileInfo = MenuItemHelper.fileInfo("12", () -> {
                try {
                    ShellEventUtil.showFileInfo(file);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            menuItems.add(fileInfo);

            FXMenuItem copyFilePath = MenuItemHelper.copyFilePath("12", () -> {
                try {
                    ClipboardUtil.copy(SftpUtil.concat(this.getCurrPath(), file.getFileName()));
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            menuItems.add(copyFilePath);

            if (file.isFile()) {
                FXMenuItem renameFile = MenuItemHelper.renameFile("12", () -> {
                    try {
                        String newName = MessageBox.prompt(I18nHelper.pleaseInputContent(), file.getFileName());
                        this.renameFile(file, newName);
                    } catch (Exception ex) {
                        MessageBox.exception(ex);
                    }
                });
                menuItems.add(renameFile);
            }
        }

        FXMenuItem deleteFile = MenuItemHelper.deleteFile("12", () -> {
            try {
                this.deleteFile();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
        menuItems.add(deleteFile);
        return menuItems;
    }

    protected void onMouseClicked(MouseEvent event) {
        try {
            List<SftpFile> files = this.getSelectedItems();
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (files == null) {
                    return;
                }
                if (files.size() != 1) {
                    return;
                }
                SftpFile file = files.getFirst();
                if (!file.isDir()) {
                    return;
                }
                this.intoDir(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public boolean currentIsRootDirectory() {
        return "/".equals(this.currPath());
    }

    public void intoDir(SftpFile file) throws SftpException {
        if (file.isReturnDirectory()) {
            this.returnDir();
            return;
        }
        String currPath = SftpUtil.concat(this.currPath(), file.getFileName());
        this.currPath(currPath);
        this.loadFile();
    }

    public void returnDir() {
        if (this.currentIsRootDirectory()) {
            return;
        }
        String currPath = this.currPath();
        if (currPath.endsWith("/")) {
            currPath = currPath.substring(0, currPath.length() - 1);
        }
        currPath = currPath.substring(0, currPath.lastIndexOf("/") + 1);
        this.currPath(currPath);
        this.loadFile();
    }

    public void copyFilePath() {
        List<SftpFile> files = this.getSelectedItems();
        if (files.isEmpty()) {
            ClipboardUtil.copy(this.getCurrPath());
        } else if (files.size() == 1) {
            ClipboardUtil.copy(SftpUtil.concat(this.getCurrPath(), files.getFirst().getFileName()));
        } else {
            MessageBox.warn(I18nHelper.tooManyFiles());
        }
    }

    public void mkDir(String name) throws SftpException {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip5())) {
            return;
        }
        String filePath = SftpUtil.concat(this.getCurrPath(), name);
        this.sftp().mkdir(filePath);
        SftpATTRS attrs = this.sftp().stat(filePath);
        SftpFile file = new SftpFile(this.currPath(), name, attrs);
        file.setOwner(SftpUtil.getOwner(file.getUid(), this.client));
        file.setGroup(SftpUtil.getGroup(file.getGid(), this.client));
        this.files.add(file);
        this.refreshFile();
    }

    public void touchFile(String name) throws SftpException {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip4())) {
            return;
        }
        String filePath = SftpUtil.concat(this.getCurrPath(), name);
        this.sftp().touch(filePath);
        SftpATTRS attrs = this.sftp().stat(filePath);
        SftpFile file = new SftpFile(this.currPath(), name, attrs);
        file.setOwner(SftpUtil.getOwner(file.getUid(), this.client));
        file.setGroup(SftpUtil.getGroup(file.getGid(), this.client));
        this.files.add(file);
        this.refreshFile();
    }

    public void renameFile(SftpFile file, String newName) throws SftpException, JSchException, IOException {
        String name = file.getFileName();
        if (newName == null || StringUtil.equals(name, newName)) {
            return;
        }
        String filePath = SftpUtil.concat(this.getCurrPath(), name);
        String newPath = SftpUtil.concat(this.getCurrPath(), newName);
        this.sftp().rename(filePath, newPath);
        file.setFileName(newName);
        this.refreshFile();
    }

    public boolean downloadFile(List<SftpFile> files) {
        File dir = DirChooserHelper.chooseDownload(I18nHelper.pleaseSelectDirectory());
        if (dir != null && dir.isDirectory() && dir.exists()) {
            String[] fileArr = dir.list();
            // 检查文件是否存在
            if (ArrayUtil.isNotEmpty(fileArr)) {
                for (String f1 : fileArr) {
                    Optional<SftpFile> file = files.parallelStream().filter(f -> StringUtil.equalsIgnoreCase(f.getFileName(), f1)).findAny();
                    if (file.isPresent()) {
                        if (!MessageBox.confirm(ShellI18nHelper.fileTip6())) {
                            return false;
                        }
                        break;
                    }
                }
            }
            for (SftpFile file : files) {
                try {
                    file.setParentPath(this.currPath());
                    this.client.download(dir, file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            }
            MessageBox.okToast(ShellI18nHelper.fileTip16());
            return true;
        }
        return false;
    }

    public boolean uploadFile(File file) {
        if (file != null && file.exists()) {
            return this.uploadFile(Collections.singletonList(file));
        }
        return false;
    }

    public boolean uploadFile(List<File> files) {
        if (CollectionUtil.isEmpty(files)) {
            return false;
        }
//        if (this.client.isUploading()) {
//            MessageBox.info(SSHI18nHelper.fileTip9());
//            return false;
//        }
        // 检查要上传的文件是否存在
        for (File file : files) {
            if (this.existFile(file.getName())) {
                if (!MessageBox.confirm(ShellI18nHelper.fileTip3())) {
                    return false;
                }
                break;
            }
        }
        for (File file : files) {
            try {
                this.client.upload(file, this.getCurrPath());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        }
        MessageBox.okToast(ShellI18nHelper.fileTip15());
        return true;
    }

//    public void fileUploaded(String fileName, String dest) throws SftpException {
//        String currPath = this.getCurrPath();
//        if (StringUtil.equalsAny(currPath, dest)) {
//            Optional<SftpFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
//            String filePath = SftpUtil.concat(dest, fileName);
//            if (sftpFile.isPresent()) {
//                SftpATTRS attrs = this.sftp().stat(filePath);
//                sftpFile.get().setAttrs(attrs);
//            } else {
//                SftpATTRS attrs = this.sftp().stat(filePath);
//                SftpFile file = new SftpFile(currPath, fileName, attrs);
//                file.setOwner(SftpUtil.getOwner(file.getUid(), this.client));
//                file.setGroup(SftpUtil.getGroup(file.getGid(), this.client));
//                this.files.add(file);
//            }
//            this.refreshFile();
//        }
//    }

//    public void setUploadEndedCallback(Consumer<SftpUploadEnded> callback) {
//        this.client.setUploadEndedCallback(callback);
//    }
//
//    public void setUploadFailedCallback(Consumer<SftpUploadFailed> callback) {
//        this.client.setUploadFailedCallback(callback);
//    }
//
//    public void setUploadCanceledCallback(Consumer<SftpUploadCanceled> callback) {
//        this.client.setUploadCanceledCallback(callback);
//    }
//
//    public void setUploadChangedCallback(Consumer<SftpUploadChanged> callback) {
//        this.client.setUploadChangedCallback(callback);
//    }
//
//    public void setUploadInPreparationCallback(Consumer<SftpUploadInPreparation> callback) {
//        this.client.setUploadInPreparationCallback(callback);
//    }
//
//    public void cancelUpload() {
//        this.client.cancelUpload();
//    }
//
//    public void setDownloadEndedCallback(Consumer<SftpDownloadEnded> callback) {
//        this.client.setDownloadEndedCallback(callback);
//    }
//
//    public void setDownloadFailedCallback(Consumer<SftpDownloadFailed> callback) {
//        this.client.setDownloadFailedCallback(callback);
//    }
//
//    public void setDownloadCanceledCallback(Consumer<SftpDownloadCanceled> callback) {
//        this.client.setDownloadCanceledCallback(callback);
//    }
//
//    public void setDownloadChangedCallback(Consumer<SftpDownloadChanged> callback) {
//        this.client.setDownloadChangedCallback(callback);
//    }
//
//    public void setDownloadInPreparationCallback(Consumer<SftpDownloadInPreparation> callback) {
//        this.client.setDownloadInPreparationCallback(callback);
//    }
//
//    public void cancelDownload() {
//        this.client.cancelDownload();
//    }

    public boolean existFile(String fileName) {
        Optional<SftpFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
        return sftpFile.isPresent();
    }

    public void setDeleteEndedCallback(Consumer<SftpDeleteEnded> callback) {
        this.client.setDeleteEndedCallback(callback);
    }

    public void setDeleteDeletedCallback(Consumer<SftpDeleteDeleted> callback) {
        this.client.setDeleteDeletedCallback(callback);
    }

    public void fileDeleted(String remoteFile) {
        Optional<SftpFile> optional = this.files.parallelStream()
                .filter(f -> StringUtil.equals(remoteFile, f.getFilePath()))
                .findAny();
        if (optional.isPresent()) {
            this.files.remove(optional.get());
            this.refreshFile();
        }
    }
}
