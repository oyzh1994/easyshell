package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ShellZKClusterNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk客户端集群信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ShellZKClusterTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 集群列表
     */
    @FXML
    private FXTableView<ShellZKClusterNode> clusterTable;

    @Override
    public ShellZKServerTabController parent() {
        return (ShellZKServerTabController) super.parent();
    }

    @FXML
    private void refreshCluster() {
        // 集群信息
        List<ShellZKClusterNode> clusterNodes = this.parent().getClient().clusterNodes();
        this.clusterTable.setItem(clusterNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshCluster();
            }
        });
    }
}
