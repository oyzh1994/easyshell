package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.filter.mysql.ShellMysqlDataFilterTextField;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeView;
import cn.oyzh.easyshell.util.mysql.ShellMysqlViewFactory;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
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
    private ShellMysqlClient client;

    /**
     * 左侧节点
     */
    @FXML
    private FXVBox leftBox;

    /**
     * 根节点
     */
    @FXML
    private ShellMysqlTabPane tabPane;

    /**
     * db树
     */
    @FXML
    private ShellMysqlTreeView treeView;

    /**
     * 过滤参数
     */
    @FXML
    private ShellMysqlDataFilterTextField filterKW;

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = new ShellMysqlClient(connect);
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
                this.tabPane.setClient(this.client);
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

    public ShellMysqlClient getClient() {
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

    @FXML
    private void doFilter() {
        String kw = this.filterKW.getTextTrim();
        // 过滤模式
        byte mode = this.filterKW.filterMode();
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(mode == 3 || mode == 1);
        this.treeView.setHighlightText(kw);
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setMatchMode(mode);
        ThreadUtil.start(() -> this.treeView.filter());
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    /**
     * 传输数据
     */
    @FXML
    private void transportData() {
        ShellMysqlViewFactory.transportData(this.client.getShellConnect(), null);
    }

    /**
     * 事件监听器
     */
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
            this.tabPane.setFlexWidth("100% - " + newWidth);
        }
    }

}
