package cn.oyzh.easyshell.tabs.redis.key;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStringKeyTreeItem;
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
 * redis键信息组件
 *
 * @author oyzh
 * @since 2023/08/03
 */
public class ShellRedisKeyHexController extends RichTabController {

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
    private ShellRedisKeyTreeItem keyItem;

    /**
     * 初始化数据
     */
    public void init(ShellRedisKeyTreeItem keyItem) {
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
            if (this.keyItem instanceof ShellRedisStringKeyTreeItem stringKeyItem) {
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
            MessageBox.exception(ex);
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
