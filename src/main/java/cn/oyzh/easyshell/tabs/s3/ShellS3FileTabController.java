package cn.oyzh.easyshell.tabs.s3;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.s3.ShellS3FileTableView;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
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
public class ShellS3FileTabController extends SubTabController {

    /**
     * 根节点
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
     * 创建文件
     */
    @FXML
    private SVGGlyph touchFile;

    /**
     * 上传文件夹
     */
    @FXML
    private SVGGlyph uploadDir;

    /**
     * 上传文件
     */
    @FXML
    private SVGGlyph uploadFile;

    /**
     * 删除文件
     */
    @FXML
    private SVGGlyph deleteFile;

    /**
     * 文件表格
     */
    @FXML
    private ShellS3FileTableView fileTable;

    /**
     * 文件过滤
     */
    @FXML
    private ClearableTextField filterFile;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    @Override
    public ShellS3TabController parent() {
        return (ShellS3TabController) super.parent();
    }

    public ShellS3Client client() {
        return this.parent().client();
    }

    /**
     * 初始化
     */
    public void init() {
        ShellS3Client client = this.client();
        // 收起左侧
        if (this.setting.isHiddenLeftAfterConnected()) {
            ShellEventUtil.layout1();
        }
        this.fileTable.setClient(client);
        this.fileTable.refreshFile();
        // 任务数量监听
        client.addTaskSizeListener(() -> {
            if (client.isTaskEmpty()) {
                this.manage.clear();
            } else {
                this.manage.text("(" + client.getTaskSize() + ")");
            }
        });
        // 设置收藏处理
        this.location.setFileCollectSupplier(() -> ShellFileUtil.fileCollect(this.client()));
    }

    @Override
    public void onTabInit(RichTab tab) {
        try {
            super.onTabInit(tab);
            // 监听位置
            this.fileTable.locationProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1 == null) {
                    this.location.clear();
                } else {
                    this.location.text(t1);
                }
                this.initFileAction();
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
                }
            });
            // 绑定提示快捷键
            this.filterFile.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            this.deleteFile.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
            this.refreshFile.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化文件操作
     */
    private void initFileAction() {
        this.touchFile.setDisable(!this.fileTable.isSupportTouchAction());
        this.uploadDir.setDisable(!this.fileTable.isSupportUploadAction());
        this.uploadFile.setDisable(!this.fileTable.isSupportUploadAction());
        this.deleteFile.setDisable(!this.fileTable.isSupportDeleteAction());
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
     * 管理上传、下载
     */
    @FXML
    private void manage() {
        ShellViewFactory.fileManage(this.client());
    }
}
