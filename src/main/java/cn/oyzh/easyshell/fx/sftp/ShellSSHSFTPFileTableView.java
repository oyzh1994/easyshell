package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-05-26
 */
public class ShellSSHSFTPFileTableView extends ShellSFTPFileTableView {

    /**
     * 已复制的文件
     */
    private List<ShellSFTPFile> copiedFiles;

    /**
     * ssh客户端
     */
    private ShellSSHClient sshClient;

    public void setSSHClient(ShellSSHClient sshClient) {
        this.sshClient = sshClient;
        this.setClient(sshClient.sftpClient());
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        // 获取选中的文件
        List<ShellSFTPFile> files = this.getFilterSelectedItems();
        List<MenuItem> menuItems = new ArrayList<>();
        // 复制文件
        FXMenuItem copyFile = MenuItemHelper.copyFile("12", () -> this.copyFile(files));
        copyFile.setDisable(files.isEmpty());
        menuItems.add(copyFile);
        // 复制文件
        FXMenuItem pasteFile = MenuItemHelper.pasteFile("12", this::pasteFile);
        // 判断是否禁用
        if (CollectionUtil.isNotEmpty(this.copiedFiles)) {
            ShellFile f = this.copiedFiles.getFirst();
            pasteFile.setDisable(StringUtil.equals(f.getParentPath(), this.getLocation()));
        } else {
            pasteFile.setDisable(true);
        }
        menuItems.add(pasteFile);
        menuItems.add(MenuItemHelper.separator());
        // 添加父级菜单
        menuItems.addAll(super.getMenuItems());
        return menuItems;
    }

    /**
     * 复制文件
     *
     * @param files 文件列表
     */
    protected void copyFile(List<ShellSFTPFile> files) {
        this.copiedFiles = files;
    }

    /**
     * 粘贴文件
     */
    protected void pasteFile() {
        if (CollectionUtil.isEmpty(this.copiedFiles)) {
            return;
        }
        ShellFile f = this.copiedFiles.getFirst();
        if (StringUtil.equals(f.getParentPath(), this.getLocation())) {
            return;
        }
        StageManager.showMask(() -> {
            try {
                for (ShellSFTPFile f1 : copiedFiles) {
                    String dst = ShellFileUtil.concat(this.getLocation(), f1.getFileName());
                    // 判断文件是否存在
                    if (this.client.exist(dst) && !MessageBox.confirm("[" + dst + "] " + ShellI18nHelper.fileTip4())) {
                        continue;
                    }
                    this.sshClient.serverExec().move(f1.getFilePath(), dst);
                    super.onFileAdded(dst);
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            } finally {
                this.copiedFiles = null;
            }
        });
    }

}
