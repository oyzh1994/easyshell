package cn.oyzh.easyshell.tabs.ssh.docker;

import cn.oyzh.easyshell.docker.ShellDockerExec;
import cn.oyzh.easyshell.fx.docker.ShellDockerContainerStatusComboBox;
import cn.oyzh.easyshell.fx.docker.ShellDockerContainerTableView;
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
public class ShellSSHDockerContainerTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    /**
     * 刷新容器
     */
    @FXML
    private SVGGlyph refreshContainer;

    /**
     * 删除容器
     */
    @FXML
    private SVGGlyph deleteContainer;

    /**
     * 过滤容器
     */
    @FXML
    private ClearableTextField filterContainer;

    /**
     * 容器table
     */
    @FXML
    private ShellDockerContainerTableView containerTable;

    @FXML
    private ShellDockerContainerStatusComboBox containerStatus;

    private boolean initialized = false;

    public void init(ShellDockerExec exec) {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        try {
            this.containerTable.setExec(exec);
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
                    this.init(this.client().dockerExec());
                }
            });
            this.filterContainer.addTextChangeListener((observableValue, aBoolean, t1) -> {
                this.containerTable.setFilterText(t1);
            });
            this.containerStatus.selectedIndexChanged((observableValue, aBoolean, t1) -> {
                this.containerTable.setStatus(t1.byteValue());
            });
            // 快捷键
            this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (KeyboardUtil.search_keyCombination.match(event)) {
                    this.filterContainer.requestFocus();
                } else if (KeyboardUtil.refresh_keyCombination.match(event)) {
                    this.refreshContainer();
                } else if (KeyboardUtil.delete_keyCombination.match(event)) {
                    this.deleteContainer();
                }
            });
            // 绑定提示快捷键
            this.deleteContainer.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
            this.filterContainer.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            this.refreshContainer.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
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
    public void refreshContainer() {
//        // 设置执行对象
//        if (this.containerTable.getExec() == null) {
//            this.containerTable.setExec(this.client().dockerExec());
//        }
        StageManager.showMask(() -> this.containerTable.loadContainer());
    }

    @FXML
    private void deleteContainer() {
        this.containerTable.deleteContainer(this.containerTable.getSelectedItem(), false);
    }

    @FXML
    private void deleteContainerForce() {
        this.containerTable.deleteContainer(this.containerTable.getSelectedItem(), true);
    }
}
