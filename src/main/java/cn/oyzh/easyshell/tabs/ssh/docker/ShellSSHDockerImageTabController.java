package cn.oyzh.easyshell.tabs.ssh.docker;

import cn.oyzh.easyshell.event.docker.ShellContainerCommitEvent;
import cn.oyzh.easyshell.event.docker.ShellImageTagEvent;
import cn.oyzh.easyshell.fx.docker.ShellDockerImageTableView;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHDockerTabController;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * docker镜像tab内容组件
 *
 * @author oyzh
 * @since 2025-03-23
 */
public class ShellSSHDockerImageTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 刷新镜像
     */
    @FXML
    private SVGGlyph refreshImage;

    /**
     * 删除镜像
     */
    @FXML
    private SVGGlyph deleteImage;

    /**
     * 过滤镜像
     */
    @FXML
    private ClearableTextField filterImage;

    /**
     * 镜像table
     */
    @FXML
    private ShellDockerImageTableView imageTable;

    /**
     * 是否已初始化
     */
    private boolean initialized = false;

    /**
     * 初始化镜像
     */
    private void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            ShellDockerExec exec = this.client().dockerExec();
            this.imageTable.setExec(exec);
            this.refreshImage();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabInit(FXTab tab) {
        try {
            super.onTabInit(tab);
            this.root.selectedProperty().subscribe((aBoolean, t1) -> {
                if (t1) {
                    this.init();
                }
            });
            this.filterImage.addTextChangeListener((observableValue, aBoolean, t1) -> {
                this.imageTable.setFilterText(t1);
            });
            // 快捷键
            this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (KeyboardUtil.search_keyCombination.match(event)) {
                    this.filterImage.requestFocus();
                } else if (KeyboardUtil.refresh_keyCombination.match(event)) {
                    this.refreshImage();
                } else if (KeyboardUtil.delete_keyCombination.match(event)) {
                    this.deleteImage();
                }
            });
            // 绑定提示快捷键
            this.deleteImage.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
            this.filterImage.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            this.refreshImage.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellSSHDockerTabController parent() {
        return (ShellSSHDockerTabController) super.parent();
    }

    /**
     * 获取ssh客户端
     *
     * @return ssh客户端
     */
    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    /**
     * 刷新镜像
     */
    @FXML
    public void refreshImage() {
        this.imageTable.setExec(this.client().dockerExec());
        StageManager.showMask(() -> {
            try {
                this.imageTable.loadImage();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 删除镜像
     */
    @FXML
    private void deleteImage() {
        this.imageTable.deleteImage(this.imageTable.getSelectedItem(), false);
    }

    /**
     * 强制删除镜像
     */
    @FXML
    private void deleteImageForce() {
        this.imageTable.deleteImage(this.imageTable.getSelectedItem(), true);
    }

    /**
     * 标签修改事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onImageTag(ShellImageTagEvent event) {
        if (event.data() == this.client().dockerExec()) {
            this.refreshImage();
        }
    }

    /**
     * 容器保存事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onContainerCommit(ShellContainerCommitEvent event) {
        if (event.data() == this.client().dockerExec()) {
            this.refreshImage();
        }
    }

//    @Override
//    public void destroy() {
//        this.imageTable.destroy();
//        super.destroy();
//    }
}
