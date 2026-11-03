package cn.oyzh.easyshell.tabs.sqlite;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeView;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.gui.text.field.FilterTextField;
import cn.oyzh.fx.plus.controls.pane.FXSplitPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyHandler;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * SQLite 连接标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private ShellSqliteClient client;

    /**
     * 根节点
     */
    @FXML
    private FXSplitPane root;

    /**
     * 标签页容器
     */
    @FXML
    private ShellSqliteTabPane tabPane;

    /**
     * 数据库树
     */
    @FXML
    private ShellSqliteTreeView treeView;

    /**
     * 过滤参数
     */
    @FXML
    private FilterTextField filterKW;

    /**
     * 获取shell连接
     *
     * @return shell连接
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
        this.client.addStateListener((observable, oldValue, newValue) -> {
            if (newValue == ShellConnState.INTERRUPTED) {
                MessageBox.warn("[" + this.client.connectName() + "] " + I18nHelper.connectSuspended(), this.getWindow());
            }
        });
        // 加载根节点
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.client.start();
                if (!this.client.isConnected()) {
                    this.client.close();
                    MessageBox.warn(I18nHelper.connectFail(), this.getWindow());
                    this.closeTab();
                    return;
                }
                this.tabPane.setClient(this.client);
                this.tabPane.setTableAddedHandler(name -> this.treeView.root().addTable(name));
                this.tabPane.setTableCountChangedHandler(() -> this.treeView.root().refreshTableCount());
                this.tabPane.setViewAddedHandler(name -> this.treeView.root().addView(name));
                this.tabPane.setViewCountChangedHandler(() -> this.treeView.root().refreshViewCount());
                this.tabPane.setQueryChangedHandler(query -> this.treeView.root().upsertQuery(query));
                this.tabPane.setQueryDeletedHandler(query -> this.treeView.root().removeQuery(query));
                this.treeView.setClient(this.client);
                this.treeView.setTabPane(this.tabPane);
                this.tabPane.setTreeView(this.treeView);
                this.treeView.root().loadChild();
                this.treeView.root().expend();
                // 更新数据索引
                ShellSqliteQueryUtil.updateIndex(this.client);
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
                this.closeTab();
            }
        });
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellSqliteClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        IOUtil.closeAsync(this.client);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        searchKeyHandler.setMainModifierDown(true);
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
        this.filterKW.textProperty().addListener((observable, oldValue, newValue) -> this.doFilter());
        this.filterKW.wholeWordPropery().addListener((observable, oldValue, newValue) -> this.doFilter());
        this.filterKW.matchCasePropery().addListener((observable, oldValue, newValue) -> this.doFilter());
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
        // 设置高亮是否匹配大小写
        this.treeView.setHighlightMatchCase(matchCase);
        this.treeView.setHighlight(kw);
        this.treeView.getItemFilter().setKw(kw);
        this.treeView.getItemFilter().setMatchCase(matchCase);
        this.treeView.getItemFilter().setWholeWord(wholeWord);
        ThreadUtil.start(() -> this.treeView.filter());
    }

    /**
     * 导入数据
     */
    @FXML
    private void importData() {
        ShellSqliteViewFactory.importData(this.client);
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellSqliteViewFactory.exportData(this.client, null);
    }

    /**
     * 运行SQL文件
     */
    @FXML
    private void runSqlFile() {
        ShellSqliteViewFactory.runSqlFile(this.client);
    }

    /**
     * 传输数据
     */
    @FXML
    private void transportData() {
        ShellSqliteViewFactory.transportData(this.client.getShellConnect());
    }

    /**
     * 定位节点
     */
    @FXML
    private void positionNode() {
        this.treeView.positionItem();
    }
}
