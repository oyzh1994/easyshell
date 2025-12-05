package cn.oyzh.easyshell.tabs.ftp;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.ftp.ShellFTPClient;
import cn.oyzh.easyshell.ftp.ShellFTPFile;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.ftp.ShellFTPFileTableView;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.util.List;

/**
 * ftp组件
 *
 * @author oyzh
 * @since 2025/04/25
 */
public class ShellFTPTabController extends ShellBaseTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

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
    private ShellFTPFileTableView fileTable;

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

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    // /**
    //  * 设置储存
    //  */
    // private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 连接储存
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * ftp客户端
     */
    private ShellFTPClient client;

    public ShellFTPClient client() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化
     */
    public void init(ShellConnect shellConnect) {
        this.client = new ShellFTPClient(shellConnect);
        StageManager.showMask(() -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (!this.client.isConnected()) {
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                // 收起左侧
                // if (this.setting.isHiddenLeftAfterConnected()) {
                //     ShellEventUtil.layout1();
                // }
                this.hideLeft();
                this.fileTable.setClient(this.client);
                // 显示隐藏文件
                this.hiddenFile(this.shellConnect().isShowHiddenFile());
                // this.hiddenFile(this.setting.isShowHiddenFile());
                // 任务数量监听
                this.client.addTaskSizeListener(() -> {
                    if (this.client.isTaskEmpty("upload,download")) {
                        this.manage.clear();
                    } else {
                        this.manage.text("(" + this.client.getTaskSize() + ")");
                    }
                }, "upload,download");
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
        // 保存设置
        this.connectStore.update(this.shellConnect());
        // // 展开左侧
        // if (this.setting.isHiddenLeftAfterConnected()) {
        //     ShellEventUtil.layout2();
        // }
    }

    @Override
    public void onTabInit(FXTab tab) {
        try {
            super.onTabInit(tab);
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
            this.root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (KeyboardUtil.search_keyCombination.match(event)) {
                    this.filterFile.requestFocus();
                //} else if (KeyboardUtil.hide_keyCombination.match(event)) {
                //    this.hiddenFile();
                }
            });
            // 监听信息
            this.fileTable.itemList().addListener((ListChangeListener<ShellFTPFile>) c -> {
                this.fileInfo.setText(this.fileTable.fileInfo());
            });
            // 绑定提示快捷键
            //this.hiddenPane.setTipKeyCombination(KeyboardUtil.hide_keyCombination);
            this.filterFile.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            this.deleteFile.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
            this.refreshFile.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }


    @FXML
    private void refreshFile() {
        try {
            this.fileTable.loadFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void deleteFile() {
        try {
            this.fileTable.deleteFile(this.fileTable.getSelectedItems());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

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

    @FXML
    private void mkdir() {
        this.fileTable.createDir();
    }

    @FXML
    private void touchFile() {
        this.fileTable.touch();
    }

    @FXML
    private void uploadFile() {
        this.fileTable.uploadFile();
    }

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
            if (!this.getTab().isSelected()) {
                return;
            }
            List<File> files = event.data();
            this.fileTable.uploadFile(files);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }


    /**
     * 隐藏文件
     */
    @FXML
    private void hiddenFile() {
        this.hiddenFile(this.hiddenPane.isHidden());
    }

    /**
     * 管理上传、下载
     */
    @FXML
    private void manage() {
        ShellViewFactory.fileManage(this.client);
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
