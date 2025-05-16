package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public abstract class ShellFileTableView<C extends ShellFileClient<E>, E extends ShellFile> extends FXTableView<E> implements FXEventListener {

    @Override
    public void initNode() {
        super.initNode();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollectionUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
        // 初始化鼠标多选辅助类
        TableViewMouseSelectHelper.install(this);
        // 快捷键
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // 删除
            if (KeyboardUtil.delete_keyCombination.match(event)) {
                this.deleteFile(this.getFilterSelectedItems());
                event.consume();
            } else if (KeyboardUtil.rename_keyCombination.match(event)) {// 重命名
                this.renameFile(this.getFilterSelectedItems());
                event.consume();
            } else if (KeyboardUtil.refresh_keyCombination.match(event)) {// 刷新
                this.loadFile();
                event.consume();
            } else if (KeyboardUtil.info_keyCombination.match(event)) {// 文件信息
                this.fileInfo(this.getSelectedItem());
                event.consume();
            } else if (KeyboardUtil.edit_keyCombination.match(event)) {// 编辑
                this.editFile(this.getSelectedItem());
                event.consume();
            }
        });
    }

    /**
     * 过滤内容
     */
    protected String filterText;

    /**
     * 设置过滤内容
     *
     * @param filterText 过滤内容
     */
    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshFile();
        }
    }

    /**
     * 是否显示隐藏文件
     */
    protected boolean showHiddenFile;

    /**
     * 设置显示文件
     *
     * @param showHiddenFile 是否显示隐藏文件
     */
    public void setShowHiddenFile(boolean showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
        this.refreshFile();
    }

    /**
     * 文件客户端
     */
    protected C client;

    /**
     * 获取客户端
     *
     * @return 文件客户端
     */
    public C getClient() {
        return client;
    }

    /**
     * 设置客户端
     *
     * @param client 客户端
     */
    public void setClient(C client) {
        this.client = client;
    }

    /**
     * 位置属性
     */
    private final StringProperty locationProperty = new SimpleStringProperty();

    /**
     * 获取位置
     *
     * @return 位置
     */
    public String getLocation() {
        return this.locationProperty.get();
    }

    /**
     * 获取位置属性
     *
     * @return 位置属性
     */
    public StringProperty locationProperty() {
        return this.locationProperty;
    }

    /**
     * 设置位置
     *
     * @param location 位置
     */
    protected void setLocation(String location) {
        if (StringUtil.notEquals(this.getLocation(), location)) {
            this.clearItems();
        }
        this.locationProperty.set(location);
    }

    /**
     * 真实文件列表
     */
    protected List<E> files;

    /**
     * 加载文件
     */
    public void loadFile() {
        StageManager.showMask(() -> {
            try {
                this.loadFileInner();
                this.refresh();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 加载文件，内部实现
     *
     * @throws Exception 异常
     */
    protected synchronized void loadFileInner() throws Exception {
        try {
            String currPath = this.getLocation();
            if (currPath == null) {
                this.setLocation(this.client.workDir());
                currPath = this.getLocation();
            } else if (currPath.isBlank()) {
                currPath = "/";
            }
            JulLog.info("current path: {}", currPath);
            // 更新当前列表
            this.files = this.client.lsFile(currPath);
            // 过滤出来待显示的列表
            List<E> files = this.doFilter(this.files);
            // 当前在显示的列表
            List<E> items = this.getItems();
            // 删除列表
            List<E> delList = new ArrayList<>();
            // 新增列表
            List<E> addList = new ArrayList<>();
            // 遍历已有集合，如果不在待显示列表，则删除，否则更新
            for (E file : items) {
                Optional<E> optional = files.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
                if (optional.isEmpty()) {
                    delList.add(file);
                } else {
                    file.copy(optional.get());
                }
            }
            // 遍历待显示列表，如果不在已显示列表，则新增
            for (E file : files) {
                Optional<E> optional = items.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
                if (optional.isEmpty()) {
                    addList.add(file);
                }
            }

            // 删除数据
            this.removeItem(delList);
            // 新增数据
            this.addItem(addList);
        } catch (Throwable ex) {
            if (ExceptionUtil.hasMessage(ex, "inputstream is closed", "4: ", "0: Success")) {
                this.loadFileInner();
            } else {
                throw ex;
            }
        }
    }

    /**
     * 刷新文件
     */
    public void refreshFile() {
        if (this.files == null) {
            this.loadFile();
        } else {
            this.setItem(this.doFilter(this.files));
        }
        super.refresh();
    }

    /**
     * 进行过滤
     *
     * @param files 文件列表
     * @return 过滤后的文件
     */
    protected List<E> doFilter(List<E> files) {
        if (CollectionUtil.isNotEmpty(files)) {
            return files.stream()
                    .filter(f -> {
                        if (f == null) {
                            return false;
                        }
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
                    .sorted(Comparator.comparingInt(ShellFile::getFileOrder))
                    .collect(Collectors.toList());
        }
        return new CopyOnWriteArrayList<>(files);
    }

    /**
     * 检查是否无效
     *
     * @param files 文件列表
     * @return 结果
     */
    protected boolean checkInvalid(List<E> files) {
        for (E file : files) {
            if (this.checkInvalid(file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否无效
     *
     * @param file 文件
     * @return 结果
     */
    protected boolean checkInvalid(E file) {
        return !file.isNormal() || file.isWaiting();
    }

    /**
     * 显示文件信息
     *
     * @param file 文件
     */
    protected void fileInfo(E file) {
        if (file != null && !this.checkInvalid(file)) {
            ShellViewFactory.fileInfo(file, this.window());
        }
    }

    /**
     * 复制文件路径
     *
     * @param file 文件
     */
    protected void copyFilePath(E file) {
        ClipboardUtil.copy(file.getFilePath());
    }

    /**
     * 鼠标点击事件
     *
     * @param event 事件
     */
    protected void onMouseClicked(MouseEvent event) {
        try {
            // 鼠标后退
            if (event.getButton() == MouseButton.BACK && event.getClickCount() == 1) {
                this.back();
                return;
            }
            List<E> files = this.getSelectedItems();
            if (files == null) {
                return;
            }
            if (files.size() != 1) {
                return;
            }
            // 鼠标前进
            if (event.getButton() == MouseButton.FORWARD && event.getClickCount() == 1) {
                this.forward();
                return;
            }
            // 鼠标按键
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                E file = files.getFirst();
                if (file.isDirectory()) {
                    this.intoDir(file);
                } else if (file.isFile()) {
                    this.editFile(file);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 当前是否根目标
     *
     * @return 结果
     */
    public boolean currentIsRootDirectory() {
        return "/".equals(this.getLocation());
    }

    /**
     * 进入目录
     *
     * @param file 文件
     */
    public void intoDir(E file) {
        if (file.isReturnDirectory()) {
            this.returnDir();
            return;
        }
        this.intoDir(file.getFilePath());
    }

    /**
     * 进入目录
     *
     * @param filePath 文件路径
     */
    public void intoDir(String filePath) {
        this.setLocation(filePath);
        this.loadFile();
    }

    /**
     * 后退
     */
    public void back() {
        String location = this.getLocation();
        String parent = ShellFileUtil.parent(location);
        this.intoDir(parent);
    }

    /**
     * 前进
     */
    public void forward() {
        List<E> files = this.getSelectedItems();
        if (files.size() != 1) {
            return;
        }
        E file = files.getFirst();
        if (!file.isDirectory()) {
            return;
        }
        this.intoDir(file);
    }

    /**
     * 返回上一级
     */
    public void returnDir() {
        if (this.currentIsRootDirectory()) {
            return;
        }
        String currPath = this.getLocation();
        if (currPath.endsWith("/")) {
            currPath = currPath.substring(0, currPath.length() - 1);
        }
        currPath = currPath.substring(0, currPath.lastIndexOf("/") + 1);
        this.setLocation(currPath);
        this.loadFile();
    }

    /**
     * 文件是否存在
     *
     * @param fileName 文件名
     * @return 结果
     */
    public boolean existFile(String fileName) {
        Optional<E> optional = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
        return optional.isPresent();
    }

    /**
     * 删除文件
     *
     * @param files 文件列表
     */
    public void deleteFile(List<E> files) {
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        // 过滤文件
        files = files.stream().filter(f -> !this.checkInvalid(f)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (files.size() == 1) {
            E file = files.getFirst();
            if (file.isDirectory() && !MessageBox.confirm(I18nHelper.deleteDir() + " " + file.getFileName())) {
                return;
            }
            if (!file.isDirectory() && !MessageBox.confirm(I18nHelper.deleteFile() + " " + file.getFileName())) {
                return;
            }
        } else if (!MessageBox.confirm(ShellI18nHelper.fileTip2())) {
            return;
        }
        try {
            List<E> fList = new CopyOnWriteArrayList<>(files);
            for (E file : fList) {
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

    /**
     * 文件权限
     *
     * @param file 文件
     */
    public void filePermission(E file) {
        if (this.checkInvalid(file)) {
            return;
        }
        ShellViewFactory.filePermission(file, this.client, this.window());
        // 更新表格
        this.refresh();
    }

    /**
     * 重命名文件
     *
     * @param files 文件列表
     */
    public void renameFile(List<E> files) {
        try {
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            E file = files.getFirst();
            if (this.checkInvalid(file)) {
                return;
            }
            String newName = MessageBox.prompt(I18nHelper.pleaseInputContent(), file.getFileName());
            String name = file.getFileName();
            if (newName == null || StringUtil.equals(name, newName)) {
                return;
            }
            String filePath = ShellFileUtil.concat(file.getParentPath(), name);
            String newPath = ShellFileUtil.concat(file.getParentPath(), newName);
            if (this.client.rename(filePath, newPath)) {
                file.setFileName(newName);
                this.refreshFile();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 进入目录
     *
     * @param filePath 文件路径
     */
    public void cd(String filePath) {
        if (!StringUtil.isBlank(filePath)) {
            try {
                if (this.client.exist(filePath)) {
                    this.setLocation(filePath);
                    this.loadFile();
                }
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 编辑文件
     *
     * @param file 文件
     */
    public void editFile(E file) {
        if (!ShellFileUtil.fileEditable(file)) {
            return;
        }
        ShellViewFactory.fileEdit(file, this.client, this.window());
        this.onFileSaved(file);
    }

    /**
     * 创建文件
     */
    public void touch() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputFileName());
            this.touch(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 创建文件
     *
     * @param name 文件名
     */
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
        E file = this.client.fileInfo(filePath);
        this.files.add(file);
        this.refreshFile();
    }

    /**
     * 创建文件夹
     */
    public void createDir() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputDirName());
            this.createDir(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 创建文件夹
     *
     * @param name 文件名
     */
    public void createDir(String name) throws Exception {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip5())) {
            return;
        }
        String dirPath = ShellFileUtil.concat(this.getLocation(), name);
        this.client.createDir(dirPath);
        E file = this.client.fileInfo(dirPath);
        this.files.add(file);
        this.refreshFile();
    }

    /**
     * 文件保存事件
     *
     * @param file 文件
     */
    public void onFileSaved(ShellFile file) {
        if (this.existFile(file.getFileName())) {
            this.refresh();
        }
    }

    /**
     * 文件删除时间
     *
     * @param remoteFile 文件
     */
    public void onFileDeleted(String remoteFile) {
        Optional<E> optional = this.files.parallelStream()
                .filter(f -> StringUtil.equals(remoteFile, f.getFilePath()))
                .findAny();
        if (optional.isPresent()) {
            this.files.remove(optional.get());
            this.refreshFile();
        }
    }

    /**
     * 上传文件
     */
    public void uploadFile() {
        try {
            List<File> files = FileChooserHelper.chooseMultiple(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
            this.uploadFile(files);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 上传文件夹
     */
    public void uploadFolder() {
        try {
            File file = DirChooserHelper.choose(I18nHelper.pleaseSelectDirectory());
            this.uploadFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 上传文件
     *
     * @param file 文件
     */
    public void uploadFile(File file) {
        if (file != null && file.exists()) {
            this.uploadFile(Collections.singletonList(file));
        }
    }

    /**
     * 上传文件
     *
     * @param files 文件列表
     */
    public void uploadFile(List<File> files) {
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        // 检查要上传的文件是否存在
        for (File file : files) {
            if (this.existFile(file.getName())) {
                if (!MessageBox.confirm(ShellI18nHelper.fileTip3())) {
                    return;
                }
                break;
            }
        }
        for (File file : files) {
            this.client.doUpload(file, this.getLocation());
        }
        MessageBox.okToast(I18nHelper.addedToUploadList());
    }

    /**
     * 下载文件
     *
     * @param files 文件列表
     */
    public void downloadFile(List<E> files) {
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        files = files.stream().filter(f -> !this.checkInvalid(f)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        File dir = DirChooserHelper.chooseDownload(I18nHelper.pleaseSelectDirectory());
        if (dir != null && dir.isDirectory() && dir.exists()) {
            String[] fileArr = dir.list();
            // 检查文件是否存在
            if (ArrayUtil.isNotEmpty(fileArr)) {
                for (String f1 : fileArr) {
                    Optional<E> file = files.parallelStream().filter(f -> StringUtil.equalsIgnoreCase(f.getFileName(), f1)).findAny();
                    if (file.isPresent()) {
                        if (!MessageBox.confirm(ShellI18nHelper.fileTip6())) {
                            return;
                        }
                        break;
                    }
                }
            }
            for (E file : files) {
                this.client.doDownload(file, dir);
            }
            MessageBox.okToast(I18nHelper.addedToDownloadList());
        }
    }

    /**
     * 进入home目录
     */
    public void intoHome() throws Exception {
        this.intoDir(this.client.workDir());
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<E> files = this.getFilterSelectedItems();
        // if (CollectionUtil.isEmpty(files)) {
        //     return Collections.emptyList();
        // }
        // // 检查是否包含无效文件
        // if (this.checkInvalid(files)) {
        //     return Collections.emptyList();
        // }
        List<MenuItem> menuItems = new ArrayList<>();
        // 创建文件
        FXMenuItem touchFile = MenuItemHelper.touchFile("12", this::touch);
        menuItems.add(touchFile);
        // 创建文件夹
        FXMenuItem createDir = FXMenuItem.newItem(I18nHelper.mkdir(), new FolderSVGGlyph("12"), this::createDir);
        menuItems.add(createDir);
        menuItems.add(MenuItemHelper.separator());
        if (files.size() == 1) {
            E file = files.getFirst();
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
        deleteFile.setDisable(files.isEmpty());
        menuItems.add(deleteFile);
        return menuItems;
    }

    /**
     * 获取过滤的选中列表
     *
     * @return 过滤的选中列表
     */
    public List<E> getFilterSelectedItems() {
        List<E> list = super.getSelectedItems();
        return list.stream().filter(f -> !this.checkInvalid(f)).collect(Collectors.toList());
    }
}
