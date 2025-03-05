package cn.oyzh.easyssh.trees.sftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.easyssh.ssh.SftpFile;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    }

    @Setter
    @Getter
    private SSHClient client;

    @Setter
    @Getter
    private String currPath = "/";

    public SSHSftp sftp() {
        return this.client.getSftp();
    }

    public void loadFile() throws JSchException, SftpException, IOException {
        List<SftpFile> files = this.sftp().ls(this.currPath, this.client);
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
}
