package cn.oyzh.easyshell.fx.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.event.file.ShellFileSavedEvent;
import cn.oyzh.easyshell.file.FileClient;
import cn.oyzh.easyshell.util.ShellFile;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public abstract class ShellFileTableView<C extends FileClient<E>, E extends ShellFile> extends FXTableView<E> implements FXEventListener {

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
                this.deleteFile(this.getSelectedItems());
                event.consume();
            } else if (KeyboardUtil.rename_keyCombination.match(event)) {// 重命名
                this.renameFile(this.getSelectedItems());
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
        String location = locationProperty.get();
        if (location == null) {
            return "/";
        }
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
                this.setLocation(this.client.pwdDir());
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
        return file.isCurrentFile() || file.isReturnDirectory() || file.isWaiting();
    }

    /**
     * 显示文件信息
     *
     * @param file 文件
     */
    protected void fileInfo(E file) {
        if (file != null && !this.checkInvalid(file)) {
            ShellViewFactory.fileInfo(file);
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
    public abstract void deleteFile(List<E> files);

    /**
     * 文件权限
     *
     * @param file 文件
     */
    public abstract void filePermission(E file);

    /**
     * 重命名文件
     *
     * @param files 文件列表
     */
    public void renameFile(List<E> files) {
        try {
            if (files == null || files.size() != 1) {
                return;
            }
            if (this.checkInvalid(files)) {
                return;
            }
            E file = files.getFirst();
            String newName = MessageBox.prompt(I18nHelper.pleaseInputContent(), file.getFileName());
            String name = file.getFileName();
            if (newName == null || StringUtil.equals(name, newName)) {
                return;
            }
            String filePath = ShellFileUtil.concat(file.getParentPath(), name);
            String newPath = ShellFileUtil.concat(file.getParentPath(), newName);
            this.client.rename(filePath, newPath);
            file.setFileName(newName);
            this.refreshFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 进入目录
     *
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    public abstract void cd(String filePath) throws Exception;

    /**
     * 编辑文件
     *
     * @param file 文件
     */
    public abstract void editFile(E file);

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
    public abstract void touch(String name) throws Exception;

    /**
     * 创建文件夹
     */
    public void mkdir() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputDirName());
            this.mkdir(name);
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
    public abstract void mkdir(String name) throws Exception;

    /**
     * 文件保存事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void onFileSaved(ShellFileSavedEvent event) {
        if (this.existFile(event.fileName())) {
            this.refresh();
        }
    }

    public void onFileDeleted(String remoteFile) {
        Optional<E> optional = this.files.parallelStream()
                .filter(f -> StringUtil.equals(remoteFile, f.getFilePath()))
                .findAny();
        if (optional.isPresent()) {
            this.files.remove(optional.get());
            this.refreshFile();
        }
    }
}
