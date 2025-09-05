package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ZKClusterNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
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
     * 集群列表
     */
    @FXML
    private FXTableView<ZKClusterNode> clusterTable;

    @Override
    public ShellZKServerTabController parent() {
        return (ShellZKServerTabController) super.parent();
    }

    @FXML
    private void refreshCluster() {
        // 集群信息
        List<ZKClusterNode> clusterNodes = this.parent().getClient().clusterNodes();
        this.clusterTable.setItem(clusterNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.getTab().selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshCluster();
            }
        });
    }
}
