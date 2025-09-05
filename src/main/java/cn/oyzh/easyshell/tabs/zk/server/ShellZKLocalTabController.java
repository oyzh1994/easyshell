package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk客户端本地信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ShellZKLocalTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 客户端环境
     */
    @FXML
    private FXTableView<ZKEnvNode> localEnvTable;

    @Override
    public ShellZKServerTabController parent() {
        return (ShellZKServerTabController) super.parent();
    }

    @FXML
    private void refreshLocal() {
        // 客户端环境信息
        List<ZKEnvNode> localEnviNodes = this.parent().getClient().localNodes();
        this.localEnvTable.setItem(localEnviNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshLocal();
            }
        });
    }
}
