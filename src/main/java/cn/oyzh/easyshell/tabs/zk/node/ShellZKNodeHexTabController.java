package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.hex.HexStatusLabel;
import cn.oyzh.fx.plus.controls.hex.HexView;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.fxml.FXML;

import java.io.File;

/**
 * zk节点hex组件
 *
 * @author oyzh
 * @since 2025/04/11
 */
public class ShellZKNodeHexTabController extends SubTabController {

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
     * 初始化数据
     */
    public void initHex() {
        try {
            if (this.activeItem() != null) {
                if (this.file == null) {
                    this.file = new File(ShellConst.getCachePath(), UUIDUtil.uuidSimple() + ".hex");
                }
                FileUtil.writeBytes(this.activeItem().getData(), this.file);
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

    private ShellZKNodeTreeItem activeItem() {
        return this.parent().getActiveItem();
    }

    @Override
    public ShellZKNodeTabController parent() {
        return (ShellZKNodeTabController) super.parent();
    }

    @Override
    public void destroy() {
        this.hexView.destroy();
        FileUtil.del(this.file);
        super.destroy();
    }
}