package cn.oyzh.easyshell.tabs.zk;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.filter.zk.ShellZKNodeFilterTypeComboBox;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.tabs.zk.node.ShellZKNodeTab;
import cn.oyzh.easyshell.tabs.zk.query.ShellZKQueryTab;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.zk.ShellZKViewFactory;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.gui.text.field.FilterTextField;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * zk标签页内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellZKTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private ShellZKClient client;

    /**
     * 根节点
     */
    @FXML
    private FXSplitPane root;

    /**
     * 标签页容器
     */
    @FXML
    private ShellZKTabPane tabPane;

    /**
     * 节点树
     */
    @FXML
    private ShellZKTreeView treeView;

    /**
     * 过滤类型
     */
    @FXML
    private ShellZKNodeFilterTypeComboBox filterType;

    /**
     * 过滤内容
     */
    @FXML
    private FilterTextField filterKW;

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        this.client = ShellClientUtil.newClient(connect);
        // 监听连接状态
        this.client.addStateListener((observableValue, shellConnState, t1) -> {
            if (t1 == ShellConnState.INTERRUPTED) {
                MessageBox.warn(ShellI18nHelper.connectSuspended(this.client.connectName()), this.getWindow());
            }
        });
        // 加载根节点
        StageManager.showMask(this.getWindow(), () -> {
            try {
                if (!this.client.isConnected()) {
                    this.client.start();
                }
                if (ThreadUtil.isInterrupted()) {
                    return;
                }
                if (!this.client.isConnected()) {
                    this.client.close();
                    MessageBox.warn(I18nHelper.connectFail(), this.getWindow());
                    this.closeTab();
                    return;
                }
                this.tabPane.setClient(this.client);
                this.treeView.setTabPane(this.tabPane);
                this.treeView.client(this.client);
                this.treeView.loadItems();
                this.treeView.root().expend();
                this.hideLeft();
            } catch (Throwable ex) {
                if (!ThreadUtil.isInterrupted()) {
                    ex.printStackTrace();
                    MessageBox.exception(ex, this.getWindow());
                }
                this.closeTab();
            }
        }, this::closeTab);
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellZKClient getClient() {
        return client;
    }

    @Override
    public void onTabCloseRequest(Event event) {
        boolean unsaved = false;
        for (Tab tab : this.tabPane.getTabs()) {
            if (tab instanceof ShellZKNodeTab nodeTab && nodeTab.hasUnsavedData()) {
                unsaved = true;
                break;
            }
            if (tab instanceof ShellZKQueryTab queryTab && queryTab.isUnsaved()) {
                unsaved = true;
                break;
            }
        }
        if (unsaved && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        }
    }

    @Override
    public void onTabClosed(Event event) {
        // 关闭全部子标签页
        if (this.tabPane != null) {
            this.tabPane.closeAllTabs();
        }
        super.onTabClosed(event);
        IOUtil.closeAsync(this.client);
    }

    /**
     * 执行过滤
     */
    private void doFilter() {
        if (this.treeView == null) {
            return;
        }
        String kw = this.filterKW.getTextTrim();
        // 匹配大小写
        boolean matchCase = this.filterKW.isMatchCase();
        // 全字模式
        boolean wholeWord = this.filterKW.isWholeWord();
        // 过滤类型
        int type = this.filterType.getSelectedIndex();
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(matchCase);
        this.treeView.setHighlight(kw);
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setType((byte) type);
        this.treeView.getItemFilter().setMatchCase(matchCase);
        this.treeView.getItemFilter().setWholeWord(wholeWord);
        ThreadUtil.start(this.treeView::filter);
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }

    /**
     * 复制节点路径
     */
    @FXML
    private void copyNodePath() {
        if (this.treeView.getSelectedItem() instanceof ShellZKNodeTreeItem item) {
            ClipboardUtil.setStringAndTip(item.decodeNodePath());
        }
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ShellZKViewFactory.importData(this.client.getShellConnect());
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellZKViewFactory.exportData(this.client.getShellConnect(), null);
    }

    /**
     * 传输数据
     */
    @FXML
    private void transportData() {
        ShellZKViewFactory.transportData(this.client.getShellConnect());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 过滤处理
        this.filterType.selectedIndexChanged((observable, oldValue, newValue) -> this.doFilter());
        // 过滤
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        searchKeyHandler.setMainModifierDown(true);
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
        // 内容过滤
        this.filterKW.textProperty().addListener((observable, oldValue, newValue) -> this.doFilter());
        this.filterKW.wholeWordPropery().addListener((observable, oldValue, newValue) -> this.doFilter());
        this.filterKW.matchCasePropery().addListener((observable, oldValue, newValue) -> this.doFilter());
    }
}
