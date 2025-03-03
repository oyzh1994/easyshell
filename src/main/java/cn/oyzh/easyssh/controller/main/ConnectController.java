package cn.oyzh.easyssh.controller.main;

import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.easyssh.trees.connect.SSHConnectTreeView;
import cn.oyzh.fx.gui.svg.pane.SortSVGPane;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.stage.WindowEvent;


/**
 * ssh连接业务
 *
 * @author oyzh
 * @since 2024/04/23
 */
public class ConnectController extends SubStageController {

    /**
     * 左侧ssh树
     */
    @FXML
    private SSHConnectTreeView tree;

    /**
     * 节点排序组件
     */
    @FXML
    private SortSVGPane sortPane;

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.tree.scrollTo(this.tree.getSelectedItem());
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        // 关闭连接
        this.tree.closeConnects();
        // 取消F5按键监听
        KeyListener.unListenReleased(this.tree, KeyCode.F5);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // ssh树变化事件
        this.tree.selectItemChanged(SSHEventUtil::treeItemChanged);
        // 文件拖拽初始化
        this.stage.initDragFile(this.tree.getDragContent(), this.tree.root()::dragFile);
        // 刷新触发事件
        KeyListener.listenReleased(this.tree, KeyCode.F5, keyEvent -> this.tree.reload());
    }

    @FXML
    private void addConnect() {
        SSHEventUtil.showAddConnect();
    }

    @FXML
    private void sortTree() {
        if (this.sortPane.isAsc()) {
            this.tree.sortAsc();
            this.sortPane.desc();
        } else {
            this.tree.sortDesc();
            this.sortPane.asc();
        }
    }

    @FXML
    private void importConnect() {
        SSHEventUtil.showImportConnect(null);
    }

    @FXML
    private void exportConnect() {
        SSHEventUtil.showExportConnect();
    }
}
