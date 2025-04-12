package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFilePermissionController;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.tableview.TableViewMouseSelectHelper;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
public class SftpFileBaseTableView extends FXTableView<SftpFile> {

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
            List<FXMenuItem> items = this.getMenuItems();
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

    protected List<SftpFile> files;

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
            }
            JulLog.info("current path: {}", currPath);
            // 更新当前列表
            this.files = sftp.lsFile(currPath, this.client);
            // 过滤出来待显示的列表
            List<SftpFile> files = this.doFilter(this.files);
            // 当前在显示的列表
            List<SftpFile> items = this.getItems();
            // 删除列表
            List<SftpFile> delList = new ArrayList<>();
            // 新增列表
            List<SftpFile> addList = new ArrayList<>();
            // 遍历已有集合，如果不在待显示列表，则删除，否则更新
            for (SftpFile file : items) {
                Optional<SftpFile> optional = files.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
                if (optional.isEmpty()) {
                    delList.add(file);
                } else {
                    file.copy(optional.get());
                }
            }
            // 遍历待显示列表，如果不在已显示列表，则新增
            for (SftpFile file : files) {
                Optional<SftpFile> optional = items.stream().filter(f -> StringUtil.equals(f.getFilePath(), file.getFilePath())).findAny();
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

    protected List<SftpFile> doFilter(List<SftpFile> files) {
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
                    .sorted(Comparator.comparingInt(SftpFile::getOrder))
                    .collect(Collectors.toList());
        }
//        return files;
        return new CopyOnWriteArrayList<>(files);
    }

    protected boolean checkInvalid(List<SftpFile> files) {
        for (SftpFile file : files) {
            if (this.checkInvalid(file)) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkInvalid(SftpFile file) {
        return file.isCurrentFile() || file.isReturnDirectory() || file.isWaiting();
    }

    @Override
    public List<FXMenuItem> getMenuItems() {
        List<SftpFile> files = this.getSelectedItems();
        if (CollectionUtil.isEmpty(files)) {
            return Collections.emptyList();
        }
//        // 发现操作中的文件，则跳过
//        for (SftpFile file : files) {
//            if (file.isWaiting()) {
//                return Collections.emptyList();
//            }
//        }
        // 检查是否包含无效文件
        if (this.checkInvalid(files)) {
            return Collections.emptyList();
        }
        List<FXMenuItem> menuItems = new ArrayList<>();
        if (files.size() == 1) {
            SftpFile file = files.getFirst();
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
        }
        // 删除文件
        FXMenuItem deleteFile = MenuItemHelper.deleteFile("12", () -> this.deleteFile(files));
        menuItems.add(deleteFile);
        return menuItems;
    }

    protected void showFileInfo(SftpFile file) {
        ShellEventUtil.showFileInfo(file);
    }

    protected void copyFilePath(SftpFile file) {
        ClipboardUtil.copy(file.getFilePath());
    }

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

    public void intoDir(SftpFile file) {
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
        Optional<SftpFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
        return sftpFile.isPresent();
    }

    public void deleteFile(List<SftpFile> files) {
        if (CollectionUtil.isEmpty(files) || this.checkInvalid(files)) {
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
        } else if (!MessageBox.confirm(ShellI18nHelper.fileTip2())) {
            return;
        }
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        ThreadUtil.start(() -> {
            try {
                List<SftpFile> sftpFiles = new CopyOnWriteArrayList<>(files);
                for (SftpFile file : sftpFiles) {
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

    public void filePermission(SftpFile file) {
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

    public void renameFile(SftpFile file) {
        try {
            if (this.checkInvalid(file)) {
                return;
            }
            String newName = MessageBox.prompt(I18nHelper.pleaseInputContent(), file.getFileName());
            String name = file.getFileName();
            if (newName == null || StringUtil.equals(name, newName)) {
                return;
            }
            String filePath = SftpUtil.concat(this.getLocation(), name);
            String newPath = SftpUtil.concat(this.getLocation(), newName);
            this.sftp().rename(filePath, newPath);
            file.setFileName(newName);
            this.refreshFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public void fileDeleted(String remoteFile) {
        Optional<SftpFile> optional = this.files.parallelStream()
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
}
