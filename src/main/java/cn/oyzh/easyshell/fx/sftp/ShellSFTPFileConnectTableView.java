package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPFileConnectTableView extends ShellSFTPFileBaseTableView {

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
    public List<? extends MenuItem> getMenuItems() {
        List<ShellSftpFile> files = this.getSelectedItems();
//        if (CollectionUtil.isEmpty(files)) {
//            // 菜单列表
//            List<MenuItem> menuItems = new ArrayList<>();
////            // 创建文件
////            FXMenuItem touch = MenuItemHelper.touchFile("12", this::touch);
////            // 创建文件夹
////            FXMenuItem mkdir = FXMenuItem.newItem(I18nHelper.mkdir(), new FolderSVGGlyph("12"), this::mkdir);
//            // 上传文件
//            FXMenuItem uploadFile = MenuItemHelper.uploadFile("12", this::uploadFile);
//            // 上传文件夹
//            FXMenuItem uploadFolder = MenuItemHelper.uploadFolder("12", this::uploadFolder);
////            menuItems.add(touch);
////            menuItems.add(mkdir);
//            menuItems.add(uploadFile);
//            menuItems.add(uploadFolder);
//            return menuItems;
//        }
//        // 发现操作中的文件，则跳过
//        for (ShellSftpFile file : files) {
//            if (file.isWaiting()) {
//                return Collections.emptyList();
//            }
//        }
        // 检查是否包含无效文件
        if (this.checkInvalid(files)) {
            return Collections.emptyList();
        }
        // 获取父级菜单
        List<MenuItem> menuItems = new ArrayList<>(super.getMenuItems());
        menuItems.add(MenuItemHelper.separator());
        // 上传文件
        FXMenuItem uploadFile = MenuItemHelper.uploadFile("12", this::uploadFile);
        // 上传文件夹
        FXMenuItem uploadFolder = MenuItemHelper.uploadFolder("12", this::uploadFolder);
        menuItems.add(uploadFile);
        menuItems.add(uploadFolder);
        if (!files.isEmpty()) {
            // 下载文件
            FXMenuItem downloadFile = MenuItemHelper.downloadFile("12", () -> this.downloadFile(files));
            menuItems.add(downloadFile);
        }
        return menuItems;
    }

//    @Override
//    protected void onMouseClicked(MouseEvent event) {
//        try {
//            List<ShellSftpFile> files = this.getSelectedItems();
//            if (files == null) {
//                return;
//            }
//            if (files.size() != 1) {
//                return;
//            }
//            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
//                ShellSftpFile file = files.getFirst();
//                if (file.isDir()) {
//                    this.intoDir(file);
//                } else if (file.isFile()) {
//                    this.editFile(file);
//                }
//            } else {
//                super.onMouseClicked(event);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public void copyFilePath() {
//        List<ShellSftpFile> files = this.getSelectedItems();
//        if (files.isEmpty()) {
//            ClipboardUtil.copy(this.getLocation());
//        } else if (files.size() == 1) {
//            ClipboardUtil.copy(ShellSftpUtil.concat(this.getLocation(), files.getFirst().getFileName()));
//        } else {
//            MessageBox.warn(I18nHelper.tooManyFiles());
//        }
//    }

    /**
     * 下载回调
     */
    private Consumer<List<ShellSftpFile>> downloadFileCallback;

    public Consumer<List<ShellSftpFile>> getDownloadFileCallback() {
        return downloadFileCallback;
    }

    public void setDownloadFileCallback(Consumer<List<ShellSftpFile>> downloadFileCallback) {
        this.downloadFileCallback = downloadFileCallback;
    }

    public boolean downloadFile(List<ShellSftpFile> files) {
        File dir = DirChooserHelper.chooseDownload(I18nHelper.pleaseSelectDirectory());
        if (dir != null && dir.isDirectory() && dir.exists()) {
            String[] fileArr = dir.list();
            // 检查文件是否存在
            if (ArrayUtil.isNotEmpty(fileArr)) {
                for (String f1 : fileArr) {
                    Optional<ShellSftpFile> file = files.parallelStream().filter(f -> StringUtil.equalsIgnoreCase(f.getFileName(), f1)).findAny();
                    if (file.isPresent()) {
                        if (!MessageBox.confirm(ShellI18nHelper.fileTip6())) {
                            return false;
                        }
                        break;
                    }
                }
            }
            for (ShellSftpFile file : files) {
                try {
                    file.setParentPath(this.getLocation());
                    this.client.download(dir, file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            }
            // 下载回调触发
            if (this.downloadFileCallback != null) {
                this.downloadFileCallback.accept(files);
            }
            return true;
        }
        return false;
    }

    /**
     * 上传回调
     */
    private Consumer<List<File>> uploadFileCallback;

    public Consumer<List<File>> getUploadFileCallback() {
        return uploadFileCallback;
    }

    public void setUploadFileCallback(Consumer<List<File>> uploadFileCallback) {
        this.uploadFileCallback = uploadFileCallback;
    }

    public void uploadFile() {
        try {
            List<File> files = FileChooserHelper.chooseMultiple(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
            this.uploadFile(files);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public void uploadFolder() {
        try {
            File file = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory());
            this.uploadFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
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
                this.client.upload(file, this.getLocation());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        }
        // 下载回调触发
        if (this.uploadFileCallback != null) {
            this.uploadFileCallback.accept(files);
        }
        return true;
    }
}
