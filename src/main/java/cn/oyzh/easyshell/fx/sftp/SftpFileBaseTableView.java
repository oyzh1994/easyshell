package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFileEditController;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFilePermissionController;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.event.sftp.ShellSftpFileSavedEvent;
import cn.oyzh.easyshell.fx.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.sftp.ShellSftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.event.FXEventListener;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

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
public class SftpFileBaseTableView extends FXTableView<ShellSftpFile> implements FXEventListener {

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
    }

    private String filterText;

    public void setFilterText(String filterText) {
        if (!StringUtil.equals(this.filterText, filterText)) {
            this.filterText = filterText;
            this.refreshFile();
        }
    }

    private boolean showHiddenFile = false;

    public void setShowHiddenFile(boolean showHiddenFile) {
//        if (showHiddenFile != this.showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
        this.refreshFile();
//        }
    }

    public ShellClient getClient() {
        return client;
    }

    public void setClient(ShellClient client) {
        this.client = client;
    }

    protected ShellClient client;

    /**
     * 位置属性
     */
    private final StringProperty locationProperty = new SimpleStringProperty();

    public String getLocation() {
        return this.locationProperty.get();
    }

    public StringProperty locationProperty() {
        return this.locationProperty;
    }

    protected void setLocation(String location) {
        if (StringUtil.notEquals(this.getLocation(), location)) {
            this.clearItems();
        }
        this.locationProperty.set(location);
    }

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

    public ShellSftp sftp() {
        return this.client.openSftp();
    }

    protected List<ShellSftpFile> files;

    public void loadFile() {
        try {
            this.loadFileInner();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    protected synchronized void loadFileInner() throws SftpException {
        ShellSftp sftp = this.sftp();
        try {
            String currPath = this.getLocation();
            if (currPath == null) {
                this.setLocation(sftp.pwd());
                currPath = this.getLocation();
            } else if (currPath.isBlank()) {
                currPath = "/";
            }
            JulLog.info("current path: {}", currPath);
            // 更新当前列表
            this.files = sftp.lsFile(currPath, this.client);
            // 过滤出来待显示的列表
            List<ShellSftpFile> files = this.doFilter(this.files);
            // 当前在显示的列表
            List<ShellSftpFile> items = this.getItems();
            // 删除列表
            List<ShellSftpFile> delList = new ArrayList<>();
            // 新增列表
            List<ShellSftpFile> addList = new ArrayList<>();
            // 遍历已有集合，如果不在待显示列表，则删除，否则更新
            for (ShellSftpFile file : items) {
                Optional<ShellSftpFile> optional = files.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
                if (optional.isEmpty()) {
                    delList.add(file);
                } else {
                    file.copy(optional.get());
                }
            }
            // 遍历待显示列表，如果不在已显示列表，则新增
            for (ShellSftpFile file : files) {
                Optional<ShellSftpFile> optional = items.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
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
                sftp.close();
                this.loadFileInner();
            } else {
                throw ex;
            }
        }
    }

    public void refreshFile() {
        if (this.files == null) {
            this.loadFile();
        } else {
            this.setItem(this.doFilter(this.files));
        }
    }

    protected List<ShellSftpFile> doFilter(List<ShellSftpFile> files) {
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
                    .sorted(Comparator.comparingInt(ShellSftpFile::getOrder))
                    .collect(Collectors.toList());
        }
//        return files;
        return new CopyOnWriteArrayList<>(files);
    }

    protected boolean checkInvalid(List<ShellSftpFile> files) {
        for (ShellSftpFile file : files) {
            if (this.checkInvalid(file)) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkInvalid(ShellSftpFile file) {
        return file.isCurrentFile() || file.isReturnDirectory() || file.isWaiting();
    }

    @Override
    public List<? extends MenuItem> getMenuItems() {
        List<ShellSftpFile> files = this.getSelectedItems();
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

        List<MenuItem> menuItems = new ArrayList<>();
        // 创建文件
        FXMenuItem touch = MenuItemHelper.touchFile("12", this::touch);
        menuItems.add(touch);
        // 创建文件夹
        FXMenuItem mkdir = FXMenuItem.newItem(I18nHelper.mkdir(), new FolderSVGGlyph("12"), this::mkdir);
        menuItems.add(mkdir);
        menuItems.add(MenuItemHelper.separator());
        if (files.size() == 1) {
            ShellSftpFile file = files.getFirst();
            // 文件信息
            FXMenuItem fileInfo = MenuItemHelper.fileInfo("12", () -> this.showFileInfo(file));
            menuItems.add(fileInfo);
            // 复制路径
            FXMenuItem copyFilePath = MenuItemHelper.copyFilePath("12", () -> this.copyFilePath(file));
            menuItems.add(copyFilePath);
            // 重命名文件
            FXMenuItem renameFile = MenuItemHelper.renameFile("12", () -> this.renameFile(file));
            menuItems.add(renameFile);
            // 文件权限
            FXMenuItem filePermission = MenuItemHelper.filePermission("12", () -> this.filePermission(file));
            menuItems.add(filePermission);
            menuItems.add(MenuItemHelper.separator());
        }
        // 刷新文件
        FXMenuItem refreshFile = MenuItemHelper.refreshFile("12", this::loadFile);
        menuItems.add(refreshFile);
        // 删除文件
        FXMenuItem deleteFile = MenuItemHelper.deleteFile("12", () -> this.deleteFile(files));
        menuItems.add(deleteFile);
        return menuItems;
    }

    protected void showFileInfo(ShellSftpFile file) {
        ShellEventUtil.showFileInfo(file);
    }

    protected void copyFilePath(ShellSftpFile file) {
        ClipboardUtil.copy(file.getFilePath());
    }

    protected void onMouseClicked(MouseEvent event) {
        try {
            // 鼠标后退
            if (event.getButton() == MouseButton.BACK && event.getClickCount() == 1) {
                this.back();
                return;
            }
            List<ShellSftpFile> files = this.getSelectedItems();
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
                ShellSftpFile file = files.getFirst();
                if (file.isDir()) {
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

    public boolean currentIsRootDirectory() {
        return "/".equals(this.getLocation());
    }

    public void intoDir(ShellSftpFile file) {
        if (file.isReturnDirectory()) {
            this.returnDir();
            return;
        }
        this.intoDir(file.getFilePath());
    }

    public void intoDir(String filePath) {
        this.setLocation(filePath);
        this.loadFile();
    }

    /**
     * 后退
     */
    public void back() {
        String location = this.getLocation();
        String parent = ShellSftpUtil.parent(location);
        this.intoDir(parent);
    }

    /**
     * 前进
     */
    public void forward() {
        List<ShellSftpFile> files = this.getSelectedItems();
        if (files.size() != 1) {
            return;
        }
        ShellSftpFile file = files.getFirst();
        if (!file.isDir()) {
            return;
        }
        this.intoDir(file);
    }

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
     * 进入home目录
     */
    public void intoHome() {
        this.intoDir(this.client.getUserHome());
    }

    public boolean existFile(String fileName) {
        Optional<ShellSftpFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
        return sftpFile.isPresent();
    }

    public void deleteFile(List<ShellSftpFile> files) {
        if (CollectionUtil.isEmpty(files) || this.checkInvalid(files)) {
            return;
        }
        if (files.size() == 1) {
            ShellSftpFile file = files.getFirst();
            if (file.isDir() && !MessageBox.confirm(I18nHelper.deleteDir() + " " + file.getFileName())) {
                return;
            }
            if (!file.isDir() && !MessageBox.confirm(I18nHelper.deleteFile() + " " + file.getFileName())) {
                return;
            }
        } else if (!MessageBox.confirm(ShellI18nHelper.fileTip2())) {
            return;
        }
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<ShellSftpFile> sftpFiles = new CopyOnWriteArrayList<>(files);
                for (ShellSftpFile file : sftpFiles) {
                    // 不可删除文件
                    if (file.isReturnDirectory() || file.isCurrentFile()) {
                        continue;
                    }
                    // 隐藏文件
                    if (file.isHiddenFile() && !MessageBox.confirm(file.getFileName() + " " + ShellI18nHelper.fileTip1())) {
                        continue;
                    }
                    // 执行删除
                    this.client.delete(file);
                }
                this.refreshFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    public void filePermission(ShellSftpFile file) {
        try {
            if (this.checkInvalid(file)) {
                return;
            }
            StageAdapter adapter = StageManager.parseStage(ShellSftpFilePermissionController.class);
            adapter.setProp("file", file);
            adapter.setProp("client", this.client);
            adapter.display();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public void renameFile(ShellSftpFile file) {
        try {
            if (this.checkInvalid(file)) {
                return;
            }
            String newName = MessageBox.prompt(I18nHelper.pleaseInputContent(), file.getFileName());
            String name = file.getFileName();
            if (newName == null || StringUtil.equals(name, newName)) {
                return;
            }
            String filePath = ShellSftpUtil.concat(this.getLocation(), name);
            String newPath = ShellSftpUtil.concat(this.getLocation(), newName);
            this.sftp().rename(filePath, newPath);
            file.setFileName(newName);
            this.refreshFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public void fileDeleted(String remoteFile) {
        Optional<ShellSftpFile> optional = this.files.parallelStream()
                .filter(f -> StringUtil.equals(remoteFile, f.getFilePath()))
                .findAny();
        if (optional.isPresent()) {
            this.files.remove(optional.get());
            this.refreshFile();
        }
    }

    public void cd(String path) {
        if (!StringUtil.isBlank(path)) {
            try {
                if (this.sftp().exist(path)) {
                    this.setLocation(path);
                    this.loadFile();
                }
            } catch (SftpException ex) {
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 编辑文件
     *
     * @param file 文件
     */
    public void editFile(ShellSftpFile file) {
        if (this.checkInvalid(file)) {
            return;
        }
        if (!file.isFile()) {
            return;
        }
        if (file.size() > 500 * 1024) {
            return;
        }
        // 检查类型
        String extName = FileNameUtil.extName(file.getFileName());
        if (!StringUtil.equalsAnyIgnoreCase(extName,
                "txt", "text", "log", "yaml", "java",
                "xml", "json", "htm", "html", "xhtml",
                "php", "css", "c", "cpp", "rs",
                "js", "csv", "sql", "md", "ini",
                "cfg", "sh", "bat", "py", "asp",
                "aspx", "env", "tsv", "conf")) {
            return;
        }
        StageAdapter adapter = StageManager.parseStage(ShellSftpFileEditController.class);
        adapter.setProp("file", file);
        adapter.setProp("client", this.client);
        adapter.display();
    }

    public void touch() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputFileName());
            this.touch(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public void touch(String name) throws SftpException {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip4())) {
            return;
        }
        String filePath = ShellSftpUtil.concat(this.getLocation(), name);
        ShellSftp sftp = this.sftp();
        sftp.touch(filePath);
        SftpATTRS attrs = sftp.stat(filePath);
        ShellSftpFile file = new ShellSftpFile(this.getLocation(), name, attrs);
        // 读取链接文件
        ShellSftpUtil.realpath(file, sftp);
        if (this.client.isWindows()) {
            file.setOwner("-");
            file.setGroup("-");
        } else {
            file.setOwner(ShellSftpUtil.getOwner(file.getUid(), this.client));
            file.setGroup(ShellSftpUtil.getGroup(file.getGid(), this.client));
        }
        this.files.add(file);
        this.refreshFile();
    }


    public void mkdir() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputDirName());
            this.mkdir(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public void mkdir(String name) throws SftpException {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        name = name.trim();
        if (this.existFile(name) && !MessageBox.confirm(ShellI18nHelper.fileTip5())) {
            return;
        }
        String filePath = ShellSftpUtil.concat(this.getLocation(), name);
        ShellSftp sftp = this.sftp();
        sftp.mkdir(filePath);
        SftpATTRS attrs = sftp.stat(filePath);
        ShellSftpFile file = new ShellSftpFile(this.getLocation(), name, attrs);
        // 读取链接文件
        ShellSftpUtil.realpath(file, sftp);
        if (this.client.isWindows()) {
            file.setOwner("-");
            file.setGroup("-");
        } else {
            file.setOwner(ShellSftpUtil.getOwner(file.getUid(), this.client));
            file.setGroup(ShellSftpUtil.getGroup(file.getGid(), this.client));
        }
        this.files.add(file);
        this.refreshFile();
    }

    /**
     * 文件保存事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onFileSaved(ShellSftpFileSavedEvent event) {
        if (this.existFile(event.fileName())) {
            this.refresh();
        }
    }
}
