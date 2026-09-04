package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.query.ShellDamengQueryUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.tabs.ShellBaseTabController;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeView;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
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
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengTabController extends ShellBaseTabController {

    /**
     * 客户端
     */
    private ShellDamengClient client;

    /**
     * 根节点
     */
    @FXML
    private FXSplitPane root;

    /**
     * 根节点
     */
    @FXML
    private ShellDamengTabPane tabPane;

    /**
     * db树
     */
    @FXML
    private ShellDamengTreeView treeView;

    /**
     * 过滤参数
     */
    @FXML
    private FilterTextField filterKW;

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
                MessageBox.warn("[" + this.client.connectName() + "] " + I18nHelper.connectSuspended());
            }
        });
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
                // 更新数据索引
                ShellDamengQueryUtil.updateIndex(this.client);
                this.hideLeft();
            } catch (Throwable ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.closeTab();
            }
        });
    }

    public ShellDamengClient getClient() {
        return client;
    }

    @Override
    public void onTabClosed(Event event) {
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
        ShellDamengViewFactory.importData(this.client, null);
    }

    /**
     * 导出数据
     */
    @FXML
    private void exportData() {
        ShellDamengViewFactory.exportData(this.client, null, null);
    }

    /**
     * 运行sql文件
     */
    @FXML
    private void runSqlFile() {
        ShellDamengViewFactory.runSqlFile(this.client, null);
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
        ShellDamengViewFactory.transportData(this.client.getShellConnect(), null);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 过滤
        KeyHandler searchKeyHandler = new KeyHandler();
        searchKeyHandler.setHandler(e -> this.filterKW.requestFocus());
        searchKeyHandler.setKeyCode(KeyCode.F);
        searchKeyHandler.setMainModifierDown(true);
        searchKeyHandler.setKeyType(KeyEvent.KEY_RELEASED);
        KeyListener.addHandler(this.root, searchKeyHandler);
        // 内容过滤
        this.filterKW.textProperty().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
        this.filterKW.wholeWordPropery().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
        this.filterKW.matchCasePropery().addListener((observable, oldValue, newValue) -> {
            this.doFilter();
        });
    }
}
