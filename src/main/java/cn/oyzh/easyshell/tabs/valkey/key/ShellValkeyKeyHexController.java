package cn.oyzh.easyshell.tabs.valkey.key;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyStringKeyTreeItem;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.hex.HexStatusLabel;
import cn.oyzh.fx.plus.controls.hex.HexView;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * redis键hex视图组件
 *
 * @author oyzh
 * @since 2026-07-13
 */
public class ShellValkeyKeyHexController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab hexRoot;

    /**
     * hex视图组件
     */
    @FXML
    private HexView hexView;

    /**
     * hex状态组件
     */
    @FXML
    private HexStatusLabel statusLabel;

    /**
     * 当前文件
     */
    private File file;

    /**
     * 键
     */
    private ShellValkeyKeyTreeItem keyItem;

    /**
     * 初始化数据
     *
     * @param keyItem redis键节点
     */
    public void init(ShellValkeyKeyTreeItem keyItem) {
        this.keyItem = keyItem;
        if (this.hexRoot.isSelected()) {
            this.initObject();
        }
    }

    /**
     * 初始化对象
     */
    private void initObject() {
        try {
            if (this.keyItem instanceof ShellValkeyStringKeyTreeItem stringKeyItem) {
                if (this.file == null) {
                    this.file = new File(ShellConst.getCachePath(), UUIDUtil.uuidSimple() + ".hex");
                }
                Object data = stringKeyItem.data();
                if (data instanceof String s) {
                    FileUtil.writeString(s, this.file);
                } else if (data instanceof byte[] bytes) {
                    FileUtil.writeBytes(bytes, this.file);
                }
                this.hexView.enable();
                this.hexView.openFile(this.file);
                this.statusLabel.init(this.hexView);
            } else {
                this.hexView.close();
                this.hexView.disable();
                this.statusLabel.stop();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        super.initialize(location, resourceBundle);
        this.hexRoot.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.initObject();
            }
        });
    }

    @Override
    public void destroy() {
        this.hexView.destroy();
        this.statusLabel.destroy();
        FileUtil.del(this.file);
        super.destroy();
    }
}
