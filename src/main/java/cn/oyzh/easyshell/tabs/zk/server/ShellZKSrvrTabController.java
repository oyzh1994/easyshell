package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk客户端srvr信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ShellZKSrvrTabController extends SubTabController {

    /**
     * 服务信息
     */
    @FXML
    private FXTableView<ZKEnvNode> srvrTable;

    @Override
    public ShellZKServerTabController parent() {
        return (ShellZKServerTabController) super.parent();
    }

    @FXML
    private void refreshSrvr() {
        // 服务信息
        List<ZKEnvNode> srvrNodes = this.parent().getClient().srvrNodes();
        this.srvrTable.setItem(srvrNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.getTab().selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshSrvr();
            }
        });
    }
}
