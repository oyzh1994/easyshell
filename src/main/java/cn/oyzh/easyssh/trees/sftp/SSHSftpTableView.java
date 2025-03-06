package cn.oyzh.easyssh.trees.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.util.SSHI18nHelper;
import cn.oyzh.easyssh.sftp.SftpUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.util.ClipboardUtil;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SSHSftpTableView extends FXTableView<SftpFile> {

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
        new TableViewMouseSelectHelper(this);
    }

    private String filterText;

    public void setFilterText(String filterText) throws JSchException, SftpException, IOException {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshFile();
        }
    }

    private boolean showHiddenFile = true;

    public void setShowHiddenFile(boolean showHiddenFile) throws JSchException, SftpException, IOException {
        if (showHiddenFile != this.showHiddenFile) {
            this.showHiddenFile = showHiddenFile;
            this.refreshFile();
        }
    }

    @Setter
    @Getter
    private SSHClient client;

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
        this.currPathProperty.set(currPath);
    }

    public SSHSftp sftp() {
        return this.client.getSftp();
    }

    private List<SftpFile> files;

    public void loadFile() throws JSchException, SftpException, IOException {
        String currPath = this.getCurrPath();
        if (currPath == null) {
            this.setCurrPath(this.sftp().pwd());
            currPath = this.getCurrPath();
        }
        JulLog.info("current path: {}", currPath);
        this.files = this.sftp().ls(currPath, this.client);
        this.setItem(this.doFilter(this.files));
    }

    public void refreshFile() throws JSchException, SftpException, IOException {
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
    }

    public void deleteFile() throws SftpException, JSchException, IOException {
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
        } else if (!MessageBox.confirm(I18nHelper.deleteFiles())) {
            return;
        }
        if (CollectionUtil.isNotEmpty(files)) {
            List<SftpFile> filesToDelete = new ArrayList<>(files.size());
            for (SftpFile file : files) {
                if (file.isHiddenFile() && !MessageBox.confirm(file.getFileName() + " " + SSHI18nHelper.fileTip1())) {
                    continue;
                }
                String path = SftpUtil.concat(this.currPath(), file.getFileName());
                if (file.isDir()) {
                    this.sftp().rmDir(path);
                } else {
                    this.sftp().rm(path);
                }
                filesToDelete.add(file);
            }
//            this.removeItem(filesToDelete);
            this.files.removeAll(filesToDelete);
            this.refreshFile();
        }
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<FXMenuItem> menuItems = new ArrayList<>();
        List<SftpFile> files = this.getSelectedItems();
        if (files.size() == 1) {
            SftpFile file = files.getFirst();
            FXMenuItem fileInfo = MenuItemHelper.fileInfo("12", () -> {
                try {
                    SSHEventUtil.showFileInfo(file);
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
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
                return;
            }
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

    public void intoDir(SftpFile file) throws JSchException, SftpException, IOException {
        if (file.isReturnDirectory()) {
            this.returnDir();
            return;
        }
        String currPath = SftpUtil.concat(this.currPath(), file.getFileName());
        this.currPath(currPath);
        this.loadFile();
    }

    public void returnDir() throws JSchException, SftpException, IOException {
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

    public void mkDir(String name) throws SftpException, JSchException, IOException {
        String filePath = SftpUtil.concat(this.getCurrPath(), name);
        this.sftp().mkdir(filePath);
        SftpATTRS attrs = this.sftp().stat(filePath);
        SftpFile file = new SftpFile(name, attrs);
//        this.addItem(file);
        this.files.add(file);
        this.refreshFile();
//        this.loadFile();
    }

    public void touchFile(String name) throws SftpException, JSchException, IOException {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        String filePath = SftpUtil.concat(this.getCurrPath(), name);
        this.sftp().touch(filePath);
        SftpATTRS attrs = this.sftp().stat(filePath);
        SftpFile file = new SftpFile(name, attrs);
//        this.addItem(file);
        this.files.add(file);
        this.refreshFile();
//        this.loadFile();
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
//        this.loadFile();
    }

    public void uploadFile(List<File> files) throws SftpException, JSchException, IOException {
        for (File file : files) {
            this.sftp().upload(file, this.getCurrPath());
//            SftpATTRS attrs = this.sftp().stat(file.getName());
//            this.files.add(new SftpFile(file.getName(), attrs));
//            this.refreshFile();
        }
    }
}
