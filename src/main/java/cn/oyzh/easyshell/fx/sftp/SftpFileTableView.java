package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFileEditController;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteDeleted;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteEnded;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SftpFileTableView extends SftpFileBaseTableView {

    @Override
    public void loadFile() {
        StageManager.showMask(() -> {
            try {
                super.loadFileInner();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }


    @Override
    public List<FXMenuItem> getMenuItems() {
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
        // 获取父级菜单
        List<FXMenuItem> menuItems = super.getMenuItems();
        // 下载文件
        FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
        menuItems.add(downloadFile);
        return menuItems;
    }

    @Override
    protected void onMouseClicked(MouseEvent event) {
        try {
            List<SftpFile> files = this.getSelectedItems();
            if (files == null) {
                return;
            }
            if (files.size() != 1) {
                return;
            }
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                SftpFile file = files.getFirst();
                if (file.isDir()) {
                    this.intoDir(file);
                } else {
                    this.editFile(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 编辑文件
     *
     * @param file 文件
     */
    public void editFile(SftpFile file) {
        if (!file.isFile()) {
            return;
        }
        if (file.size() > 500 * 1024) {
            return;
        }
        // 检查类型
        String extName = FileNameUtil.extName(file.getFileName());
        if (!StringUtil.equalsAnyIgnoreCase(extName, "txt", "text", "log", "yaml", "java", "xml", "json", "htm",
                "html", "xhtml", "php", "css", "c", "cpp", "rs", "js", "csv", "sql", "md", "ini", "cfg", "sh", "bat", "py", "asp",
                "aspx", "env", "tsv")) {
            return;
        }
        StageAdapter adapter = StageManager.parseStage(ShellSftpFileEditController.class);
        adapter.setProp("file", file);
        adapter.setProp("client", this.client);
        adapter.display();
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

    public void setDeleteEndedCallback(Consumer<SftpDeleteEnded> callback) {
        this.client.setDeleteEndedCallback(callback);
    }

    public void setDeleteDeletedCallback(Consumer<SftpDeleteDeleted> callback) {
        this.client.setDeleteDeletedCallback(callback);
    }
}
