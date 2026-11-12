package cn.oyzh.easyshell.tabs.valkey;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.filter.valkey.ShellValkeyKeyFilterTypeComboBox;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.tabs.valkey.key.ShellValkeyKeyTab;
import cn.oyzh.easyshell.tabs.valkey.query.ShellValkeyQueryTab;
import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeView;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.valkey.ShellValkeyViewFactory;
import cn.oyzh.fx.gui.text.field.FilterTextField;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * redis标签页内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private ShellValkeyClient client;

    /**
     * 根节点
     */
    @FXML
    private FXSplitPane root;

    /**
     * 标签页容器
     */
    @FXML
    private ShellValkeyTabPane tabPane;

    /**
     * db树
     */
    @FXML
    private ShellValkeyTreeView treeView;

    /**
     * 过滤参数
     */
    @FXML
    private FilterTextField filterKW;

    /**
     * 过滤类型
     */
    @FXML
    private ShellValkeyKeyFilterTypeComboBox filterType;

    /**
     * 获取shell连接
     *
     * @return shell连接
     */
    public ShellConnect shellConnect() {
        return this.client.shellConnect();
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
                this.treeView.setClient(this.client);
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
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient getClient() {
        return client;
    }

    @Override
    public void onTabCloseRequest(Event event) {
        boolean unsaved = false;
        for (Tab tab : this.tabPane.getTabs()) {
            if (tab instanceof ShellValkeyKeyTab keyTab && keyTab.hasUnsavedData()) {
                unsaved = true;
                break;
            }
            if (tab instanceof ShellValkeyQueryTab queryTab && queryTab.isUnsaved()) {
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
        this.treeView.getItemFilter().setWholeWord(wholeWord);
        this.treeView.getItemFilter().setMatchCase(matchCase);
        this.treeView.getItemFilter().setType((byte) type);
        ThreadUtil.start(this.treeView::filter);
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ShellValkeyViewFactory.importData(this.client.shellConnect(), null);
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellValkeyViewFactory.exportData(this.client.shellConnect(), null);
    }

    /**
     * 传输数据
     */
    @FXML
    private void transportData() {
        ShellValkeyViewFactory.transportData(this.client.shellConnect(), null);
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
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
