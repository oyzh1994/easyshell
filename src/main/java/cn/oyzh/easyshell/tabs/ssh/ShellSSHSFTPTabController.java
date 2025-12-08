package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.sftp.ShellSSHSFTPFileTableView;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.sftp2.ShellSFTPFile;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.util.List;

/**
 * ssh-sftp组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellSSHSFTPTabController extends SubTabController {

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * 当前位置
     */
    @FXML
    private ShellFileLocationTextField location;

    /**
     * 上传/下载管理
     */
    @FXML
    private SVGLabel manage;

    /**
     * 刷新文件
     */
    @FXML
    private SVGGlyph refreshFile;

    /**
     * 删除文件
     */
    @FXML
    private SVGGlyph deleteFile;

    /**
     * 隐藏文件
     */
    @FXML
    private HiddenSVGPane hiddenPane;

    /**
     * 文件表格
     */
    @FXML
    private ShellSSHSFTPFileTableView fileTable;

    /**
     * 文件过滤
     */
    @FXML
    private ClearableTextField filterFile;

    /**
     * 文件信息
     */
    @FXML
    private FXLabel fileInfo;

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;
    //
    // /**
    //  * 设置储存
    //  */
    // private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 初始化标志位
     */
    private boolean initialized = false;

    /**
     * 初始化
     */
    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.fileTable.setSSHClient(this.client());
        // 显示隐藏文件
        this.hiddenFile(this.shellConnect().isShowHiddenFile());
        // this.hiddenFile(this.setting.isShowHiddenFile());
        ShellSFTPClient sftpClient = this.sftpClient();
        // 任务数量监听
        sftpClient.addTaskSizeListener(() -> {
            if (sftpClient.isTaskEmpty("upload,download")) {
                this.manage.clear();
            } else {
                this.manage.text("(" + sftpClient.getTaskSize() + ")");
            }
        }, "upload,download");
        // 设置收藏处理
        this.location.setFileCollectSupplier(() -> ShellFileUtil.fileCollect(sftpClient));
    }

//    @Override
//    public void onTabClosed(Event event) {
//        super.onTabClosed(event);
//        this.client().close();
//    }

    @Override
    public void onTabInit(FXTab tab) {
        try {
            super.onTabInit(tab);
            this.root.selectedProperty().subscribe((aBoolean, t1) -> {
                if (t1) {
                    this.init();
                }
            });
            // 监听位置
            this.fileTable.locationProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1 == null) {
                    this.location.clear();
                } else {
                    this.location.text(t1);
                }
            });
            // 文件过滤
            this.filterFile.addTextChangeListener((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setFilterText(t1);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            // 路径跳转
            this.location.setOnJumpLocation(path -> {
                this.fileTable.cd(path);
            });
            // 快捷键
            this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (KeyboardUtil.search_keyCombination.match(event)) {
                    this.filterFile.requestFocus();
                    //} else if (KeyboardUtil.hide_keyCombination.match(event)) {
                    //    this.hiddenFile();
                }
            });
            // 监听信息
            this.fileTable.itemList().addListener((ListChangeListener<ShellSFTPFile>) c -> {
                this.fileInfo.setText(this.fileTable.fileInfo());
            });
            // 绑定提示快捷键
            // this.hiddenPane.setTipKeyCombination(KeyboardUtil.hide_keyCombination);
            this.filterFile.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            this.deleteFile.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
            this.refreshFile.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellSSHTabController parent() {
        return (ShellSSHTabController) super.parent();
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    public ShellSFTPClient sftpClient() {
        return this.client().sftpClient();
    }

    /**
     * 刷新文件
     */
    @FXML
    private void refreshFile() {
        try {
            this.fileTable.loadFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除文件
     */
    @FXML
    private void deleteFile() {
        try {
            this.fileTable.deleteFile(this.fileTable.getSelectedItems());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 返回上一级
     */
    @FXML
    private void returnDir() {
        try {
            this.fileTable.returnDir();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 进入home目录
     */
    @FXML
    private void intoHome() {
        try {
            this.fileTable.intoHome();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 创建文件夹
     */
    @FXML
    private void mkdir() {
        this.fileTable.createDir();
    }

    /**
     * 创建文件
     */
    @FXML
    private void touchFile() {
        this.fileTable.touch();
    }

    /**
     * 上传文件
     */
    @FXML
    private void uploadFile() {
        this.fileTable.uploadFile();
    }

    /**
     * 上传文件夹
     */
    @FXML
    private void uploadFolder() {
        this.fileTable.uploadFolder();
    }

    /**
     * 文件拖拽事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void draggedFile(ShellFileDraggedEvent event) {
        try {
            // 判断是否选中
            if (!this.root.isSelected() || !this.getTab().isSelected()) {
                return;
            }
            List<File> files = event.data();
            if (this.fileTable.isPkgTransfer()) {
                this.fileTable.updateByPkg(files, this.client());
            } else {
                this.fileTable.uploadFile(files);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 管理上传、下载
     */
    @FXML
    private void manage() {
        ShellViewFactory.fileManage(this.sftpClient());
    }

    /**
     * 隐藏文件
     */
    @FXML
    private void hiddenFile() {
        this.hiddenFile(this.hiddenPane.isHidden());
    }

    /**
     * 隐藏文件
     *
     * @param showHidden 是否显示隐藏文件
     */
    private void hiddenFile(boolean showHidden) {
        if (!showHidden) {
            this.hiddenPane.hidden();
            this.fileTable.setShowHiddenFile(false);
            this.hiddenPane.setTipText(I18nHelper.showHiddenFiles());
        } else {
            this.hiddenPane.show();
            this.fileTable.setShowHiddenFile(true);
            this.hiddenPane.setTipText(I18nHelper.doNotShowHiddenFiles());
        }
        this.shellConnect().setShowHiddenFile(showHidden);
        // this.setting.setShowHiddenFile(hidden);
        // this.settingStore.update(this.setting);
    }

    @Override
    public void destroy() {
        this.fileTable.destroy();
        super.destroy();
    }
}
