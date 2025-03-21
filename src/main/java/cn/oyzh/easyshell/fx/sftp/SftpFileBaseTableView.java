package cn.oyzh.easyshell.fx.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFileEditController;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteDeleted;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteEnded;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.plus.chooser.DirChooserHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SftpFileBaseTableView extends FXTableView<SftpFile> {

    @Override
    protected void initTableView() {
        super.initTableView();
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    protected void initEvenListener() {
        super.initEvenListener();
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<SftpFile> files = this.getSelectedItems();
            if (CollectionUtil.isNotEmpty(files)) {
                this.showContextMenu(this.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
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

    private boolean showHiddenFile = true;

    public void setShowHiddenFile(boolean showHiddenFile) {
        if (showHiddenFile != this.showHiddenFile) {
            this.showHiddenFile = showHiddenFile;
            this.refreshFile();
        }
    }

    public ShellClient getClient() {
        return client;
    }

    public void setClient(ShellClient client) {
        this.client = client;
    }

    protected ShellClient client;

    /**
     * 当前路径
     */
    private final StringProperty currPathProperty = new SimpleStringProperty();

    public String getCurrPath() {
        return this.currPathProperty.get();
    }

    public StringProperty currPathProperty() {
        return this.currPathProperty;
    }

    protected void setCurrPath(String currPath) {
        this.currPathProperty.set(currPath);
    }

    protected String currPath() {
        return this.currPathProperty.get();
    }

    protected void currPath(String currPath) {
        if (StringUtil.notEquals(this.currPath(), currPath)) {
            this.clearItems();
        }
        this.currPathProperty.set(currPath);
    }

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
            String currPath = this.getCurrPath();
            if (currPath == null) {
                this.setCurrPath(sftp.pwd());
                currPath = this.getCurrPath();
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
            if (ExceptionUtil.hasMessage(ex, "inputstream is closed", "4:", "0: Success")) {
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

    @Override
    public List<FXMenuItem> getMenuItems() {
        List<SftpFile> files = this.getSelectedItems();
        if (CollectionUtil.isEmpty(files)) {
            return Collections.emptyList();
        }
        List<FXMenuItem> menuItems = new ArrayList<>();
        if (files.size() == 1) {
            SftpFile file = files.getFirst();
            FXMenuItem fileInfo = MenuItemHelper.fileInfo("12", () -> this.showFileInfo(file));
            menuItems.add(fileInfo);
            FXMenuItem copyFilePath = MenuItemHelper.copyFilePath("12", () -> this.copyFilePath(file));
            menuItems.add(copyFilePath);
        }
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
        return "/".equals(this.currPath());
    }

    public void intoDir(SftpFile file)   {
        if (file.isReturnDirectory()) {
            this.returnDir();
            return;
        }
        String currPath = SftpUtil.concat(this.currPath(), file.getFileName());
        this.currPath(currPath);
        this.loadFile();
    }

    public void returnDir() {
        if (this.currentIsRootDirectory()) {
            return;
        }
        String currPath = this.currPath();
        if (currPath.endsWith("/")) {
            currPath = currPath.substring(0, currPath.length() - 1);
        }
        currPath = currPath.substring(0, currPath.lastIndexOf("/") + 1);
        this.currPath(currPath);
        this.loadFile();
    }

    public boolean existFile(String fileName) {
        Optional<SftpFile> sftpFile = this.files.parallelStream().filter(f -> StringUtil.equals(fileName, f.getFileName())).findAny();
        return sftpFile.isPresent();
    }
}
