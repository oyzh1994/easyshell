package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.trees.mysql.DBTreeView;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;

/**
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private MysqlClient client;

    /**
     * 左侧节点
     */
    @FXML
    private FXVBox leftBox;

    /**
     * 根节点
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * db树
     */
    @FXML
    private DBTreeView treeView;

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = new MysqlClient(connect);
        // 加载根节点
        StageManager.showMask(() -> {
            try {
                this.client.start();
                if (!this.client.isConnected()) {
                    this.client.close();
                    MessageBox.warn(I18nHelper.connectFail());
                    this.closeTab();
                    return;
                }
                this.treeView.setClient(this.client);
                this.treeView.root().loadChild();
                this.treeView.root().expend();
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public MysqlClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.close(this.client);
        if (this.listener != null) {
            this.listener.unregister();
            this.listener = null;
        }
    }

    public void doFilter(ActionEvent actionEvent) {
    }

    @FXML
    public void importData() {
    }

    @FXML
    public void exportData() {
    }

    @FXML
    public void positionNode() {
    }

    @FXML
    public void transportData() {
    }

    private ShellMysqlTabEventListener listener;

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        this.listener = new ShellMysqlTabEventListener(this.tabPane);
        this.listener.register();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 拉伸辅助
        NodeWidthResizer resizer = new NodeWidthResizer(this.leftBox, Cursor.DEFAULT, this::resizeLeft);
        resizer.widthLimit(240f, 750f);
        resizer.initResizeEvent();
    }

    /**
     * 左侧组件重新布局
     *
     * @param newWidth 新宽度
     */
    private void resizeLeft(Float newWidth) {
        if (newWidth != null && !Float.isNaN(newWidth)) {
            // 设置组件宽
            this.leftBox.setRealWidth(newWidth);
            // this.tabPane.setLayoutX(newWidth);
            this.tabPane.setFlexWidth("100% - " + newWidth);
            // this.leftBox.parentAutosize();
        }
    }

}
