package cn.oyzh.easyshell.tabs.ssh.docker;

import cn.oyzh.easyshell.docker.ShellDockerExec;
import cn.oyzh.easyshell.fx.docker.ShellDockerImageTableView;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHDockerTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
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
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellSSHDockerImageTabController extends SubTabController {

    /**
     * ssh命令行文本域
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

    private boolean initialized = false;

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
    public void onTabInit(RichTab tab) {
        try {
            super.onTabInit(tab);
            this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
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

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    @FXML
    public void refreshImage() {
        StageManager.showMask(() -> this.imageTable.loadImage());
    }

    @FXML
    private void deleteImage() {
        this.imageTable.deleteImage(this.imageTable.getSelectedItem(), false);
    }

    @FXML
    private void deleteImageForce() {
        this.imageTable.deleteImage(this.imageTable.getSelectedItem(), true);
    }
}
