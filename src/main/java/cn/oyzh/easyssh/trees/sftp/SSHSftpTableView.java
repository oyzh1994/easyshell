package cn.oyzh.easyssh.trees.sftp;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.easyssh.ssh.SftpFile;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

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

    {
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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

    public void loadFile() throws JSchException, SftpException, IOException {
        String currPath = this.getCurrPath();
        if (currPath == null) {
            this.setCurrPath(this.sftp().pwd());
            currPath = this.getCurrPath();
        }
        JulLog.info("current path: {}", currPath);
        List<SftpFile> files = this.sftp().ls(currPath, this.client);
        if (CollectionUtil.isNotEmpty(files)) {
            if (this.currentIsRootDirectory()) {
                files = files.stream()
                        .filter(f -> !f.isReturnDirectory() && !f.isCurrentFile())
                        .sorted(Comparator.comparingInt(SftpFile::getOrder))
                        .collect(Collectors.toList());
            } else {
                files = files.stream()
                        .filter(f -> !f.isCurrentFile())
                        .sorted(Comparator.comparingInt(SftpFile::getOrder))
                        .collect(Collectors.toList());
            }
        }
        this.setItem(files);
    }

    public void deleteFile() throws SftpException {
        List<SftpFile> files = this.getSelectedItems();
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
            for (SftpFile file : files) {
                this.sftp().rm(file);
            }
            this.removeItem(files);
        }
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<FXMenuItem> menuItems = new ArrayList<>();
        FXMenuItem deleteFile = MenuItemHelper.deleteFile("12", () -> {
            try {
                this.deleteFile();
            } catch (SftpException ex) {
                MessageBox.exception(ex);
            }
        });
        menuItems.add(deleteFile);
        return menuItems;
    }

    protected void onMouseClicked(MouseEvent event) {
        try {
            if (event.getClickCount() == 2) {
                List<SftpFile> files = this.getSelectedItems();
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
        String currPath = this.currPath();
        if (currPath.endsWith("/")) {
            currPath = currPath + file.getFileName();
        } else {
            currPath = currPath + file.getFilePath();
        }
        if (currPath.startsWith("//")) {
            currPath = currPath.substring(1);
        }
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
        try {
            List<SftpFile> files = this.getSelectedItems();
            if (files.isEmpty()) {
                ClipboardUtil.copy(this.getCurrPath());
            } else if (files.size() == 1) {
                ClipboardUtil.copy(this.getCurrPath() + files.getFirst().getFileName());
            } else {
                MessageBox.warn(I18nHelper.tooManyFiles());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
