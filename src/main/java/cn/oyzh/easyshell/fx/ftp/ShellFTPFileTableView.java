package cn.oyzh.easyshell.fx.ftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.file.ShellFileSavedEvent;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.ftp.ShellFTPFile;
import cn.oyzh.easyshell.ftp.ShellFTPUploadTask;
import cn.oyzh.easyshell.fx.file.ShellFileTableView;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderSVGGlyph;
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
public class ShellFTPFileTableView extends ShellFileTableView<ShellFTPClient, ShellFTPFile> implements FXEventListener {

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
//            }
//        });
//    }
//
//    private boolean showHiddenFile = false;
//
//    public void setShowHiddenFile(boolean showHiddenFile) {
//        this.showHiddenFile = showHiddenFile;
//        this.refreshFile();
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

//    protected ShellFTPClient client;
//
//    public ShellFTPClient getClient() {
//        return client;
//    }

    @Override
    public void setClient(ShellFTPClient client) {
        super.setClient(client);
        this.client.uploadTasks().addListener((ListChangeListener<ShellFTPUploadTask>) c -> {
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

//    /**
//     * 位置属性
//     */
//    private final StringProperty locationProperty = new SimpleStringProperty();
//
//    public String getLocation() {
//        String location = locationProperty.get();
//        if (location == null) {
//            return "/";
//        }
//        return location;
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
//
//    protected List<ShellFTPFile> files;

//    public void loadFile() {
//        StageManager.showMask(() -> {
//            try {
//                this.loadFileInner();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                MessageBox.exception(ex);
//            }
//        });
//    }

//    protected synchronized void loadFileInner() throws Exception {
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
//            List<ShellFTPFile> files = this.doFilter(this.files);
//            // 当前在显示的列表
//            List<ShellFTPFile> items = this.getItems();
//            // 删除列表
//            List<ShellFTPFile> delList = new ArrayList<>();
//            // 新增列表
//            List<ShellFTPFile> addList = new ArrayList<>();
//            // 遍历已有集合，如果不在待显示列表，则删除，否则更新
//            for (ShellFTPFile file : items) {
//                Optional<ShellFTPFile> optional = files.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
//                if (optional.isEmpty()) {
//                    delList.add(file);
//                } else {
//                    file.copy(optional.get());
//                }
//            }
//            // 遍历待显示列表，如果不在已显示列表，则新增
//            for (ShellFTPFile file : files) {
//                Optional<ShellFTPFile> optional = items.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
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
//
//    @Override
//    public void refreshFile() {

    /// /        if (this.files == null) {
    /// /            this.loadFile();
    /// /        } else {
    /// /            this.setItem(this.doFilter(this.files));
    /// /        }
//        super.refreshFile();
//        super.refresh();
//    }

//    protected List<ShellFTPFile> doFilter(List<ShellFTPFile> files) {
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
//                    .sorted(Comparator.comparingInt(ShellFTPFile::getFileOrder))
//                    .collect(Collectors.toList());
//        }
//        return new CopyOnWriteArrayList<>(files);
//    }
//
//    protected boolean checkInvalid(List<ShellFTPFile> files) {
//        for (ShellFTPFile file : files) {
//            if (this.checkInvalid(file)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    protected boolean checkInvalid(ShellFTPFile file) {
//        return file.isCurrentFile() || file.isReturnDirectory() || file.isWaiting();
//    }
    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<ShellFTPFile> files = this.getSelectedItems();
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
            ShellFTPFile file = files.getFirst();
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
            // 复制文件路径
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

//    /**
//     * 显示文件信息
//     *
//     * @param file 文件
//     */
//    protected void fileInfo(ShellFTPFile file) {
//        if (file != null && !this.checkInvalid(file)) {
//            ShellViewFactory.fileInfo(file);
//        }
//    }

//    protected void copyFilePath(ShellFTPFile file) {
//        ClipboardUtil.copy(file.getFilePath());
//    }

//    protected void onMouseClicked(MouseEvent event) {
//        try {
//            // 鼠标后退
//            if (event.getButton() == MouseButton.BACK && event.getClickCount() == 1) {
//                this.back();
//                return;
//            }
//            List<ShellFTPFile> files = this.getSelectedItems();
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
//                ShellFTPFile file = files.getFirst();
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
//    public void intoDir(ShellFTPFile file) {
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
//
//    /**
//     * 后退
//     */
//    public void back() {
//        String location = this.getLocation();
//        String parent = ShellFileUtil.parent(location);
//        this.intoDir(parent);
//    }
//
//    /**
//     * 前进
//     */
//    public void forward() {
//        List<ShellFTPFile> files = this.getSelectedItems();
//        if (files.size() != 1) {
//            return;
//        }
//        ShellFTPFile file = files.getFirst();
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

//    public boolean existFile(String fileName) {
//        Optional<ShellFTPFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
//        return sftpFile.isPresent();
//    }

    @Override
    public void deleteFile(List<ShellFTPFile> files) {
        if (CollectionUtil.isEmpty(files) || this.checkInvalid(files)) {
            return;
        }
        if (files.size() == 1) {
            ShellFTPFile file = files.getFirst();
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
            List<ShellFTPFile> sftpFiles = new CopyOnWriteArrayList<>(files);
            for (ShellFTPFile file : sftpFiles) {
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
    public void filePermission(ShellFTPFile file) {
        if (this.checkInvalid(file)) {
            return;
        }
        ShellViewFactory.ftpFilePermission(file, this.client);
    }

//    /**
//     * 重命名文件
//     *
//     * @param files 文件列表
//     */
//    public void renameFile(List<ShellFTPFile> files) {
//        try {
//            if (files == null || files.size() != 1) {
//                return;
//            }
//            if (this.checkInvalid(files)) {
//                return;
//            }
//            ShellFTPFile file = files.getFirst();
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
        ShellFTPFile file = this.client.finfo(filePath);
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
    public void mkdir(String filePath) throws Exception {
        String dirPath = ShellFileUtil.concat(this.getLocation(), filePath);
        this.client.mkdir(dirPath);
        ShellFTPFile file = this.client.finfo(dirPath);
        this.files.add(file);
        this.refreshFile();
    }

    @Override
    public void cd(String filePath) throws Exception {
        this.setLocation(filePath);
        this.client.cd(filePath);
        this.loadFile();
    }

    @Override
    public void editFile(ShellFTPFile file) {
        if (!ShellFileUtil.fileEditable(file)) {
            return;
        }
        ShellViewFactory.ftpFileEdit(file, this.client);
    }

//    /**
//     * 上传文件
//     */
//    public void uploadFile() {
//        try {
//            List<File> files = FileChooserHelper.chooseMultiple(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
//            this.uploadFile(files);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }
//
//    /**
//     * 上传文件夹
//     */
//    public void uploadFolder() {
//        try {
//            File file = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory());
//            this.uploadFile(file);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            MessageBox.exception(ex);
//        }
//    }
//
//    public boolean uploadFile(File file) {
//        if (file != null && file.exists()) {
//            return this.uploadFile(Collections.singletonList(file));
//        }
//        return false;
//    }
//
//    public boolean uploadFile(List<File> files) {
//        if (CollectionUtil.isEmpty(files)) {
//            return false;
//        }
//        // 检查要上传的文件是否存在
//        for (File file : files) {
//            if (this.existFile(file.getName())) {
//                if (!MessageBox.confirm(ShellI18nHelper.fileTip3())) {
//                    return false;
//                }
//                break;
//            }
//        }
//        for (File file : files) {
//            this.client.doUpload(file, this.getLocation());
//        }
//        MessageBox.okToast(I18nHelper.addedToUploadList());
//        return true;
//    }

//    public boolean downloadFile(List<ShellFTPFile> files) {
//        File dir = DirChooserHelper.chooseDownload(I18nHelper.pleaseSelectDirectory());
//        if (dir != null && dir.isDirectory() && dir.exists()) {
//            MessageBox.okToast(I18nHelper.addedToDownloadList());
//            String[] fileArr = dir.list();
//            // 检查文件是否存在
//            if (ArrayUtil.isNotEmpty(fileArr)) {
//                for (String f1 : fileArr) {
//                    Optional<ShellFTPFile> file = files.parallelStream().filter(f -> StringUtil.equalsIgnoreCase(f.getFileName(), f1)).findAny();
//                    if (file.isPresent()) {
//                        if (!MessageBox.confirm(ShellI18nHelper.fileTip6())) {
//                            return false;
//                        }
//                        break;
//                    }
//                }
//            }
//            for (ShellFTPFile file : files) {
//                    file.setParentPath(this.getLocation());
//                    this.client.doDownload(file, dir);
//            }
//            return true;
//        }
//        return false;
//    }

    @Override
    @EventSubscribe
    public void onFileSaved(ShellFileSavedEvent event) {
        super.onFileSaved(event);
    }
}
