package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.file.ShellFileSavedEvent;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPUploadTask;
import cn.oyzh.easyshell.sftp.ShellSFTPUtil;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpATTRS;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ShellSFTPFileBaseTableView extends ShellFileTableView<ShellSFTPClient, ShellSFTPFile> implements FXEventListener {

    @Override
    public void setClient(ShellSFTPClient client) {
        super.setClient(client);
//        this.client.getDeleteManager().addDeleteDeletedCallback(this, this::fileDeleted);
        this.client.uploadTasks().addListener((ListChangeListener<ShellSFTPUploadTask>) change -> {
            if (this.client.isUploadTaskEmpty()) {
                this.loadFile();
            }
        });
        this.client.deleteTasks().addListener((ListChangeListener<ShellFileDeleteTask>) change -> {
            change.next();
            if (change.wasRemoved()) {
                for (ShellFileDeleteTask task : change.getRemoved()) {
                    this.onFileDeleted(task.getFilePath());
                }
            }
        });
    }

    //    @Override
//    public void initNode() {
//        super.initNode();
//        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//    }
//
//    @Override
//    protected void initEvenListener() {
//        super.initEvenListener();
//        // 右键菜单事件
//        this.setOnContextMenuRequested(e -> {
//            List<? extends MenuItem> items = this.getMenuItems();
//            if (CollectionUtil.isNotEmpty(items)) {
//                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
//            } else {
//                this.clearContextMenu();
//            }
//        });
//        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
//        // 初始化鼠标多选辅助类
//        TableViewMouseSelectHelper.install(this);
//        // 快捷键
//        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            // 删除
//            if (KeyboardUtil.delete_keyCombination.match(event)) {
//                this.deleteFile(this.getSelectedItems());
//                event.consume();
//            } else if (KeyboardUtil.rename_keyCombination.match(event)) {// 重命名
//                this.renameFile(this.getSelectedItems());
//                event.consume();
//            } else if (KeyboardUtil.refresh_keyCombination.match(event)) {// 刷新
//                this.loadFile();
//                event.consume();
//            } else if (KeyboardUtil.info_keyCombination.match(event)) {// 文件信息
//                this.fileInfo(this.getSelectedItem());
//                event.consume();
//            } else if (KeyboardUtil.edit_keyCombination.match(event)) {// 编辑
//                this.editFile(this.getSelectedItem());
//                event.consume();
//            }
//        });
//    }
//
//    private String filterText;
//
//    public void setFilterText(String filterText) {
//        if (!StringUtil.equals(this.filterText, filterText)) {
//            this.filterText = filterText;
//            this.refreshFile();
//        }
//    }
//
//    private boolean showHiddenFile = false;
//
//    public void setShowHiddenFile(boolean showHiddenFile) {
////        if (showHiddenFile != this.showHiddenFile) {
//        this.showHiddenFile = showHiddenFile;
//        this.refreshFile();
////        }
//    }

//    protected ShellSFTPClient client;
//
//    public ShellSFTPClient getClient() {
//        return client;
//    }
//
//    public void setClient(ShellSFTPClient client) {
//        this.client = client;
//    }

//    /**
//     * 位置属性
//     */
//    private final StringProperty locationProperty = new SimpleStringProperty();
//
//    public String getLocation() {
//        return this.locationProperty.get();
//    }
//
//    public StringProperty locationProperty() {
//        return this.locationProperty;
//    }
//
//    protected void setLocation(String location) {
//        if (StringUtil.notEquals(this.getLocation(), location)) {
//            this.clearItems();
//        }
//        this.locationProperty.set(location);
//    }

//    protected String location() {
//        return this.locationProperty.get();
//    }
//
//    protected void location(String location) {
//        if (StringUtil.notEquals(this.location(), location)) {
//            this.clearItems();
//        }
//        this.locationProperty.set(location);
//    }


//    protected List<ShellSFTPFile> files;
//
//    public void loadFile() {
//        try {
//            this.loadFileInner();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    protected synchronized void loadFileInner() throws SftpException {
//        try {
//            String currPath = this.getLocation();
//            if (currPath == null) {
//                this.setLocation(this.client.pwdDir());
//                currPath = this.getLocation();
//            } else if (currPath.isBlank()) {
//                currPath = "/";
//            }
//            JulLog.info("current path: {}", currPath);
//            // 更新当前列表
//            this.files = this.client.lsFile(currPath);
//            // 过滤出来待显示的列表
//            List<ShellSFTPFile> files = this.doFilter(this.files);
//            // 当前在显示的列表
//            List<ShellSFTPFile> items = this.getItems();
//            // 删除列表
//            List<ShellSFTPFile> delList = new ArrayList<>();
//            // 新增列表
//            List<ShellSFTPFile> addList = new ArrayList<>();
//            // 遍历已有集合，如果不在待显示列表，则删除，否则更新
//            for (ShellSFTPFile file : items) {
//                Optional<ShellSFTPFile> optional = files.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
//                if (optional.isEmpty()) {
//                    delList.add(file);
//                } else {
//                    file.copy(optional.get());
//                }
//            }
//            // 遍历待显示列表，如果不在已显示列表，则新增
//            for (ShellSFTPFile file : files) {
//                Optional<ShellSFTPFile> optional = items.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
//                if (optional.isEmpty()) {
//                    addList.add(file);
//                }
//            }
//
//            // 删除数据
//            this.removeItem(delList);
//            // 新增数据
//            this.addItem(addList);
//        } catch (Throwable ex) {
//            if (ExceptionUtil.hasMessage(ex, "inputstream is closed", "4: ", "0: Success")) {
//                this.loadFileInner();
//            } else {
//                throw ex;
//            }
//        }
//    }

//    public void refreshFile() {
//        if (this.files == null) {
//            this.loadFile();
//        } else {
//            this.setItem(this.doFilter(this.files));
//        }
//    }
//
//    protected List<ShellSFTPFile> doFilter(List<ShellSFTPFile> files) {
//        if (CollectionUtil.isNotEmpty(files)) {
//            return files.stream()
//                    .filter(f -> {
//                        if (f.isCurrentFile()) {
//                            return false;
//                        }
//                        if (this.currentIsRootDirectory() && f.isReturnDirectory()) {
//                            return false;
//                        }
//                        if (!this.showHiddenFile && f.isHiddenFile()) {
//                            return false;
//                        }
//                        if (StringUtil.isNotEmpty(this.filterText) && !StringUtil.containsIgnoreCase(f.getFileName(), this.filterText)) {
//                            return false;
//                        }
//                        return true;
//                    })
//                    .sorted(Comparator.comparingInt(ShellSFTPFile::getFileOrder))
//                    .collect(Collectors.toList());
//        }

    /// /        return files;
//        return new CopyOnWriteArrayList<>(files);
//    }

//    protected boolean checkInvalid(List<ShellSFTPFile> files) {
//        for (ShellSFTPFile file : files) {
//            if (this.checkInvalid(file)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    protected boolean checkInvalid(ShellSFTPFile file) {
//        return file.isCurrentFile() || file.isReturnDirectory() || file.isWaiting();
//    }
    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<ShellSFTPFile> files = this.getSelectedItems();
//        // 发现操作中的文件，则跳过
//        for (ShellSFTPFile file : files) {
//            if (file.isWaiting()) {
//                return Collections.emptyList();
//            }
//        }
        // 检查是否包含无效文件
        if (this.checkInvalid(files)) {
            return Collections.emptyList();
        }

        List<MenuItem> menuItems = new ArrayList<>();
        // 创建文件
        FXMenuItem touch = MenuItemHelper.touchFile("12", this::touch);
        menuItems.add(touch);
        // 创建文件夹
        FXMenuItem mkdir = FXMenuItem.newItem(I18nHelper.mkdir(), new FolderSVGGlyph("12"), this::mkdir);
        menuItems.add(mkdir);
        menuItems.add(MenuItemHelper.separator());
        if (files.size() == 1) {
            ShellSFTPFile file = files.getFirst();
            // 编辑文件
            FXMenuItem editFile = MenuItemHelper.editFile("12", () -> this.editFile(file));
            if (!ShellFileUtil.fileEditable(file)) {
                editFile.setDisable(true);
            }
            editFile.setAccelerator(KeyboardUtil.edit_keyCombination);
            menuItems.add(editFile);
            // 文件信息
            FXMenuItem fileInfo = MenuItemHelper.fileInfo("12", () -> this.fileInfo(file));
            fileInfo.setAccelerator(KeyboardUtil.info_keyCombination);
            menuItems.add(fileInfo);
            // 复制路径
            FXMenuItem copyFilePath = MenuItemHelper.copyFilePath("12", () -> this.copyFilePath(file));
            menuItems.add(copyFilePath);
            // 重命名文件
            FXMenuItem renameFile = MenuItemHelper.renameFile("12", () -> this.renameFile(files));
            renameFile.setAccelerator(KeyboardUtil.rename_keyCombination);
            menuItems.add(renameFile);
            // 文件权限
            FXMenuItem filePermission = MenuItemHelper.filePermission("12", () -> this.filePermission(file));
            menuItems.add(filePermission);
            menuItems.add(MenuItemHelper.separator());
        }
        // 刷新文件
        FXMenuItem refreshFile = MenuItemHelper.refreshFile("12", this::loadFile);
        refreshFile.setAccelerator(KeyboardUtil.refresh_keyCombination);
        menuItems.add(refreshFile);
        // 删除文件
        FXMenuItem deleteFile = MenuItemHelper.deleteFile("12", () -> this.deleteFile(files));
        deleteFile.setAccelerator(KeyboardUtil.delete_keyCombination);
        menuItems.add(deleteFile);
        return menuItems;
    }

//    /**
//     * 显示文件信息
//     *
//     * @param file 文件
//     */
//    protected void fileInfo(ShellSFTPFile file) {
//        if (file != null && !this.checkInvalid(file)) {
//            ShellViewFactory.fileInfo(file);
//        }
//    }
//
//    protected void copyFilePath(ShellSFTPFile file) {
//        ClipboardUtil.copy(file.getFilePath());
//    }

//    protected void onMouseClicked(MouseEvent event) {
//        try {
//            // 鼠标后退
//            if (event.getButton() == MouseButton.BACK && event.getClickCount() == 1) {
//                this.back();
//                return;
//            }
//            List<ShellSFTPFile> files = this.getSelectedItems();
//            if (files == null) {
//                return;
//            }
//            if (files.size() != 1) {
//                return;
//            }
//            // 鼠标前进
//            if (event.getButton() == MouseButton.FORWARD && event.getClickCount() == 1) {
//                this.forward();
//                return;
//            }
//            // 鼠标按键
//            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
//                ShellSFTPFile file = files.getFirst();
//                if (file.isDirectory()) {
//                    this.intoDir(file);
//                } else if (file.isFile()) {
//                    this.editFile(file);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public boolean currentIsRootDirectory() {
//        return "/".equals(this.getLocation());
//    }
//
//    public void intoDir(ShellSFTPFile file) {
//        if (file.isReturnDirectory()) {
//            this.returnDir();
//            return;
//        }
//        this.intoDir(file.getFilePath());
//    }
//
//    public void intoDir(String filePath) {
//        this.setLocation(filePath);
//        this.loadFile();
//    }

//    /**
//     * 后退
//     */
//    public void back() {
//        String location = this.getLocation();
//        String parent = ShellFileUtil.parent(location);
//        this.intoDir(parent);
//    }

//    /**
//     * 前进
//     */
//    public void forward() {
//        List<ShellSFTPFile> files = this.getSelectedItems();
//        if (files.size() != 1) {
//            return;
//        }
//        ShellSFTPFile file = files.getFirst();
//        if (!file.isDirectory()) {
//            return;
//        }
//        this.intoDir(file);
//    }

//    public void returnDir() {
//        if (this.currentIsRootDirectory()) {
//            return;
//        }
//        String currPath = this.getLocation();
//        if (currPath.endsWith("/")) {
//            currPath = currPath.substring(0, currPath.length() - 1);
//        }
//        currPath = currPath.substring(0, currPath.lastIndexOf("/") + 1);
//        this.setLocation(currPath);
//        this.loadFile();
//    }

    /**
     * 进入home目录
     */
    public void intoHome() {
        this.intoDir(this.client.getUserHome());
    }

//    public boolean existFile(String fileName) {
//        Optional<ShellSFTPFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
//        return sftpFile.isPresent();
//    }

    @Override
    public void deleteFile(List<ShellSFTPFile> files) {
        if (CollectionUtil.isEmpty(files) || this.checkInvalid(files)) {
            return;
        }
        if (files.size() == 1) {
            ShellSFTPFile file = files.getFirst();
            if (file.isDirectory() && !MessageBox.confirm(I18nHelper.deleteDir() + " " + file.getFileName())) {
                return;
            }
            if (!file.isDirectory() && !MessageBox.confirm(I18nHelper.deleteFile() + " " + file.getFileName())) {
                return;
            }
        } else if (!MessageBox.confirm(ShellI18nHelper.fileTip2())) {
            return;
        }
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        try {
            List<ShellSFTPFile> sftpFiles = new CopyOnWriteArrayList<>(files);
            for (ShellSFTPFile file : sftpFiles) {
                // 不可删除文件
                if (file.isReturnDirectory() || file.isCurrentFile()) {
                    continue;
                }
                // 隐藏文件
                if (file.isHiddenFile() && !MessageBox.confirm(file.getFileName() + " " + ShellI18nHelper.fileTip1())) {
                    continue;
                }
                // 执行删除
                this.client.doDelete(file);
            }
            this.refreshFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void filePermission(ShellSFTPFile file) {
        if (this.checkInvalid(file)) {
            return;
        }
        ShellViewFactory.sftpFilePermission(file, this.client);
    }

//    /**
//     * 重命名文件
//     *
//     * @param files 文件列表
//     */
//    public void renameFile(List<ShellSFTPFile> files) {
//        try {
//            if (files == null || files.size() != 1) {
//                return;
//            }
//            if (this.checkInvalid(files)) {
//                return;
//            }
//            ShellSFTPFile file = files.getFirst();
//            String newName = MessageBox.prompt(I18nHelper.pleaseInputContent(), file.getFileName());
//            String name = file.getFileName();
//            if (newName == null || StringUtil.equals(name, newName)) {
//                return;
//            }
//            String filePath = ShellFileUtil.concat(file.getParentPath(), name);
//            String newPath = ShellFileUtil.concat(file.getParentPath(), newName);
//            this.client.rename(filePath, newPath);
//            file.setFileName(newName);
//            this.refreshFile();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

//    public void fileDeleted(String remoteFile) {
//        Optional<ShellSFTPFile> optional = this.files.parallelStream()
//                .filter(f -> StringUtil.equals(remoteFile, f.getFilePath()))
//                .findAny();
//        if (optional.isPresent()) {
//            this.files.remove(optional.get());
//            this.refreshFile();
//        }
//    }

    @Override
    public void cd(String path) {
        if (!StringUtil.isBlank(path)) {
            try {
                if (this.client.exist(path)) {
                    this.setLocation(path);
                    this.loadFile();
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        }
    }

    @Override
    public void editFile(ShellSFTPFile file) {
        if (!ShellFileUtil.fileEditable(file)) {
            return;
        }
        ShellViewFactory.sftpFileEdit(file, this.client);
    }
//
//    public void touch() {
//        try {
//            String name = MessageBox.prompt(I18nHelper.pleaseInputFileName());
//            this.touch(name);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

    @Override
    public void touch(String name) throws Exception {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip4())) {
            return;
        }
        String filePath = ShellFileUtil.concat(this.getLocation(), name);
        this.client.touch(filePath);
        SftpATTRS attrs = this.client.stat(filePath);
        ShellSFTPFile file = new ShellSFTPFile(this.getLocation(), name, attrs);
        // 读取链接文件
        ShellSFTPUtil.realpath(file, this.client);
        file.setOwner(ShellSFTPUtil.getOwner(file.getUid(), this.client));
        file.setGroup(ShellSFTPUtil.getGroup(file.getGid(), this.client));
        this.files.add(file);
        this.refreshFile();
    }

//    public void mkdir() {
//        try {
//            String name = MessageBox.prompt(I18nHelper.pleaseInputDirName());
//            this.mkdir(name);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }

    @Override
    public void mkdir(String name) throws Exception {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip5())) {
            return;
        }
        String filePath = ShellFileUtil.concat(this.getLocation(), name);
        this.client.mkdir(filePath);
        SftpATTRS attrs = this.client.stat(filePath);
        ShellSFTPFile file = new ShellSFTPFile(this.getLocation(), name, attrs);
        // 读取链接文件
        ShellSFTPUtil.realpath(file, this.client);
        if (this.client.isWindows()) {
            file.setOwner("-");
            file.setGroup("-");
        } else {
            file.setOwner(ShellSFTPUtil.getOwner(file.getUid(), this.client));
            file.setGroup(ShellSFTPUtil.getGroup(file.getGid(), this.client));
        }
        this.files.add(file);
        this.refreshFile();
    }

    @Override
    @EventSubscribe
    public void onFileSaved(ShellFileSavedEvent event) {
        super.onFileSaved(event);
    }
}
