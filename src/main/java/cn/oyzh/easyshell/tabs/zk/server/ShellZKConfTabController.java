package cn.oyzh.easyshell.tabs.zk.server;

import cn.oyzh.easyshell.dto.zk.ShellZKEnvNode;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.List;

/**
 * zk客户端conf信息tab内容组件
 *
 * @author oyzh
 * @since 2024/12/24
 */
public class ShellZKConfTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 配置信息
     */
    @FXML
    private FXTableView<ShellZKEnvNode> confTable;

    @Override
    public ShellZKServerTabController parent() {
        return (ShellZKServerTabController) super.parent();
    }

    @FXML
    private void refreshConf() {
        // 配置信息
        List<ShellZKEnvNode> confNodes = this.parent().getClient().confNodes();
        this.confTable.setItem(confNodes);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refreshConf();
            }
        });
    }
}
