package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ZKEnvNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk客户端envi信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ZKEnviTabController extends SubTabController {

    /**
     * 服务端环境
     */
    @FXML
    private FXTableView<ZKEnvNode> enviTable;

    @Override
    public ZKServerTabController parent() {
        return (ZKServerTabController) super.parent();
    }

    @FXML
    private void refreshEnvi() {
        // 服务端环境信息
        List<ZKEnvNode> serverEnviNodes = this.parent().getClient().enviNodes();
        this.enviTable.setItem(serverEnviNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.getTab().selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshEnvi();
            }
        });
    }
}
