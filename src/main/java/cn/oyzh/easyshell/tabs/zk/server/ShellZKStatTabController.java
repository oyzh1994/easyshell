package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk客户端stat信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ShellZKStatTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 状态信息
     */
    @FXML
    private FXTableView<ZKEnvNode> statTable;

    @Override
    public ShellZKServerTabController parent() {
        return (ShellZKServerTabController) super.parent();
    }

    @FXML
    private void refreshStat() {
        // 状态信息
        List<ZKEnvNode> statNodes = this.parent().getClient().statNodes();
        this.statTable.setItem(statNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshStat();
            }
        });
    }
}
